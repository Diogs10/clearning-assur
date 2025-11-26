package sn.suntelecoms.assurway.clearingapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.suntelecoms.assurway.clearingapi.constantes.StatutDossierRecours;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "suivi_dossier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuiviDossier {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private DossierRecours dossier;

    private String typeAction;
    
    private StatutDossierRecours statutPrecedent;
    private StatutDossierRecours statutActuel;

    private String description;
    
    private String utilisateurAction;

    private LocalDateTime dateAction;

    @PrePersist
    void onCreate() {
        this.dateAction = LocalDateTime.now();
    }
}
