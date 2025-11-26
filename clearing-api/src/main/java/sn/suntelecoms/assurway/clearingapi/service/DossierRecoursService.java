package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.suntelecoms.assurway.clearingapi.constantes.StatutDossierRecours;
import sn.suntelecoms.assurway.clearingapi.dto.AssureurDTO;
import sn.suntelecoms.assurway.clearingapi.dto.DossierRecoursDTO;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceAlreadyExistsException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.model.Assureur;
import sn.suntelecoms.assurway.clearingapi.model.DossierRecours;
import sn.suntelecoms.assurway.clearingapi.model.User;
import sn.suntelecoms.assurway.clearingapi.repository.AssureurRepository;
import sn.suntelecoms.assurway.clearingapi.repository.DossierRecoursRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DossierRecoursService {

    private final DossierRecoursRepository dossierRecoursRepository;
    private final AssureurRepository assureurRepository;
    private final UserService userService;
    private final SuiviDossierService suiviDossierService;

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

        Assureur assureur = assureurRepository.findById(request.getAssureurDestinataireId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assureur", "id", request.getAssureurDestinataireId()));

        User currentUser;
        Assureur assureurSource;
        
        try {
            currentUser = userService.getCurrentUserWithAssureur();
            assureurSource = currentUser.getAssureur();
            
        } catch (RuntimeException e) {
            throw new RuntimeException(
                "Impossible de créer le dossier: " + e.getMessage() + 
                " Veuillez contacter l'administrateur pour lier votre compte à un assureur."
            );
        }

        DossierRecours dossier = DossierRecours.builder()
                .assureurDestinataireId(assureur)
                .assureurSourceId(assureurSource)
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

    public DossierRecoursDTO.DossierRecoursResponse getDossierById(UUID id) {
        
        DossierRecours dossier = dossierRecoursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier de recours", "id", id));
        
        return mapToResponse(dossier);
    }

    @Transactional
    public DossierRecoursDTO.DossierRecoursResponse updateDossierStatus(
            UUID dossierId, StatutDossierRecours newStatut, String motif) {

        DossierRecours dossier = dossierRecoursRepository.findById(dossierId)
            .orElseThrow(() -> new ResourceNotFoundException("Dossier de recours", "id", dossierId));
        
        if (StatutDossierRecours.from(dossier.getStatut().getValue())) {
            if (!dossier.getStatut().equals(newStatut)) {
                dossier.setStatut(newStatut);
            }
        } else {
            throw new IllegalArgumentException("Statut invalide: " + dossier.getStatut().getValue());
        }
        
        DossierRecours updatedDossier = dossierRecoursRepository.save(dossier);

        suiviDossierService.logStatusChange(dossierId, newStatut, motif); 

        return mapToResponse(updatedDossier);
    }

    public DossierRecoursDTO.DossierRecoursResponse getDossierByNumero(String numeroDossier) {
        
        DossierRecours dossier = dossierRecoursRepository.findByNumeroDossier(numeroDossier)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Dossier de recours", 
                    "numeroDossier", 
                    numeroDossier
                ));
        
        return mapToResponse(dossier);
    }

    public Page<DossierRecoursDTO.DossierRecoursResponse> getAllDossiers(int page, int size, String sortBy) {
        
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(Math.max(0, page), size, sort);
        
        Page<DossierRecours> dossiersPage = dossierRecoursRepository.findAll(pageable);
        
        return dossiersPage.map(this::mapToResponse);
    }

    public Page<DossierRecoursDTO.DossierRecoursResponse> getDossiersByAssureur(
            UUID assureurId, int page, int size) {
        
        
        Pageable pageable = PageRequest.of(Math.max(0, page), size, 
                                          Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<DossierRecours> dossiersPage = 
            dossierRecoursRepository.findByAssureurDestinataireId(assureurId, pageable);
        
        return dossiersPage.map(this::mapToResponse);
    }

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
        
        
        Pageable pageable = PageRequest.of(Math.max(0, page), size, 
                                          Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<DossierRecours> dossiersPage = dossierRecoursRepository.searchDossiers(
                assureurId, nomAssure, nomTiers, startDate, endDate, 
                montantMin, montantMax, pageable
        );
        
        return dossiersPage.map(this::mapToResponse);
    }

    public Page<DossierRecoursDTO.DossierRecoursResponse> searchByNomAssure(
            String nomAssure, int page, int size) {
        
        
        Pageable pageable = PageRequest.of(Math.max(0, page), size, 
                                          Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<DossierRecours> dossiersPage = 
            dossierRecoursRepository.findByNomAssureContainingIgnoreCase(nomAssure, pageable);
        
        return dossiersPage.map(this::mapToResponse);
    }

    public Page<DossierRecoursDTO.DossierRecoursResponse> searchByImmatriculation(
            String immatriculation, int page, int size) {
        
        
        Pageable pageable = PageRequest.of(Math.max(0, page), size, 
                                          Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<DossierRecours> dossiersAssure = 
            dossierRecoursRepository.findByImmatriculationAssure(immatriculation, pageable);
        
        if (!dossiersAssure.isEmpty()) {
            return dossiersAssure.map(this::mapToResponse);
        }
        
        Page<DossierRecours> dossiersTiers = 
            dossierRecoursRepository.findByImmatriculationTiers(immatriculation, pageable);
        
        return dossiersTiers.map(this::mapToResponse);
    }

    public Page<DossierRecoursDTO.DossierRecoursResponse> getRecentDossiers(int days, int page, int size) {
        
        LocalDateTime dateDebut = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(Math.max(0, page), size);
        
        Page<DossierRecours> dossiersPage = 
            dossierRecoursRepository.findRecentDossiers(dateDebut, pageable);
        
        return dossiersPage.map(this::mapToResponse);
    }

    @Transactional
    public DossierRecoursDTO.DossierRecoursResponse updateDossier(UUID id, DossierRecoursDTO.DossierRecoursUpdateRequest request) {
        
        DossierRecours dossier = dossierRecoursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier de recours", "id", id));
        
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
        
        return mapToResponse(updatedDossier);
    }

    @Transactional
    public void deleteDossier(UUID id) {
        
        if (!dossierRecoursRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dossier de recours", "id", id);
        }
        
        dossierRecoursRepository.deleteById(id);
    }

    public Map<String, Object> getStatistiquesByAssureur(UUID assureurId) {
        
        Assureur assureur = assureurRepository.findById(assureurId).get();
        long count = dossierRecoursRepository.countByAssureurDestinataireId(assureur);
        Double total = dossierRecoursRepository.sumMontantRecoursByAssureur(assureurId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("nombreDossiers", count);
        stats.put("montantTotal", total != null ? total : 0.0);
        stats.put("montantMoyen", count > 0 ? (total != null ? total / count : 0.0) : 0.0);
        
        return stats;
    }

    public Map<String, Object> getStatistiquesByPeriod(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        Object[] result = dossierRecoursRepository.getStatisticsByPeriod(startDate, endDate);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("nombreDossiers", result[0] != null ? result[0] : 0L);
        stats.put("montantTotal", result[1] != null ? result[1] : 0.0);
        stats.put("montantMoyen", result[2] != null ? result[2] : 0.0);
        stats.put("periodeDebut", startDate);
        stats.put("periodeFin", endDate);
        
        return stats;
    }

    public List<DossierRecoursDTO.DossierRecoursResponse> getDossiersAvecMontantEleve(Double montantSeuil) {
        
        List<DossierRecours> dossiers = 
            dossierRecoursRepository.findDossiersAvecMontantEleve(montantSeuil);
        
        return dossiers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String generateNumeroDossier() {
        String prefix = "REC";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + "-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }

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

    private DossierRecoursDTO.DossierRecoursResponse mapToResponse(DossierRecours dossier) {
        DossierRecoursDTO.DossierRecoursResponse response = new DossierRecoursDTO.DossierRecoursResponse();
        response.setId(dossier.getId());
        response.setAssureurDestinataire( new AssureurDTO.AssureurResponse(
            dossier.getAssureurDestinataireId().getId(), 
            dossier.getAssureurDestinataireId().getNom(),
            dossier.getAssureurDestinataireId().getAdresse(), 
            dossier.getAssureurDestinataireId().getTelephone(),
            dossier.getAssureurDestinataireId().getEmail(),
            dossier.getAssureurDestinataireId().getLogo(),
            dossier.getAssureurDestinataireId().getCreatedAt()
        ));
        response.setAssureurSource( new AssureurDTO.AssureurResponse(
            dossier.getAssureurSourceId().getId(),
            dossier.getAssureurSourceId().getNom(),
            dossier.getAssureurSourceId().getAdresse(),
            dossier.getAssureurSourceId().getTelephone(),
            dossier.getAssureurSourceId().getEmail(),
            dossier.getAssureurSourceId().getLogo(),
            dossier.getAssureurSourceId().getCreatedAt()
        ));
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
        response.setStatut(dossier.getStatut());
        return response;
    }
}