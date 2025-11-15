package sn.suntelecoms.assurway.clearingapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RoleDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRoleRequest {
        @NotBlank(message = "L'authority est obligatoire")
        private String authority;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleResponse {
        private UUID id;
        private String authority;
        private LocalDateTime createdAt;
        private Set<PrivilegeDTO.PrivilegeResponse> privileges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetRolePrivilegesRequest {
        @NotNull(message = "L'ID du rôle est obligatoire")
        private UUID idRole;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignPrivilegesToRoleRequest {
        @NotBlank(message = "Le rôle est obligatoire")
        private String role;
        private List<UUID> privileges;
    }
}