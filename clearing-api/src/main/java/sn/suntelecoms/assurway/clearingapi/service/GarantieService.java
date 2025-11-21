package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.suntelecoms.assurway.clearingapi.dto.GarantieDTO;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceAlreadyExistsException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.model.Garantie;
import sn.suntelecoms.assurway.clearingapi.repository.GarantieRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GarantieService {

    private final GarantieRepository garantieRepository;

    @Transactional
    public GarantieDTO.GarantieResponse createGarantie(GarantieDTO.CreateGarantieRequest request) {
        if (garantieRepository.existsByLibelle(request.getLibelle())) {
            throw new ResourceAlreadyExistsException("Garantie", "libelle", request.getLibelle());
        }

        Garantie Garantie = new Garantie();
        Garantie.setLibelle(request.getLibelle());
        Garantie.setCreatedAt(Garantie.getCreatedAt());

        Garantie saved = garantieRepository.save(Garantie);
        return mapToDTO(saved);
    }

    public GarantieDTO.GarantieResponse getGarantieById(UUID id) {
        Garantie Garantie = garantieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Garantie", "id", id));
        return mapToDTO(Garantie);
    }

    public Page<GarantieDTO.GarantieResponse> getAllGarantiesPaginated(int page, int size) {
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, size);
        Page<Garantie> Garanties = garantieRepository.findAll(pageable);
        return Garanties.map(this::mapToDTO);
    }

    public List<GarantieDTO.GarantieResponse> getAllGaranties() {
        List<Garantie> Garanties = garantieRepository.findAll();
        return Garanties.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public GarantieDTO.GarantieResponse updateGarantie(UUID id, GarantieDTO.UpdateGarantieRequest request) {
        Garantie Garantie = garantieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Garantie", "id", id));

        if (request.getLibelle() != null && !request.getLibelle().equals(Garantie.getLibelle())) {
            if (garantieRepository.existsByLibelle(request.getLibelle())) {
                throw new ResourceAlreadyExistsException("Garantie", "libelle", request.getLibelle());
            }
            Garantie.setLibelle(request.getLibelle());
        }

        Garantie updated = garantieRepository.save(Garantie);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteGarantie(UUID id) {
        Garantie Garantie = garantieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Garantie", "id", id));
        garantieRepository.delete(Garantie);
    }

    private GarantieDTO.GarantieResponse mapToDTO(Garantie Garantie) {
        GarantieDTO.GarantieResponse dto = new GarantieDTO.GarantieResponse();
        dto.setId(Garantie.getId());
        dto.setLibelle(Garantie.getLibelle());
        dto.setCreatedAt(Garantie.getCreatedAt());
        return dto;
    }
}

