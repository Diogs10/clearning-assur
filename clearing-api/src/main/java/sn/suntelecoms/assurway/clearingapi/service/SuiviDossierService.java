package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.suntelecoms.assurway.clearingapi.constantes.StatutDossierRecours;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.model.DossierRecours;
import sn.suntelecoms.assurway.clearingapi.model.SuiviDossier;
import sn.suntelecoms.assurway.clearingapi.repository.DossierRecoursRepository;
import sn.suntelecoms.assurway.clearingapi.repository.SuiviDossierRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuiviDossierService {

    private final SuiviDossierRepository suiviDossierRepository;
    private final DossierRecoursRepository dossierRecoursRepository;
    private final UserService userService;

    @Transactional
    public void logStatusChange(UUID dossierId, StatutDossierRecours newStatut, String motif) {
        
        DossierRecours dossier = dossierRecoursRepository.findById(dossierId)
            .orElseThrow(() -> new ResourceNotFoundException("Dossier de recours", "id", dossierId));

        String username = userService.getCurrentUser().getUsername(); 
        
        SuiviDossier suivi = SuiviDossier.builder()
            .dossier(dossier)
            .typeAction("CHANGEMENT_STATUT")
            .statutPrecedent(dossier.getStatut())
            .statutActuel(newStatut)
            .description(motif != null ? "Statut changé en " + newStatut + " avec motif: " + motif : "Statut changé en " + newStatut)
            .utilisateurAction(username)
            .build();

        suiviDossierRepository.save(suivi);
    }
        
    public List<SuiviDossier> getSuiviByDossier(UUID dossierId) {
        return suiviDossierRepository.findByDossierIdOrderByDateActionDesc(dossierId);
    }
}