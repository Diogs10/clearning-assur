package sn.suntelecoms.assurway.clearingapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class AssureurDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAssureurRequest {
        @NotBlank(message = "Le nom est obligatoire")
        private String nom;

        private String adresse;

        @NotBlank(message = "Le téléphone est obligatoire")
        private String telephone;

        @Email(message = "Format d'email invalide")
        private String email;

        private String logo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateAssureurRequest {
        private String nom;
        private String adresse;
        private String telephone;
        @Email(message = "Format d'email invalide")
        private String email;
        private String logo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssureurResponse {
        private UUID id;
        private String nom;
        private String adresse;
        private String telephone;
        private String email;
        private String logo;
        private LocalDateTime createdAt;
    }
}
