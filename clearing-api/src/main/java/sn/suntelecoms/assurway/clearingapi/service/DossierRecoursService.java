package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.suntelecoms.assurway.clearingapi.dto.DossierRecoursDTO;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceAlreadyExistsException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.model.DossierRecours;
import sn.suntelecoms.assurway.clearingapi.repository.DossierRecoursRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DossierRecoursService {

    private final DossierRecoursRepository dossierRecoursRepository;

    /**
     * Crée un nouveau dossier de recours
     */
    @Transactional
    public DossierRecoursDTO.DossierRecoursResponse createDossier(DossierRecoursDTO.DossierRecoursCreateRequest request) {

        if (request.getNumeroDossier() != null && 
            dossierRecoursRepository.existsByNumeroDossier(request.getNumeroDossier())) {
            throw new ResourceAlreadyExistsException(
                "Dossier de recours", 
                "numeroDossier", 
                request.getNumeroDossier()
            );
        }

        String numeroDossier = request.getNumeroDossier() != null ? 
            request.getNumeroDossier() : 
            generateNumeroDossier();

        DossierRecours dossier = DossierRecours.builder()
                .assureurDestinataireId(request.getAssureurDestinataireId())
                .commentaire(request.getCommentaire())
                .dateSinistre(request.getDateSinistre())
                .immatriculationAssure(request.getImmatriculationAssure())
                .immatriculationTiers(request.getImmatriculationTiers())
                .montantRecours(request.getMontantRecours())
                .montantSinistre(request.getMontantSinistre())
                .natureDomage(request.getNatureDomage())
                .natureGarantieIds(request.getNatureGarantieIds())
                .niveauResponsabilite(request.getNiveauResponsabilite())
                .nomAssure(request.getNomAssure())
                .nomTiers(request.getNomTiers())
                .numeroDossier(numeroDossier)
                .responsabiliteEnOeuvre(request.getResponsabiliteEnOeuvre())
                .documents(convertDocuments(request.getDocuments()))
                .build();

        DossierRecours savedDossier = dossierRecoursRepository.save(dossier);

        return mapToResponse(savedDossier);
    }

    /**
     * Récupère un dossier par son ID
     */
    public DossierRecoursDTO.DossierRecoursResponse getDossierById(UUID id) {
        log.debug("Récupération du dossier ID: {}", id);
        
        DossierRecours dossier = dossierRecoursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier de recours", "id", id));
        
        return mapToResponse(dossier);
    }

    /**
     * Récupère un dossier par son numéro
     */
    public DossierRecoursDTO.DossierRecoursResponse getDossierByNumero(String numeroDossier) {
        log.debug("Récupération du dossier numéro: {}", numeroDossier);
        
        DossierRecours dossier = dossierRecoursRepository.findByNumeroDossier(numeroDossier)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Dossier de recours", 
                    "numeroDossier", 
                    numeroDossier
                ));
        
        return mapToResponse(dossier);
    }

    /**
     * Récupère tous les dossiers avec pagination
     */
    public Page<DossierRecoursDTO.DossierRecoursResponse> getAllDossiers(int page, int size, String sortBy) {
        log.debug("Récupération de tous les dossiers - page: {}, size: {}", page, size);
        
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(Math.max(0, page), size, sort);
        
        Page<DossierRecours> dossiersPage = dossierRecoursRepository.findAll(pageable);
        
        return dossiersPage.map(this::mapToResponse);
    }

    /**
     * Récupère les dossiers par assureur destinataire
     */
    public Page<DossierRecoursDTO.DossierRecoursResponse> getDossiersByAssureur(
            UUID assureurId, int page, int size) {
        
        log.debug("Récupération des dossiers de l'assureur: {}", assureurId);
        
        Pageable pageable = PageRequest.of(Math.max(0, page), size, 
                                          Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<DossierRecours> dossiersPage = 
            dossierRecoursRepository.findByAssureurDestinataireId(assureurId, pageable);
        
        return dossiersPage.map(this::mapToResponse);
    }

    /**
     * Recherche multicritère
     */
    public Page<DossierRecoursDTO.DossierRecoursResponse> searchDossiers(
            UUID assureurId,
            String nomAssure,
            String nomTiers,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Double montantMin,
            Double montantMax,
            int page,
            int size) {
        
        log.debug("Recherche de dossiers avec critères multiples");
        
        Pageable pageable = PageRequest.of(Math.max(0, page), size, 
                                          Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<DossierRecours> dossiersPage = dossierRecoursRepository.searchDossiers(
                assureurId, nomAssure, nomTiers, startDate, endDate, 
                montantMin, montantMax, pageable
        );
        
        return dossiersPage.map(this::mapToResponse);
    }

    /**
     * Recherche par nom de l'assuré
     */
    public Page<DossierRecoursDTO.DossierRecoursResponse> searchByNomAssure(
            String nomAssure, int page, int size) {
        
        log.debug("Recherche des dossiers par nom assuré: {}", nomAssure);
        
        Pageable pageable = PageRequest.of(Math.max(0, page), size, 
                                          Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<DossierRecours> dossiersPage = 
            dossierRecoursRepository.findByNomAssureContainingIgnoreCase(nomAssure, pageable);
        
        return dossiersPage.map(this::mapToResponse);
    }

    /**
     * Recherche par immatriculation
     */
    public Page<DossierRecoursDTO.DossierRecoursResponse> searchByImmatriculation(
            String immatriculation, int page, int size) {
        
        log.debug("Recherche des dossiers par immatriculation: {}", immatriculation);
        
        Pageable pageable = PageRequest.of(Math.max(0, page), size, 
                                          Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // Chercher dans les deux types d'immatriculation
        Page<DossierRecours> dossiersAssure = 
            dossierRecoursRepository.findByImmatriculationAssure(immatriculation, pageable);
        
        if (!dossiersAssure.isEmpty()) {
            return dossiersAssure.map(this::mapToResponse);
        }
        
        Page<DossierRecours> dossiersTiers = 
            dossierRecoursRepository.findByImmatriculationTiers(immatriculation, pageable);
        
        return dossiersTiers.map(this::mapToResponse);
    }

    /**
     * Récupère les dossiers récents (derniers N jours)
     */
    public Page<DossierRecoursDTO.DossierRecoursResponse> getRecentDossiers(int days, int page, int size) {
        log.debug("Récupération des dossiers des {} derniers jours", days);
        
        LocalDateTime dateDebut = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(Math.max(0, page), size);
        
        Page<DossierRecours> dossiersPage = 
            dossierRecoursRepository.findRecentDossiers(dateDebut, pageable);
        
        return dossiersPage.map(this::mapToResponse);
    }

    /**
     * Met à jour un dossier
     */
    @Transactional
    public DossierRecoursDTO.DossierRecoursResponse updateDossier(UUID id, DossierRecoursDTO.DossierRecoursUpdateRequest request) {
        log.info("Mise à jour du dossier ID: {}", id);
        
        DossierRecours dossier = dossierRecoursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier de recours", "id", id));
        
        // Mise à jour des champs modifiables
        if (request.getCommentaire() != null) {
            dossier.setCommentaire(request.getCommentaire());
        }
        if (request.getMontantRecours() != null) {
            dossier.setMontantRecours(request.getMontantRecours());
        }
        if (request.getMontantSinistre() != null) {
            dossier.setMontantSinistre(request.getMontantSinistre());
        }
        if (request.getDocuments() != null) {
            dossier.setDocuments(convertDocuments(request.getDocuments()));
        }
        if (request.getNatureGarantieIds() != null) {
            dossier.setNatureGarantieIds(request.getNatureGarantieIds());
        }
        
        DossierRecours updatedDossier = dossierRecoursRepository.save(dossier);
        log.info("Dossier mis à jour avec succès: ID={}", updatedDossier.getId());
        
        return mapToResponse(updatedDossier);
    }

    /**
     * Supprime un dossier
     */
    @Transactional
    public void deleteDossier(UUID id) {
        log.info("Suppression du dossier ID: {}", id);
        
        if (!dossierRecoursRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dossier de recours", "id", id);
        }
        
        dossierRecoursRepository.deleteById(id);
        log.info("Dossier supprimé avec succès: ID={}", id);
    }

    /**
     * Obtient les statistiques d'un assureur
     */
    public Map<String, Object> getStatistiquesByAssureur(UUID assureurId) {
        log.debug("Calcul des statistiques pour l'assureur: {}", assureurId);
        
        long count = dossierRecoursRepository.countByAssureurDestinataireId(assureurId);
        Double total = dossierRecoursRepository.sumMontantRecoursByAssureur(assureurId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("nombreDossiers", count);
        stats.put("montantTotal", total != null ? total : 0.0);
        stats.put("montantMoyen", count > 0 ? (total != null ? total / count : 0.0) : 0.0);
        
        return stats;
    }

    /**
     * Obtient les statistiques par période
     */
    public Map<String, Object> getStatistiquesByPeriod(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        log.debug("Calcul des statistiques pour la période: {} - {}", startDate, endDate);
        
        Object[] result = dossierRecoursRepository.getStatisticsByPeriod(startDate, endDate);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("nombreDossiers", result[0] != null ? result[0] : 0L);
        stats.put("montantTotal", result[1] != null ? result[1] : 0.0);
        stats.put("montantMoyen", result[2] != null ? result[2] : 0.0);
        stats.put("periodeDebut", startDate);
        stats.put("periodeFin", endDate);
        
        return stats;
    }

    /**
     * Obtient les dossiers avec montant élevé
     */
    public List<DossierRecoursDTO.DossierRecoursResponse> getDossiersAvecMontantEleve(Double montantSeuil) {
        log.debug("Récupération des dossiers avec montant > {}", montantSeuil);
        
        List<DossierRecours> dossiers = 
            dossierRecoursRepository.findDossiersAvecMontantEleve(montantSeuil);
        
        return dossiers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============== Méthodes utilitaires ==============

    /**
     * Génère un numéro de dossier unique
     */
    private String generateNumeroDossier() {
        String prefix = "REC";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + "-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }

    /**
     * Convertit les documents DTO en entités
     */
    private List<DossierRecours.Document> convertDocuments(List<DossierRecoursDTO.DocumentDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }
        
        return dtos.stream()
                .map(dto -> {
                    DossierRecours.Document doc = new DossierRecours.Document();
                    doc.setFileName(dto.getFileName());
                    doc.setMimeType(dto.getMimeType());
                    doc.setSize(dto.getSize());
                    doc.setType(dto.getType());
                    doc.setUuid(dto.getUuid());
                    return doc;
                })
                .collect(Collectors.toList());
    }

    /**
     * Convertit les documents entités en DTOs
     */
    private List<DossierRecoursDTO.DocumentDTO> convertDocumentsToDTOs(
            List<DossierRecours.Document> documents) {
        
        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }
        
        return documents.stream()
                .map(doc -> new DossierRecoursDTO.DocumentDTO(
                        doc.getFileName(),
                        doc.getMimeType(),
                        doc.getSize(),
                        doc.getType(),
                        doc.getUuid()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Mappe une entité vers un DTO de réponse
     */
    private DossierRecoursDTO.DossierRecoursResponse mapToResponse(DossierRecours dossier) {
        DossierRecoursDTO.DossierRecoursResponse response = new DossierRecoursDTO.DossierRecoursResponse();
        response.setId(dossier.getId());
        response.setAssureurDestinataireId(dossier.getAssureurDestinataireId());
        response.setNumeroDossier(dossier.getNumeroDossier());
        response.setNomAssure(dossier.getNomAssure());
        response.setNomTiers(dossier.getNomTiers());
        response.setDateSinistre(dossier.getDateSinistre());
        response.setMontantRecours(dossier.getMontantRecours());
        response.setMontantSinistre(dossier.getMontantSinistre());
        response.setNatureDomage(dossier.getNatureDomage());
        response.setNatureGarantieIds(dossier.getNatureGarantieIds());
        response.setResponsabiliteEnOeuvre(dossier.getResponsabiliteEnOeuvre());
        response.setDocuments(convertDocumentsToDTOs(dossier.getDocuments()));
        response.setCreatedAt(dossier.getCreatedAt());
        return response;
    }
}