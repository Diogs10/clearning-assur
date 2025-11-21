package sn.suntelecoms.assurway.clearingapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DossierRecoursDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentDTO {
        private String fileName;
        private String mimeType;
        private Long size;
        private String type;
        private String uuid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DossierRecoursCreateRequest {

        @NotNull
        private UUID assureurDestinataireId;

        private String commentaire;

        @NotNull
        private LocalDateTime dateSinistre;

        private List<DocumentDTO> documents;

        private String immatriculationAssure;
        private String immatriculationTiers;

        private Double montantRecours;
        private Double montantSinistre;

        private String natureDomage;

        private List<UUID> natureGarantieIds;

        private String niveauResponsabilite;

        private String nomAssure;
        private String nomTiers;

        private String numeroDossier;

        private String responsabiliteEnOeuvre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DossierRecoursUpdateRequest {
        private String commentaire;
        private Double montantRecours;
        private Double montantSinistre;
        private List<DocumentDTO> documents;
        private List<UUID> natureGarantieIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DossierRecoursResponse {
        private UUID id;
        private UUID assureurDestinataireId;
        private String numeroDossier;
        private String nomAssure;
        private String nomTiers;
        private LocalDateTime dateSinistre;
        private Double montantRecours;
        private Double montantSinistre;
        private String natureDomage;
        private List<UUID> natureGarantieIds;
        private String responsabiliteEnOeuvre;
        private List<DocumentDTO> documents;
        private LocalDateTime createdAt;
    }
}
