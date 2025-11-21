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
import sn.suntelecoms.assurway.clearingapi.dto.TypeDocumentDTO;
import sn.suntelecoms.assurway.clearingapi.service.TypeDocumentService;
import sn.suntelecoms.assurway.clearingapi.dto.PaginatedResponse;
import sn.suntelecoms.assurway.clearingapi.util.ResponseUtil;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/type-documents")
@RequiredArgsConstructor
@Tag(name = "Type document", description = "API de gestion des types document")
@SecurityRequirement(name = "bearer-jwt")
public class TypeDocumentController {

    private final TypeDocumentService typeDocumentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un type document", description = "Cré un nouvel type document dans le système")
    public ResponseEntity<ApiResponse<TypeDocumentDTO.TypeDocumentResponse>> createTypeDocument(
            @Valid @RequestBody TypeDocumentDTO.CreateTypeDocumentRequest request) {
        TypeDocumentDTO.TypeDocumentResponse typeDocument = typeDocumentService.createTypeDocument(request);
        return ResponseUtil.created(typeDocument, "Type document cré avec succès");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Récupérer un type document par ID", description = "Récupère les détails d'un type document")
    public ResponseEntity<ApiResponse<TypeDocumentDTO.TypeDocumentResponse>> getTypeDocumentById(@PathVariable UUID id) {
        TypeDocumentDTO.TypeDocumentResponse typeDocument = typeDocumentService.getTypeDocumentById(id);
        return ResponseUtil.success(typeDocument);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les types document paginés", description = "Récupère la liste paginée des types document")
    public ResponseEntity<ApiResponse<PaginatedResponse<TypeDocumentDTO.TypeDocumentResponse>>> getAllTypeDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TypeDocumentDTO.TypeDocumentResponse> typeDocuments = typeDocumentService.getAllTypeDocumentsPaginated(page, size);
        return ResponseUtil.successPaginated(typeDocuments, "Liste des types document récupérée avec succès");
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Lister tous les types document (sans pagination)", description = "Récupère la liste complète de tous les types document")
    public ResponseEntity<ApiResponse<List<TypeDocumentDTO.TypeDocumentResponse>>> getAllTypeDocumentsAsList() {
        List<TypeDocumentDTO.TypeDocumentResponse> typeDocuments = typeDocumentService.getAllTypeDocuments();
        return ResponseUtil.success(typeDocuments, "Liste complète des types document récupérée avec succès");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un type document", description = "Met à jour les informations d'un type document")
    public ResponseEntity<ApiResponse<TypeDocumentDTO.TypeDocumentResponse>> updateTypeDocument(
            @PathVariable UUID id,
            @Valid @RequestBody TypeDocumentDTO.UpdateTypeDocumentRequest request) {
        TypeDocumentDTO.TypeDocumentResponse typeDocuments = typeDocumentService.updateTypeDocument(id, request);
        return ResponseUtil.success(typeDocuments, "Type document mis à jour avec succès");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un type document", description = "Supprime un type document du système")
    public ResponseEntity<ApiResponse<Void>> deleteTypeDocument(@PathVariable UUID id) {
        typeDocumentService.deleteTypeDocument(id);
        return ResponseUtil.success(null, "Type document supprimé avec succès");
    }
}
