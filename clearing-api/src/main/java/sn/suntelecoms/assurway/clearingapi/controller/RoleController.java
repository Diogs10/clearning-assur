package sn.suntelecoms.assurway.clearingapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.suntelecoms.assurway.clearingapi.dto.ApiResponse;
import sn.suntelecoms.assurway.clearingapi.dto.PrivilegeDTO;
import sn.suntelecoms.assurway.clearingapi.dto.RoleDTO;
import sn.suntelecoms.assurway.clearingapi.service.RoleService;
import sn.suntelecoms.assurway.clearingapi.util.ResponseUtil;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "API de gestion des rôles")
@SecurityRequirement(name = "bearer-jwt")
public class RoleController {

    private final RoleService roleService;

    /**
     * GET /api/roles - Lister tous les rôles
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Lister tous les rôles", description = "Récupère la liste de tous les rôles")
    public ResponseEntity<ApiResponse<List<RoleDTO.RoleResponse>>> getAllRoles() {
        List<RoleDTO.RoleResponse> roles = roleService.getAllRoles();
        return ResponseUtil.success(roles, "Liste des rôles récupérée avec succès");
    }

    /**
     * POST /api/roles - Créer un rôle
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un rôle", description = "Crée un nouveau rôle")
    public ResponseEntity<ApiResponse<RoleDTO.RoleResponse>> createRole(
            @Valid @RequestBody RoleDTO.CreateRoleRequest request) {
        RoleDTO.RoleResponse role = roleService.createRole(request);
        return ResponseUtil.created(role, "Rôle créé avec succès");
    }

    /**
     * GET /api/roles/{id} - Récupérer un rôle par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Récupérer un rôle", description = "Récupère les détails d'un rôle avec ses privilèges")
    public ResponseEntity<ApiResponse<RoleDTO.RoleResponse>> getRoleById(@PathVariable UUID id) {
        RoleDTO.RoleResponse role = roleService.getRoleById(id);
        return ResponseUtil.success(role);
    }

    /**
     * POST /api/roles/get-privileges - Récupérer les privilèges d'un rôle
     */
    @PostMapping("/get-privileges")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Récupérer les privilèges d'un rôle", description = "Récupère tous les privilèges associés à un rôle")
    public ResponseEntity<ApiResponse<Set<PrivilegeDTO.PrivilegeResponse>>> getRolePrivileges(
            @Valid @RequestBody RoleDTO.GetRolePrivilegesRequest request) {
        Set<PrivilegeDTO.PrivilegeResponse> privileges = roleService.getRolePrivileges(request.getIdRole());
        return ResponseUtil.success(privileges);
    }

    /**
     * POST /api/roles/give-autorisations - Affecter des privilèges à un rôle
     */
    @PostMapping("/give-autorisations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Affecter les privilèges à un rôle", description = "Assigne une liste de privilèges à un rôle")
    public ResponseEntity<ApiResponse<RoleDTO.RoleResponse>> assignPrivilegesToRole(
            @Valid @RequestBody RoleDTO.AssignPrivilegesToRoleRequest request) {
        RoleDTO.RoleResponse role = roleService.assignPrivilegesToRole(request.getRole(), request.getPrivileges());
        return ResponseUtil.success(role, "Privilèges attribués avec succès");
    }

    /**
     * DELETE /api/roles/{id} - Supprimer un rôle
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un rôle", description = "Supprime un rôle du système")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseUtil.success(null, "Rôle supprimé avec succès");
    }
}