package sn.suntelecoms.assurway.clearingapi.model;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dossier_recours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierRecours {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID assureurDestinataireId;

    private String commentaire;

    private LocalDateTime dateSinistre;

    private String immatriculationAssure;
    private String immatriculationTiers;

    private Double montantRecours;
    private Double montantSinistre;

    private String natureDomage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<UUID> natureGarantieIds;

    private String niveauResponsabilite;

    private String nomAssure;
    private String nomTiers;

    private String numeroDossier;

    private String responsabiliteEnOeuvre;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Document> documents;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Embeddable
    @Data
    public static class Document {
        private String fileName;
        private String mimeType;
        private Long size;
        private String type;
        private String uuid;
    }
}
