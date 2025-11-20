package sn.suntelecoms.assurway.clearingapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.suntelecoms.assurway.clearingapi.dto.ApiResponse;
import sn.suntelecoms.assurway.clearingapi.dto.AssureurDTO;
import sn.suntelecoms.assurway.clearingapi.dto.PaginatedResponse;
import sn.suntelecoms.assurway.clearingapi.service.AssureurService;
import sn.suntelecoms.assurway.clearingapi.util.ResponseUtil;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/assureurs")
@RequiredArgsConstructor
@Tag(name = "Assureurs", description = "API de gestion des assureurs")
@SecurityRequirement(name = "bearer-jwt")
public class AssureurController {

    private final AssureurService assureurService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un assureur", description = "Crée un nouvel assureur dans le système")
    public ResponseEntity<ApiResponse<AssureurDTO.AssureurResponse>> createAssureur(
            @Valid @RequestBody AssureurDTO.CreateAssureurRequest request) {
        AssureurDTO.AssureurResponse assureur = assureurService.createAssureur(request);
        return ResponseUtil.created(assureur, "Assureur créé avec succès");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Récupérer un assureur par ID", description = "Récupère les détails d'un assureur")
    public ResponseEntity<ApiResponse<AssureurDTO.AssureurResponse>> getAssureurById(@PathVariable UUID id) {
        AssureurDTO.AssureurResponse assureur = assureurService.getAssureurById(id);
        return ResponseUtil.success(assureur);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les assureurs paginés", description = "Récupère la liste paginée des assureurs")
    public ResponseEntity<ApiResponse<PaginatedResponse<AssureurDTO.AssureurResponse>>> getAllAssureurs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AssureurDTO.AssureurResponse> assureurs = assureurService.getAllAssureursPaginated(page, size);
        return ResponseUtil.successPaginated(assureurs, "Liste des assureurs récupérée avec succès");
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Lister tous les assureurs (sans pagination)", description = "Récupère la liste complète de tous les assureurs")
    public ResponseEntity<ApiResponse<List<AssureurDTO.AssureurResponse>>> getAllAssureursAsList() {
        List<AssureurDTO.AssureurResponse> assureurs = assureurService.getAllAssureurs();
        return ResponseUtil.success(assureurs, "Liste complète des assureurs récupérée avec succès");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un assureur", description = "Met à jour les informations d'un assureur")
    public ResponseEntity<ApiResponse<AssureurDTO.AssureurResponse>> updateAssureur(
            @PathVariable UUID id,
            @Valid @RequestBody AssureurDTO.UpdateAssureurRequest request) {
        AssureurDTO.AssureurResponse assureur = assureurService.updateAssureur(id, request);
        return ResponseUtil.success(assureur, "Assureur mis à jour avec succès");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un assureur", description = "Supprime un assureur du système")
    public ResponseEntity<ApiResponse<Void>> deleteAssureur(@PathVariable UUID id) {
        assureurService.deleteAssureur(id);
        return ResponseUtil.success(null, "Assureur supprimé avec succès");
    }
}
