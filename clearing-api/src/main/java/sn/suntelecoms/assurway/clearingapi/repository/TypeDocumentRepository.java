package sn.suntelecoms.assurway.clearingapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.suntelecoms.assurway.clearingapi.model.TypeDocument;


public interface TypeDocumentRepository extends JpaRepository<TypeDocument, UUID>{
    
    boolean existsByLibelle(String libelle);

    Optional<TypeDocument> findByLibelle(String libelle);
}