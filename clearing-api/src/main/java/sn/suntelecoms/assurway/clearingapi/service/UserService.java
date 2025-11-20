package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.NotFoundException;
import sn.suntelecoms.assurway.clearingapi.dto.AssureurDTO;
import sn.suntelecoms.assurway.clearingapi.dto.RoleDTO;
import sn.suntelecoms.assurway.clearingapi.dto.UserDTO;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceAlreadyExistsException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.exception.BusinessException;
import sn.suntelecoms.assurway.clearingapi.model.Assureur;
import sn.suntelecoms.assurway.clearingapi.model.Role;
import sn.suntelecoms.assurway.clearingapi.model.User;
import sn.suntelecoms.assurway.clearingapi.repository.AssureurRepository;
import sn.suntelecoms.assurway.clearingapi.repository.RoleRepository;
import sn.suntelecoms.assurway.clearingapi.repository.UserRepository;
import sn.suntelecoms.assurway.clearingapi.service.KeycloakUserManagementService.UserAlreadyExistsException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AssureurRepository assureurRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakUserManagementService keycloakUserManagementService;

    @Transactional
    public UserDTO.UserResponse createUser(UserDTO.CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Utilisateur", "email", request.getEmail());
        }

        String username = request.getEmail().split("@")[0];
        int counter = 1;
        String originalUsername = username;
        while (userRepository.existsByUsername(username)) {
            username = originalUsername + counter++;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTelephone(request.getTelephone());
        user.setPassword(passwordEncoder.encode(request.getPassword())); 
        user.setEnabled(true);
        user.setHasPasswordUpdate(false);
        if (request.getAssureurId() != null) {
            Assureur assureur = assureurRepository.findById(request.getAssureurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assureur", "id", request.getAssureurId()));
            user.setAssureur(assureur);
        }
        String roleAuthority;
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            Role role = roleRepository.findByAuthority(request.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle", "authority", request.getRole()));
            user.getRoles().add(role);
            roleAuthority = role.getAuthority();
        } else {
            Role defaultRole = roleRepository.findByAuthority("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle USER non trouvé"));
            user.getRoles().add(defaultRole);
            roleAuthority = defaultRole.getAuthority();
        }
        try {
            String keycloakId = keycloakUserManagementService.createUser(
                request.getEmail(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                roleAuthority
            );
            
            user.setKeycloakId(keycloakId); 

        } catch (UserAlreadyExistsException e) {
            throw new ResourceAlreadyExistsException("Utilisateur Keycloak", "email", request.getEmail());
        }

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public UserDTO.UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", username));
        return mapToUserResponse(user);
    }

    public UserDTO.UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        return mapToUserResponse(user);
    }

    public Page<UserDTO.UserResponse> getAllUsers(int page, int size) {
        int safePage = Math.max(0, page); 
        Pageable pageable = PageRequest.of(safePage, size);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToUserResponse);
    }

    public List<UserDTO.UserResponse> getAllUsersAsList() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public UserDTO.UserResponse updateUser(UUID id, UserDTO.UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null) {
            if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("Utilisateur", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getTelephone() != null) user.setTelephone(request.getTelephone());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
        if (request.getAssureurId() != null) {
            Assureur assureur = assureurRepository.findById(request.getAssureurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assureur", "id", request.getAssureurId()));
            user.setAssureur(assureur);
        }       
        try {
            Map<String, List<String>> attributes = new HashMap<>();
            if (request.getTelephone() != null) {
                attributes.put("telephone", List.of(request.getTelephone()));
            }

            keycloakUserManagementService.updateUser(
                user.getKeycloakId(),
                user.getEmail(),
                user.getFirstName(), 
                user.getLastName()
            );
        } catch (NotFoundException e) {
            log.warn("Utilisateur local {} existe, mais introuvable dans Keycloak (ID: {}). Poursuite de la mise à jour locale.", id, user.getKeycloakId());
        }

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        
        try {
            keycloakUserManagementService.deleteUser(user.getKeycloakId());
        } catch (NotFoundException e) {
             log.warn("Utilisateur Keycloak avec l'ID {} introuvable. Procède à la suppression locale.", user.getKeycloakId());
        }
        
        userRepository.delete(user);
    }

    @Transactional
    public void changePassword(UUID userId, UserDTO.ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Ancien mot de passe incorrect");
        }
        
        try {
            keycloakUserManagementService.resetPassword(user.getKeycloakId(), request.getNewPassword());
        } catch (NotFoundException e) {
            log.warn("Utilisateur local {} existe, mais introuvable dans Keycloak (ID: {}). Le mot de passe Keycloak n'a pas pu être changé.", userId, user.getKeycloakId());
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setHasPasswordUpdate(true);
        userRepository.save(user);
    }

    @Transactional
    public UserDTO.UserResponse assignRolesToUser(UUID userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByAuthority(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle", "authority", roleName));
            roles.add(role);
        }
        List<String> authorities = roles.stream().map(Role::getAuthority).collect(Collectors.toList());
        try {
            keycloakUserManagementService.assignRoles(user.getKeycloakId(), authorities.toArray(new String[0]));
        } catch (NotFoundException e) {
             log.warn("Utilisateur local {} existe, mais introuvable dans Keycloak (ID: {}). L'assignation des rôles Keycloak a échoué.", userId, user.getKeycloakId());
        }
        user.setRoles(roles);
        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    private UserDTO.UserResponse mapToUserResponse(User user) {
        UserDTO.UserResponse response = new UserDTO.UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setDisplayName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setTelephone(user.getTelephone());
        response.setEnabled(user.getEnabled());
        response.setHasPasswordUpdate(user.getHasPasswordUpdate());
        response.setCreatedAt(user.getCreatedAt());
        if (user.getAssureur() != null) {
            response.setAssureur(new AssureurDTO.AssureurResponse(
                    user.getAssureur().getId(),
                    user.getAssureur().getNom(),
                    user.getAssureur().getAdresse(),
                    user.getAssureur().getTelephone(),
                    user.getAssureur().getEmail(),
                    user.getAssureur().getLogo(),
                    user.getAssureur().getCreatedAt()
            ));
        }
        
        if (user.getRoles() != null) {
            response.setRoles(user.getRoles().stream()
                    .map(role -> {
                        RoleDTO.RoleResponse roleResponse = new RoleDTO.RoleResponse();
                        roleResponse.setId(role.getId());
                        roleResponse.setAuthority(role.getAuthority());
                        roleResponse.setCreatedAt(role.getCreatedAt());
                        return roleResponse;
                    })
                    .collect(Collectors.toSet()));
        }
        
        return response;
    }
}