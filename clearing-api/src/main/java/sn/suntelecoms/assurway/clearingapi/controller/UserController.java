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
import sn.suntelecoms.assurway.clearingapi.dto.UserDTO;
import sn.suntelecoms.assurway.clearingapi.service.UserService;
import sn.suntelecoms.assurway.clearingapi.util.ResponseUtil;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API de gestion des utilisateurs")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    /**
     * POST /api/users - Créer un utilisateur
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un utilisateur", description = "Crée un nouvel utilisateur dans le système")
    public ResponseEntity<ApiResponse<UserDTO.UserResponse>> createUser(
            @Valid @RequestBody UserDTO.CreateUserRequest request) {
        UserDTO.UserResponse user = userService.createUser(request);
        return ResponseUtil.created(user, "Utilisateur créé avec succès");
    }

    /**
     * POST /api/users/get-username - Récupérer les infos d'un utilisateur par username
     */
    @PostMapping("/get-username")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Récupérer un utilisateur", description = "Récupère les informations d'un utilisateur par son username")
    public ResponseEntity<ApiResponse<UserDTO.UserResponse>> getUserByUsername(
            @Valid @RequestBody UserDTO.GetUsernameRequest request) {
        UserDTO.UserResponse user = userService.getUserByUsername(request.getUsername());
        return ResponseUtil.success(user);
    }

    /**
     * GET /api/users?max=10&offset=0 - Lister les utilisateurs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les utilisateurs", description = "Récupère la liste paginée des utilisateurs")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserDTO.UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset) {
        Page<UserDTO.UserResponse> users = userService.getAllUsers(max, offset);
        return ResponseUtil.successPaginated(users, "Liste des utilisateurs récupérée avec succès");
    }

    /**
     * GET /api/users/{id} - Récupérer un utilisateur par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Récupérer un utilisateur par ID", description = "Récupère les détails d'un utilisateur")
    public ResponseEntity<ApiResponse<UserDTO.UserResponse>> getUserById(@PathVariable UUID id) {
        UserDTO.UserResponse user = userService.getUserById(id);
        return ResponseUtil.success(user);
    }

    /**
     * PUT /api/users/{id} - Mettre à jour un utilisateur
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un utilisateur", description = "Met à jour les informations d'un utilisateur")
    public ResponseEntity<ApiResponse<UserDTO.UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO.UpdateUserRequest request) {
        UserDTO.UserResponse user = userService.updateUser(id, request);
        return ResponseUtil.success(user, "Utilisateur mis à jour avec succès");
    }

    /**
     * DELETE /api/users/{id} - Supprimer un utilisateur
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur du système")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseUtil.success(null, "Utilisateur supprimé avec succès");
    }

    /**
     * POST /api/users/{id}/change-password - Changer le mot de passe
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Changer le mot de passe", description = "Change le mot de passe d'un utilisateur")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO.ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ResponseUtil.success(null, "Mot de passe changé avec succès");
    }

    /**
     * POST /api/users/{id}/assign-roles - Assigner des rôles à un utilisateur
     */
    @PostMapping("/{id}/assign-roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assigner des rôles", description = "Assigne des rôles à un utilisateur")
    public ResponseEntity<ApiResponse<UserDTO.UserResponse>> assignRoles(
            @PathVariable UUID id,
            @RequestBody List<String> roles) {
        UserDTO.UserResponse user = userService.assignRolesToUser(id, roles);
        return ResponseUtil.success(user, "Rôles attribués avec succès");
    }
}