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
@RequestMapping("/privileges")
@RequiredArgsConstructor
@Tag(name = "Privileges", description = "API de gestion des privilèges")
@SecurityRequirement(name = "bearer-jwt")
class PrivilegeController {

    private final PrivilegeService privilegeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Lister les privilèges paginés", description = "Récupère la liste paginée des privilèges")
    public ResponseEntity<ApiResponse<PaginatedResponse<PrivilegeDTO.PrivilegeResponse>>> getAllPrivileges(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "ordre") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Page<PrivilegeDTO.PrivilegeResponse> privileges = privilegeService.getAllPrivileges(size, page, sort, order);
        return ResponseUtil.successPaginated(privileges, "Liste des privilèges récupérée avec succès");
    }

    @GetMapping("/liste-privileges")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Lister tous les privilèges (hiérarchique)", description = "Récupère tous les privilèges avec leur hiérarchie parent-enfant")
    public ResponseEntity<ApiResponse<List<PrivilegeDTO.PrivilegeResponse>>> getAllPrivilegesHierarchical() {
        List<PrivilegeDTO.PrivilegeResponse> privileges = privilegeService.getAllPrivilegesHierarchical();
        return ResponseUtil.success(privileges);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un privilège", description = "Crée un nouveau privilège")
    public ResponseEntity<ApiResponse<PrivilegeDTO.PrivilegeResponse>> createPrivilege(
            @Valid @RequestBody PrivilegeDTO.CreatePrivilegeRequest request) {
        PrivilegeDTO.PrivilegeResponse privilege = privilegeService.createPrivilege(request);
        return ResponseUtil.created(privilege, "Privilège créé avec succès");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Récupérer un privilège", description = "Récupère les détails d'un privilège")
    public ResponseEntity<ApiResponse<PrivilegeDTO.PrivilegeResponse>> getPrivilegeById(@PathVariable UUID id) {
        PrivilegeDTO.PrivilegeResponse privilege = privilegeService.getPrivilegeById(id);
        return ResponseUtil.success(privilege);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un privilège", description = "Met à jour les informations d'un privilège")
    public ResponseEntity<ApiResponse<PrivilegeDTO.PrivilegeResponse>> updatePrivilege(
            @PathVariable UUID id,
            @Valid @RequestBody PrivilegeDTO.UpdatePrivilegeRequest request) {
        PrivilegeDTO.PrivilegeResponse privilege = privilegeService.updatePrivilege(id, request);
        return ResponseUtil.success(privilege, "Privilège mis à jour avec succès");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un privilège", description = "Supprime un privilège du système")
    public ResponseEntity<ApiResponse<Void>> deletePrivilege(@PathVariable UUID id) {
        privilegeService.deletePrivilege(id);
        return ResponseUtil.success(null, "Privilège supprimé avec succès");
    }
}