package sn.suntelecoms.assurway.clearingapi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TypeDocumentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTypeDocumentRequest {
        @NotBlank(message = "Le libelle est obligatoire")
        private String libelle;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTypeDocumentRequest {
        private String libelle;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeDocumentResponse {
        private UUID id;
        private String libelle;
        private LocalDateTime createdAt;
    }
}
