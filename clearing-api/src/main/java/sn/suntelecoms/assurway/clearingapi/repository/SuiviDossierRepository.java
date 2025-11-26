package sn.suntelecoms.assurway.clearingapi.repository;

import sn.suntelecoms.assurway.clearingapi.model.SuiviDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SuiviDossierRepository extends JpaRepository<SuiviDossier, UUID> {
    
    List<SuiviDossier> findByDossierIdOrderByDateActionDesc(UUID dossierId);
}
