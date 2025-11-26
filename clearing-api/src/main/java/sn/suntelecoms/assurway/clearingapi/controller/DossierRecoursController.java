package sn.suntelecoms.assurway.clearingapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import sn.suntelecoms.assurway.clearingapi.constantes.StatutDossierRecours;
import sn.suntelecoms.assurway.clearingapi.dto.ApiResponse;
import sn.suntelecoms.assurway.clearingapi.dto.DossierRecoursDTO;
import sn.suntelecoms.assurway.clearingapi.dto.PaginatedResponse;
import sn.suntelecoms.assurway.clearingapi.service.DossierRecoursService;
import sn.suntelecoms.assurway.clearingapi.util.ResponseUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/dossiers-recours")
@RequiredArgsConstructor
@Validated
@Tag(name = "Dossiers de Recours", description = "API de gestion des dossiers de recours")
public class DossierRecoursController {

    private final DossierRecoursService dossierRecoursService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'UNDERWRITER')")
    @Operation(summary = "Créer un dossier de recours", 
               description = "Crée un nouveau dossier de recours avec tous les détails")
    public ResponseEntity<ApiResponse<DossierRecoursDTO.DossierRecoursResponse>> createDossier(
            @Valid @RequestBody DossierRecoursDTO.DossierRecoursCreateRequest request) {
        DossierRecoursDTO.DossierRecoursResponse response = dossierRecoursService.createDossier(request);
        return ResponseUtil.created(response, "Dossier de recours créé avec succès");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'UNDERWRITER', 'VIEWER')")
    @Operation(summary = "Récupérer un dossier par ID", 
               description = "Retourne les détails d'un dossier de recours par son identifiant")
    public ResponseEntity<ApiResponse<DossierRecoursDTO.DossierRecoursResponse>> getDossierById(
            @PathVariable UUID id) {
        
        DossierRecoursDTO.DossierRecoursResponse response = dossierRecoursService.getDossierById(id);
        
        return ResponseUtil.success(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'UNDERWRITER')")
    @Operation(summary = "Mettre à jour le statut d'un dossier",
            description = "Change le statut du dossier de recours et enregistre le motif")
    public ResponseEntity<ApiResponse<DossierRecoursDTO.DossierRecoursResponse>> updateDossierStatus(
            @PathVariable UUID id,
            @RequestParam StatutDossierRecours statut,
            @RequestParam(required = false) String motif) {

        DossierRecoursDTO.DossierRecoursResponse response =  dossierRecoursService.updateDossierStatus(id, statut, motif);

        return ResponseUtil.success(response, "Statut du dossier mis à jour avec succès");
    }

    @GetMapping("/numero/{numeroDossier}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'UNDERWRITER', 'VIEWER')")
    @Operation(summary = "Récupérer un dossier par numéro", 
               description = "Retourne les détails d'un dossier de recours par son numéro")
    public ResponseEntity<ApiResponse<DossierRecoursDTO.DossierRecoursResponse>> getDossierByNumero(
            @PathVariable String numeroDossier) {
        
        DossierRecoursDTO.DossierRecoursResponse response = dossierRecoursService.getDossierByNumero(numeroDossier);
        
        return ResponseUtil.success(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Lister tous les dossiers", 
               description = "Retourne la liste paginée de tous les dossiers de recours")
    public ResponseEntity<ApiResponse<PaginatedResponse<DossierRecoursDTO.DossierRecoursResponse>>> getAllDossiers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy) {
        
        Page<DossierRecoursDTO.DossierRecoursResponse> dossiersPage = 
            dossierRecoursService.getAllDossiers(page, size, sortBy);
        
        return ResponseUtil.successPaginated(dossiersPage, "Liste des dossiers recours récupérée avec succès");
    }

    @GetMapping("/assureur/{assureurId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Lister les dossiers par assureur", 
               description = "Retourne les dossiers d'un assureur destinataire spécifique")
    public ResponseEntity<ApiResponse<PaginatedResponse<DossierRecoursDTO.DossierRecoursResponse>>> getDossiersByAssureur(
            @PathVariable UUID assureurId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DossierRecoursDTO.DossierRecoursResponse> dossiersPage = 
            dossierRecoursService.getDossiersByAssureur(assureurId, page, size);
        
        return ResponseUtil.successPaginated(dossiersPage, "Liste des dossiers recours récupérée avec succès");
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Recherche multicritère", 
               description = "Recherche des dossiers selon plusieurs critères")
    public ResponseEntity<ApiResponse<PaginatedResponse<DossierRecoursDTO.DossierRecoursResponse>>> searchDossiers(
            @RequestParam(required = false) UUID assureurId,
            @RequestParam(required = false) String nomAssure,
            @RequestParam(required = false) String nomTiers,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime endDate,
            @RequestParam(required = false) Double montantMin,
            @RequestParam(required = false) Double montantMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DossierRecoursDTO.DossierRecoursResponse> dossiersPage = 
            dossierRecoursService.searchDossiers(
                assureurId, nomAssure, nomTiers, startDate, endDate, 
                montantMin, montantMax, page, size
            );
        
        return ResponseUtil.successPaginated(dossiersPage, "Liste des dossiers recours récupérée avec succès");
    }

    @GetMapping("/search/assure")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Recherche par nom d'assuré", 
               description = "Recherche des dossiers par nom de l'assuré")
    public ResponseEntity<ApiResponse<PaginatedResponse<DossierRecoursDTO.DossierRecoursResponse>>> searchByNomAssure(
            @RequestParam String nomAssure,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DossierRecoursDTO.DossierRecoursResponse> dossiersPage = 
            dossierRecoursService.searchByNomAssure(nomAssure, page, size);
        
        
        return ResponseUtil.successPaginated(dossiersPage, "Liste des dossiers recours récupérée avec succès");
    }

    @GetMapping("/search/immatriculation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Recherche par immatriculation", 
               description = "Recherche des dossiers par immatriculation (assuré ou tiers)")
    public ResponseEntity<ApiResponse<PaginatedResponse<DossierRecoursDTO.DossierRecoursResponse>>> searchByImmatriculation(
            @RequestParam String immatriculation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DossierRecoursDTO.DossierRecoursResponse> dossiersPage = 
            dossierRecoursService.searchByImmatriculation(immatriculation, page, size);
        
        return ResponseUtil.successPaginated(dossiersPage, "Liste des dossiers recours récupérée avec succès");
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Dossiers récents", 
               description = "Retourne les dossiers créés dans les derniers N jours")
    public ResponseEntity<ApiResponse<PaginatedResponse<DossierRecoursDTO.DossierRecoursResponse>>> getRecentDossiers(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DossierRecoursDTO.DossierRecoursResponse> dossiersPage = 
            dossierRecoursService.getRecentDossiers(days, page, size);
        
        return ResponseUtil.successPaginated(dossiersPage, "Liste des dossiers recours récupérée avec succès");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'UNDERWRITER')")
    @Operation(summary = "Mettre à jour un dossier", 
               description = "Met à jour les informations d'un dossier de recours")
    public ResponseEntity<DossierRecoursDTO.DossierRecoursResponse> updateDossier(
            @PathVariable UUID id,
            @Valid @RequestBody DossierRecoursDTO.DossierRecoursUpdateRequest request) {
        
        DossierRecoursDTO.DossierRecoursResponse response = dossierRecoursService.updateDossier(id, request);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un dossier", 
               description = "Supprime définitivement un dossier de recours")
    public ResponseEntity<Map<String, Object>> deleteDossier(@PathVariable UUID id) {
        
        dossierRecoursService.deleteDossier(id);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Dossier supprimé avec succès");
        
        return ResponseEntity.ok(body);
    }

    @GetMapping("/statistiques/assureur/{assureurId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Statistiques par assureur", 
               description = "Retourne les statistiques des dossiers d'un assureur")
    public ResponseEntity<Map<String, Object>> getStatistiquesByAssureur(
            @PathVariable UUID assureurId) {
        
        Map<String, Object> stats = dossierRecoursService.getStatistiquesByAssureur(assureurId);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", stats);
        
        return ResponseEntity.ok(body);
    }

    @GetMapping("/statistiques/periode")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Statistiques par période", 
               description = "Retourne les statistiques des dossiers pour une période donnée")
    public ResponseEntity<Map<String, Object>> getStatistiquesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime endDate) {
        
        Map<String, Object> stats = 
            dossierRecoursService.getStatistiquesByPeriod(startDate, endDate);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", stats);
        
        return ResponseEntity.ok(body);
    }

    @GetMapping("/montant-eleve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Dossiers à montant élevé", 
               description = "Retourne les dossiers dont le montant dépasse un seuil")
    public ResponseEntity<Map<String, Object>> getDossiersAvecMontantEleve(
            @RequestParam Double montantSeuil) {
        
        List<DossierRecoursDTO.DossierRecoursResponse> dossiers = 
            dossierRecoursService.getDossiersAvecMontantEleve(montantSeuil);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", dossiers);
        body.put("count", dossiers.size());
        
        return ResponseEntity.ok(body);
    }
}