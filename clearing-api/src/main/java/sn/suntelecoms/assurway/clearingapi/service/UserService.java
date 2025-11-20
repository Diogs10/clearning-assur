package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.suntelecoms.assurway.clearingapi.dto.RoleDTO;
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

        if (request.getRole() != null && !request.getRole().isEmpty()) {
            Role role = roleRepository.findByAuthority(request.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle", "authority", request.getRole()));
            user.getRoles().add(role);
        } else {
            Role defaultRole = roleRepository.findByAuthority("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle USER non trouvé"));
            user.getRoles().add(defaultRole);
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

    public Page<UserDTO.UserResponse> getAllUsers(int size, int page) {
        Pageable pageable = PageRequest.of(page / size, size);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToUserResponse);
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

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur", "id", id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void changePassword(UUID userId, UserDTO.ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Ancien mot de passe incorrect");
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
        
        if (user.getRoles() != null) {
            response.setRoles(user.getRoles().stream()
                    .map(role -> {
                        RoleDTO.RoleResponse roleResponse = 
                            new RoleDTO.RoleResponse();
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