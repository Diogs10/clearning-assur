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
import sn.suntelecoms.assurway.clearingapi.dto.GarantieDTO;
import sn.suntelecoms.assurway.clearingapi.dto.PaginatedResponse;
import sn.suntelecoms.assurway.clearingapi.service.GarantieService;
import sn.suntelecoms.assurway.clearingapi.util.ResponseUtil;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/garanties")
@RequiredArgsConstructor
@Tag(name = "Garanties", description = "API de gestion des garanties")
@SecurityRequirement(name = "bearer-jwt")
public class GarantieController {
    private final GarantieService garantieService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une garantie", description = "Crée une nouvelle garantie dans le système")
    public ResponseEntity<ApiResponse<GarantieDTO.GarantieResponse>> createGarantie(
            @Valid @RequestBody GarantieDTO.CreateGarantieRequest request) {
        GarantieDTO.GarantieResponse garantie = garantieService.createGarantie(request);
        return ResponseUtil.created(garantie, "Garantie créé avec succès");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Récupérer une garantie par ID", description = "Récupère les détails d'une garantie")
    public ResponseEntity<ApiResponse<GarantieDTO.GarantieResponse>> getGarantieById(@PathVariable UUID id) {
        GarantieDTO.GarantieResponse garantie = garantieService.getGarantieById(id);
        return ResponseUtil.success(garantie);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les garanties paginées", description = "Récupère la liste paginée des garanties")
    public ResponseEntity<ApiResponse<PaginatedResponse<GarantieDTO.GarantieResponse>>> getAllGaranties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GarantieDTO.GarantieResponse> garanties = garantieService.getAllGarantiesPaginated(page, size);
        return ResponseUtil.successPaginated(garanties, "Liste des garanties récupérée avec succès");
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Lister tous les garanties (sans pagination)", description = "Récupère la liste complète de tous les garanties")
    public ResponseEntity<ApiResponse<List<GarantieDTO.GarantieResponse>>> getAllGarantiesAsList() {
        List<GarantieDTO.GarantieResponse> garanties = garantieService.getAllGaranties();
        return ResponseUtil.success(garanties, "Liste complète des garanties récupérée avec succès");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour une garantie", description = "Met à jour les informations d'une garantie")
    public ResponseEntity<ApiResponse<GarantieDTO.GarantieResponse>> updateGarantie(
            @PathVariable UUID id,
            @Valid @RequestBody GarantieDTO.UpdateGarantieRequest request) {
        GarantieDTO.GarantieResponse garantie = garantieService.updateGarantie(id, request);
        return ResponseUtil.success(garantie, "Garantie mise à jour avec succès");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une garantie", description = "Supprime une garantie du système")
    public ResponseEntity<ApiResponse<Void>> deleteGarantie(@PathVariable UUID id) {
        garantieService.deleteGarantie(id);
        return ResponseUtil.success(null, "Garantie supprimée avec succès");
    }
}
