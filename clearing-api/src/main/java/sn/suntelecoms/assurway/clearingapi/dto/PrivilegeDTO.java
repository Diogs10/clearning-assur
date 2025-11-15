package sn.suntelecoms.assurway.clearingapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PrivilegeDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePrivilegeRequest {
        @NotBlank(message = "Le code est obligatoire")
        private String code;

        @NotBlank(message = "Le libellé est obligatoire")
        private String libelle;

        private Integer niveau = 1;
        private String lien;
        private String icon;
        private String isMenu = "N";
        private UUID parentId;
        private Integer ordre = 0;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrivilegeResponse {
        private UUID id;
        private String code;
        private String libelle;
        private Integer niveau;
        private String lien;
        private String icon;
        private String isMenu;
        private UUID parentId;
        private Integer ordre;
        private LocalDateTime createdAt;
        private List<PrivilegeResponse> children;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePrivilegeRequest {
        private String libelle;
        private String lien;
        private String icon;
        private String isMenu;
        private Integer ordre;
    }
}