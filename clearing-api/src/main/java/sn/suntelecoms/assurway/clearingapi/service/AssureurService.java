package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.suntelecoms.assurway.clearingapi.dto.AssureurDTO;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceAlreadyExistsException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.model.Assureur;
import sn.suntelecoms.assurway.clearingapi.repository.AssureurRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssureurService {

    private final AssureurRepository assureurRepository;

    @Transactional
    public AssureurDTO.AssureurResponse createAssureur(AssureurDTO.CreateAssureurRequest request) {
        if (assureurRepository.existsByNom(request.getNom())) {
            throw new ResourceAlreadyExistsException("Assureur", "nom", request.getNom());
        }
        if (request.getEmail() != null && assureurRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Assureur", "email", request.getEmail());
        }

        Assureur assureur = new Assureur();
        assureur.setNom(request.getNom());
        assureur.setAdresse(request.getAdresse());
        assureur.setTelephone(request.getTelephone());
        assureur.setEmail(request.getEmail());
        assureur.setLogo(request.getLogo());

        Assureur saved = assureurRepository.save(assureur);
        return mapToDTO(saved);
    }

    public AssureurDTO.AssureurResponse getAssureurById(UUID id) {
        Assureur assureur = assureurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assureur", "id", id));
        return mapToDTO(assureur);
    }

    public Page<AssureurDTO.AssureurResponse> getAllAssureursPaginated(int page, int size) {
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, size);
        Page<Assureur> assureurs = assureurRepository.findAll(pageable);
        return assureurs.map(this::mapToDTO);
    }

    public List<AssureurDTO.AssureurResponse> getAllAssureurs() {
        List<Assureur> assureurs = assureurRepository.findAll();
        return assureurs.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssureurDTO.AssureurResponse updateAssureur(UUID id, AssureurDTO.UpdateAssureurRequest request) {
        Assureur assureur = assureurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assureur", "id", id));

        if (request.getNom() != null && !request.getNom().equals(assureur.getNom())) {
            if (assureurRepository.existsByNom(request.getNom())) {
                throw new ResourceAlreadyExistsException("Assureur", "nom", request.getNom());
            }
            assureur.setNom(request.getNom());
        }

        if (request.getAdresse() != null) assureur.setAdresse(request.getAdresse());
        if (request.getTelephone() != null) assureur.setTelephone(request.getTelephone());
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(assureur.getEmail()) && assureurRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("Assureur", "email", request.getEmail());
            }
            assureur.setEmail(request.getEmail());
        }
        if (request.getLogo() != null) assureur.setLogo(request.getLogo());

        Assureur updated = assureurRepository.save(assureur);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteAssureur(UUID id) {
        Assureur assureur = assureurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assureur", "id", id));
        assureurRepository.delete(assureur);
    }

    private AssureurDTO.AssureurResponse mapToDTO(Assureur assureur) {
        AssureurDTO.AssureurResponse dto = new AssureurDTO.AssureurResponse();
        dto.setId(assureur.getId());
        dto.setNom(assureur.getNom());
        dto.setAdresse(assureur.getAdresse());
        dto.setTelephone(assureur.getTelephone());
        dto.setEmail(assureur.getEmail());
        dto.setLogo(assureur.getLogo());
        return dto;
    }
}
