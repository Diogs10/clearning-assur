package sn.suntelecoms.assurway.clearingapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.suntelecoms.assurway.clearingapi.model.Garantie;

public interface GarantieRepository extends JpaRepository<Garantie, UUID>{
    
    boolean existsByLibelle(String libelle);

    Optional<Garantie> findByLibelle(String libelle);
}
