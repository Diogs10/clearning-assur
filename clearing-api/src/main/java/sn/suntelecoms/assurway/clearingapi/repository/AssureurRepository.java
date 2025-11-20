package sn.suntelecoms.assurway.clearingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.suntelecoms.assurway.clearingapi.model.Assureur;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssureurRepository extends JpaRepository<Assureur, UUID> {

    boolean existsByNom(String nom);

    boolean existsByEmail(String email);

    Optional<Assureur> findByNom(String nom);

    Optional<Assureur> findByEmail(String email);
}
