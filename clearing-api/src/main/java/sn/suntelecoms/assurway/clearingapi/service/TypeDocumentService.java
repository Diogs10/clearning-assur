package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.suntelecoms.assurway.clearingapi.dto.TypeDocumentDTO;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceAlreadyExistsException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.model.TypeDocument;
import sn.suntelecoms.assurway.clearingapi.repository.TypeDocumentRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TypeDocumentService {

    private final TypeDocumentRepository typeDocumentRepository;

    @Transactional
    public TypeDocumentDTO.TypeDocumentResponse createTypeDocument(TypeDocumentDTO.CreateTypeDocumentRequest request) {
        if (typeDocumentRepository.existsByLibelle(request.getLibelle())) {
            throw new ResourceAlreadyExistsException("Type Document", "libelle", request.getLibelle());
        }

        TypeDocument TypeDocument = new TypeDocument();
        TypeDocument.setLibelle(request.getLibelle());
        TypeDocument.setCreatedAt(TypeDocument.getCreatedAt());

        TypeDocument saved = typeDocumentRepository.save(TypeDocument);
        return mapToDTO(saved);
    }

    public TypeDocumentDTO.TypeDocumentResponse getTypeDocumentById(UUID id) {
        TypeDocument TypeDocument = typeDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type Document", "id", id));
        return mapToDTO(TypeDocument);
    }

    public Page<TypeDocumentDTO.TypeDocumentResponse> getAllTypeDocumentsPaginated(int page, int size) {
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, size);
        Page<TypeDocument> TypeDocuments = typeDocumentRepository.findAll(pageable);
        return TypeDocuments.map(this::mapToDTO);
    }

    public List<TypeDocumentDTO.TypeDocumentResponse> getAllTypeDocuments() {
        List<TypeDocument> TypeDocuments = typeDocumentRepository.findAll();
        return TypeDocuments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TypeDocumentDTO.TypeDocumentResponse updateTypeDocument(UUID id, TypeDocumentDTO.UpdateTypeDocumentRequest request) {
        TypeDocument TypeDocument = typeDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type Document", "id", id));

        if (request.getLibelle() != null && !request.getLibelle().equals(TypeDocument.getLibelle())) {
            if (typeDocumentRepository.existsByLibelle(request.getLibelle())) {
                throw new ResourceAlreadyExistsException("Type Document", "libelle", request.getLibelle());
            }
            TypeDocument.setLibelle(request.getLibelle());
        }

        TypeDocument updated = typeDocumentRepository.save(TypeDocument);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteTypeDocument(UUID id) {
        TypeDocument TypeDocument = typeDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type Document", "id", id));
        typeDocumentRepository.delete(TypeDocument);
    }

    private TypeDocumentDTO.TypeDocumentResponse mapToDTO(TypeDocument TypeDocument) {
        TypeDocumentDTO.TypeDocumentResponse dto = new TypeDocumentDTO.TypeDocumentResponse();
        dto.setId(TypeDocument.getId());
        dto.setLibelle(TypeDocument.getLibelle());
        dto.setCreatedAt(TypeDocument.getCreatedAt());
        return dto;
    }
}
