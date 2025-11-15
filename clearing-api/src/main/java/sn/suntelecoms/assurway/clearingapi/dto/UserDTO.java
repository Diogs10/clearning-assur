package sn.suntelecoms.assurway.clearingapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class UserDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserRequest {
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        private String email;

        @NotBlank(message = "Le prénom est obligatoire")
        private String firstName;

        @NotBlank(message = "Le nom est obligatoire")
        private String lastName;

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        private String password;

        @NotBlank(message = "Le téléphone est obligatoire")
        private String telephone;

        private String role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetUsernameRequest {
        @NotBlank(message = "Le username est obligatoire")
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private UUID id;
        private String username;
        private String firstName;
        private String lastName;
        private String displayName;
        private String email;
        private String telephone;
        private Boolean enabled;
        private Boolean hasPasswordUpdate;
        private Boolean marchand;
        private LocalDateTime createdAt;
        private Set<RoleDTO.RoleResponse> roles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String telephone;
        private Boolean enabled;
        private Boolean marchand;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank(message = "L'ancien mot de passe est obligatoire")
        private String oldPassword;

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        private String newPassword;
    }
}