package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.suntelecoms.assurway.clearingapi.dto.UserDTO;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceAlreadyExistsException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.exception.BusinessException;
import sn.suntelecoms.assurway.clearingapi.model.Role;
import sn.suntelecoms.assurway.clearingapi.model.User;
import sn.suntelecoms.assurway.clearingapi.repository.RoleRepository;
import sn.suntelecoms.assurway.clearingapi.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Créer un nouvel utilisateur
     */
    @Transactional
    public UserDTO.UserResponse createUser(UserDTO.CreateUserRequest request) {
        log.info("Création d'un utilisateur avec l'email: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Utilisateur", "email", request.getEmail());
        }

        // Générer le username à partir de l'email
        String username = request.getEmail().split("@")[0];
        int counter = 1;
        String originalUsername = username;
        while (userRepository.existsByUsername(username)) {
            username = originalUsername + counter++;
        }

        // Créer l'utilisateur
        User user = new User();
        user.setUsername(username);
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTelephone(request.getTelephone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setMarchand(false);

        // Assigner le rôle
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            Role role = roleRepository.findByAuthority(request.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle", "authority", request.getRole()));
            user.getRoles().add(role);
        } else {
            // Rôle par défaut : USER
            Role defaultRole = roleRepository.findByAuthority("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle USER non trouvé"));
            user.getRoles().add(defaultRole);
        }

        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès: {}", savedUser.getUsername());

        return mapToUserResponse(savedUser);
    }

    /**
     * Récupérer les infos d'un utilisateur par username
     */
    public UserDTO.UserResponse getUserByUsername(String username) {
        log.info("Récupération de l'utilisateur: {}", username);
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", username));
        return mapToUserResponse(user);
    }

    /**
     * Récupérer un utilisateur par ID
     */
    public UserDTO.UserResponse getUserById(UUID id) {
        log.info("Récupération de l'utilisateur ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        return mapToUserResponse(user);
    }

    /**
     * Lister tous les utilisateurs avec pagination
     */
    public Page<UserDTO.UserResponse> getAllUsers(int max, int offset) {
        log.info("Récupération de la liste des utilisateurs (max={}, offset={})", max, offset);
        Pageable pageable = PageRequest.of(offset / max, max);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToUserResponse);
    }

    /**
     * Mettre à jour un utilisateur
     */
    @Transactional
    public UserDTO.UserResponse updateUser(UUID id, UserDTO.UpdateUserRequest request) {
        log.info("Mise à jour de l'utilisateur ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null) {
            // Vérifier si l'email n'est pas déjà utilisé par un autre utilisateur
            if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("Utilisateur", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getTelephone() != null) user.setTelephone(request.getTelephone());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
        if (request.getMarchand() != null) user.setMarchand(request.getMarchand());

        User updatedUser = userRepository.save(user);
        log.info("Utilisateur mis à jour avec succès: {}", updatedUser.getUsername());

        return mapToUserResponse(updatedUser);
    }

    /**
     * Supprimer un utilisateur
     */
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Suppression de l'utilisateur ID: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur", "id", id);
        }
        userRepository.deleteById(id);
        log.info("Utilisateur supprimé avec succès");
    }

    /**
     * Changer le mot de passe
     */
    @Transactional
    public void changePassword(UUID userId, UserDTO.ChangePasswordRequest request) {
        log.info("Changement de mot de passe pour l'utilisateur ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setHasPasswordUpdate(true);
        userRepository.save(user);
        log.info("Mot de passe changé avec succès");
    }

    /**
     * Assigner des rôles à un utilisateur
     */
    @Transactional
    public UserDTO.UserResponse assignRolesToUser(UUID userId, List<String> roleNames) {
        log.info("Attribution de rôles à l'utilisateur ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByAuthority(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle", "authority", roleName));
            roles.add(role);
        }

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        log.info("Rôles attribués avec succès");

        return mapToUserResponse(updatedUser);
    }

    // Méthode utilitaire de mapping
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
        response.setMarchand(user.getMarchand());
        response.setCreatedAt(user.getCreatedAt());
        
        // Mapper les rôles
        if (user.getRoles() != null) {
            response.setRoles(user.getRoles().stream()
                    .map(role -> {
                        sn.suntelecoms.assurway.clearingapi.dto.RoleDTO.RoleResponse roleResponse = 
                            new sn.suntelecoms.assurway.clearingapi.dto.RoleDTO.RoleResponse();
                        roleResponse.setId(role.getId());
                        roleResponse.setAuthority(role.getAuthority());
                        roleResponse.setMarchand(role.getMarchand());
                        roleResponse.setCreatedAt(role.getCreatedAt());
                        return roleResponse;
                    })
                    .collect(Collectors.toSet()));
        }
        
        return response;
    }
}