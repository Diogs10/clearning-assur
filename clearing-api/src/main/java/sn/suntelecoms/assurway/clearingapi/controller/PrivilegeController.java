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
import sn.suntelecoms.assurway.clearingapi.dto.PaginatedResponse;
import sn.suntelecoms.assurway.clearingapi.dto.PrivilegeDTO;
import sn.suntelecoms.assurway.clearingapi.service.PrivilegeService;
import sn.suntelecoms.assurway.clearingapi.util.ResponseUtil;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/privileges")
@RequiredArgsConstructor
@Tag(name = "Privileges", description = "API de gestion des privilèges")
@SecurityRequirement(name = "bearer-jwt")
class PrivilegeController {

    private final PrivilegeService privilegeService;

    /**
     * GET /api/privileges?max=10&offset=0&sort=ordre&order=asc - Lister les privilèges paginés
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Lister les privilèges paginés", description = "Récupère la liste paginée des privilèges")
    public ResponseEntity<ApiResponse<PaginatedResponse<PrivilegeDTO.PrivilegeResponse>>> getAllPrivileges(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "ordre") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        log.info("API: Liste des privilèges (max={}, offset={})", max, offset);
        Page<PrivilegeDTO.PrivilegeResponse> privileges = privilegeService.getAllPrivileges(max, offset, sort, order);
        return ResponseUtil.successPaginated(privileges, "Liste des privilèges récupérée avec succès");
    }

    /**
     * GET /api/privileges/liste-privileges - Lister tous les privilèges (hiérarchique)
     */
    @GetMapping("/liste-privileges")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Lister tous les privilèges (hiérarchique)", description = "Récupère tous les privilèges avec leur hiérarchie parent-enfant")
    public ResponseEntity<ApiResponse<List<PrivilegeDTO.PrivilegeResponse>>> getAllPrivilegesHierarchical() {
        log.info("API: Liste de tous les privilèges avec hiérarchie");
        List<PrivilegeDTO.PrivilegeResponse> privileges = privilegeService.getAllPrivilegesHierarchical();
        return ResponseUtil.success(privileges);
    }

    /**
     * POST /api/privileges - Créer un privilège
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un privilège", description = "Crée un nouveau privilège")
    public ResponseEntity<ApiResponse<PrivilegeDTO.PrivilegeResponse>> createPrivilege(
            @Valid @RequestBody PrivilegeDTO.CreatePrivilegeRequest request) {
        log.info("API: Création d'un privilège");
        PrivilegeDTO.PrivilegeResponse privilege = privilegeService.createPrivilege(request);
        return ResponseUtil.created(privilege, "Privilège créé avec succès");
    }

    /**
     * GET /api/privileges/{id} - Récupérer un privilège par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Récupérer un privilège", description = "Récupère les détails d'un privilège")
    public ResponseEntity<ApiResponse<PrivilegeDTO.PrivilegeResponse>> getPrivilegeById(@PathVariable UUID id) {
        log.info("API: Récupération du privilège ID: {}", id);
        PrivilegeDTO.PrivilegeResponse privilege = privilegeService.getPrivilegeById(id);
        return ResponseUtil.success(privilege);
    }

    /**
     * PUT /api/privileges/{id} - Mettre à jour un privilège
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un privilège", description = "Met à jour les informations d'un privilège")
    public ResponseEntity<ApiResponse<PrivilegeDTO.PrivilegeResponse>> updatePrivilege(
            @PathVariable UUID id,
            @Valid @RequestBody PrivilegeDTO.UpdatePrivilegeRequest request) {
        log.info("API: Mise à jour du privilège ID: {}", id);
        PrivilegeDTO.PrivilegeResponse privilege = privilegeService.updatePrivilege(id, request);
        return ResponseUtil.success(privilege, "Privilège mis à jour avec succès");
    }

    /**
     * DELETE /api/privileges/{id} - Supprimer un privilège
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un privilège", description = "Supprime un privilège du système")
    public ResponseEntity<ApiResponse<Void>> deletePrivilege(@PathVariable UUID id) {
        log.info("API: Suppression du privilège ID: {}", id);
        privilegeService.deletePrivilege(id);
        return ResponseUtil.success(null, "Privilège supprimé avec succès");
    }
}