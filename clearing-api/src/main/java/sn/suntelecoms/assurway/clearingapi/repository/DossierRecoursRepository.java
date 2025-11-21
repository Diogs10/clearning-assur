package sn.suntelecoms.assurway.clearingapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import sn.suntelecoms.assurway.clearingapi.model.Assureur;
import sn.suntelecoms.assurway.clearingapi.model.DossierRecours;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierRecoursRepository extends JpaRepository<DossierRecours, UUID> {

    Optional<DossierRecours> findByNumeroDossier(String numeroDossier);

    boolean existsByNumeroDossier(String numeroDossier);

    Page<DossierRecours> findByAssureurDestinataireId(UUID assureurDestinataireId, Pageable pageable);

    Page<DossierRecours> findByNomAssureContainingIgnoreCase(String nomAssure, Pageable pageable);

    Page<DossierRecours> findByNomTiersContainingIgnoreCase(String nomTiers, Pageable pageable);

    Page<DossierRecours> findByImmatriculationAssure(String immatriculation, Pageable pageable);

    Page<DossierRecours> findByImmatriculationTiers(String immatriculation, Pageable pageable);

    Page<DossierRecours> findByCreatedAtBetween(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable
    );

    Page<DossierRecours> findByDateSinistreBetween(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable
    );

    Page<DossierRecours> findByMontantRecoursGreaterThanEqual(
            Double montantMin, 
            Pageable pageable
    );

    Page<DossierRecours> findByNatureDomageContainingIgnoreCase(
            String natureDomage, 
            Pageable pageable
    );

    @Query("SELECT d FROM DossierRecours d WHERE " +
           "(:assureurId IS NULL OR d.assureurDestinataireId = :assureurId) AND " +
           "(:nomAssure IS NULL OR LOWER(d.nomAssure) LIKE LOWER(CONCAT('%', :nomAssure, '%'))) AND " +
           "(:nomTiers IS NULL OR LOWER(d.nomTiers) LIKE LOWER(CONCAT('%', :nomTiers, '%'))) AND " +
           "(:startDate IS NULL OR d.dateSinistre >= :startDate) AND " +
           "(:endDate IS NULL OR d.dateSinistre <= :endDate) AND " +
           "(:montantMin IS NULL OR d.montantRecours >= :montantMin) AND " +
           "(:montantMax IS NULL OR d.montantRecours <= :montantMax)")
    Page<DossierRecours> searchDossiers(
            @Param("assureurId") UUID assureurId,
            @Param("nomAssure") String nomAssure,
            @Param("nomTiers") String nomTiers,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("montantMin") Double montantMin,
            @Param("montantMax") Double montantMax,
            Pageable pageable
    );

    long countByAssureurDestinataireId(Assureur assureurDestinataire);

    @Query("SELECT SUM(d.montantRecours) FROM DossierRecours d WHERE d.assureurDestinataireId = :assureurId")
    Double sumMontantRecoursByAssureur(@Param("assureurId") UUID assureurId);

    @Query("SELECT COUNT(d), SUM(d.montantRecours), AVG(d.montantRecours) " +
           "FROM DossierRecours d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    Object[] getStatisticsByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT d FROM DossierRecours d WHERE d.createdAt >= :dateDebut ORDER BY d.createdAt DESC")
    Page<DossierRecours> findRecentDossiers(
            @Param("dateDebut") LocalDateTime dateDebut,
            Pageable pageable
    );

    @Query("SELECT d FROM DossierRecours d WHERE d.montantRecours > :montantSeuil ORDER BY d.montantRecours DESC")
    List<DossierRecours> findDossiersAvecMontantEleve(@Param("montantSeuil") Double montantSeuil);

    void deleteByCreatedAtBefore(LocalDateTime date);

    Page<DossierRecours> findByResponsabiliteEnOeuvre(String responsabilite, Pageable pageable);

    Page<DossierRecours> findByNiveauResponsabilite(String niveau, Pageable pageable);

    @Query("SELECT d, SUM(d.montantRecours) as total FROM DossierRecours d " +
           "WHERE d.assureurDestinataireId = :assureurId " +
           "GROUP BY d.id")
    List<Object[]> findDossiersWithTotalByAssureur(@Param("assureurId") UUID assureurId);
}