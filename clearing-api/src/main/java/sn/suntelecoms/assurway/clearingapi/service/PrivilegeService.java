package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.suntelecoms.assurway.clearingapi.dto.PrivilegeDTO;
import sn.suntelecoms.assurway.clearingapi.exception.BusinessException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceAlreadyExistsException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.model.Privilege;
import sn.suntelecoms.assurway.clearingapi.repository.PrivilegeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivilegeService {

    private final PrivilegeRepository privilegeRepository;

    /**
     * Créer un nouveau privilège
     */
    @Transactional
    public PrivilegeDTO.PrivilegeResponse createPrivilege(PrivilegeDTO.CreatePrivilegeRequest request) {
        log.info("Création d'un privilège: {}", request.getCode());

        if (privilegeRepository.existsByCode(request.getCode())) {
            throw new ResourceAlreadyExistsException("Privilège", "code", request.getCode());
        }

        // Vérifier que le parent existe si spécifié
        if (request.getParentId() != null) {
            privilegeRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Privilège parent", "id", request.getParentId()));
        }

        Privilege privilege = new Privilege();
        privilege.setCode(request.getCode());
        privilege.setLibelle(request.getLibelle());
        privilege.setNiveau(request.getNiveau());
        privilege.setLien(request.getLien());
        privilege.setIcon(request.getIcon());
        privilege.setIsMenu(request.getIsMenu());
        privilege.setParentId(request.getParentId());
        privilege.setOrdre(request.getOrdre());

        Privilege savedPrivilege = privilegeRepository.save(privilege);
        log.info("Privilège créé avec succès: {}", savedPrivilege.getCode());

        return mapToPrivilegeResponse(savedPrivilege);
    }

    /**
     * Lister tous les privilèges avec pagination
     */
    public Page<PrivilegeDTO.PrivilegeResponse> getAllPrivileges(int max, int offset, String sort, String order) {
        log.info("Récupération des privilèges (max={}, offset={}, sort={}, order={})", max, offset, sort, order);

        Sort sortBy = order.equalsIgnoreCase("desc") 
            ? Sort.by(sort).descending() 
            : Sort.by(sort).ascending();

        Pageable pageable = PageRequest.of(offset / max, max, sortBy);
        Page<Privilege> privileges = privilegeRepository.findAll(pageable);

        return privileges.map(this::mapToPrivilegeResponse);
    }

    /**
     * Lister tous les privilèges avec hiérarchie
     */
    public List<PrivilegeDTO.PrivilegeResponse> getAllPrivilegesHierarchical() {
        log.info("Récupération de tous les privilèges avec hiérarchie");
        List<Privilege> rootPrivileges = privilegeRepository.findByParentIdIsNull();
        
        return rootPrivileges.stream()
                .map(this::mapToPrivilegeResponseWithChildren)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un privilège par ID
     */
    public PrivilegeDTO.PrivilegeResponse getPrivilegeById(UUID id) {
        log.info("Récupération du privilège ID: {}", id);
        Privilege privilege = privilegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Privilège", "id", id));
        return mapToPrivilegeResponseWithChildren(privilege);
    }

    /**
     * Mettre à jour un privilège
     */
    @Transactional
    public PrivilegeDTO.PrivilegeResponse updatePrivilege(UUID id, PrivilegeDTO.UpdatePrivilegeRequest request) {
        log.info("Mise à jour du privilège ID: {}", id);

        Privilege privilege = privilegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Privilège", "id", id));

        if (request.getLibelle() != null) privilege.setLibelle(request.getLibelle());
        if (request.getLien() != null) privilege.setLien(request.getLien());
        if (request.getIcon() != null) privilege.setIcon(request.getIcon());
        if (request.getIsMenu() != null) privilege.setIsMenu(request.getIsMenu());
        if (request.getOrdre() != null) privilege.setOrdre(request.getOrdre());

        Privilege updatedPrivilege = privilegeRepository.save(privilege);
        log.info("Privilège mis à jour avec succès: {}", updatedPrivilege.getCode());

        return mapToPrivilegeResponse(updatedPrivilege);
    }

    /**
     * Supprimer un privilège
     */
    @Transactional
    public void deletePrivilege(UUID id) {
        log.info("Suppression du privilège ID: {}", id);
        Privilege privilege = privilegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Privilège", "id", id));

        // Vérifier qu'il n'a pas d'enfants
        if (!privilege.getChildren().isEmpty()) {
            throw new BusinessException("Impossible de supprimer un privilège qui a des enfants");
        }

        privilegeRepository.deleteById(id);
        log.info("Privilège supprimé avec succès");
    }

    /**
     * Récupérer tous les privilèges de type menu
     */
    public List<PrivilegeDTO.PrivilegeResponse> getMenuPrivileges() {
        log.info("Récupération des privilèges de type menu");
        List<Privilege> menuPrivileges = privilegeRepository.findAllMenuItems();
        return menuPrivileges.stream()
                .map(this::mapToPrivilegeResponse)
                .collect(Collectors.toList());
    }

    // Méthodes utilitaires de mapping
    private PrivilegeDTO.PrivilegeResponse mapToPrivilegeResponse(Privilege privilege) {
        PrivilegeDTO.PrivilegeResponse response = new PrivilegeDTO.PrivilegeResponse();
        response.setId(privilege.getId());
        response.setCode(privilege.getCode());
        response.setLibelle(privilege.getLibelle());
        response.setNiveau(privilege.getNiveau());
        response.setLien(privilege.getLien());
        response.setIcon(privilege.getIcon());
        response.setIsMenu(privilege.getIsMenu());
        response.setParentId(privilege.getParentId());
        response.setOrdre(privilege.getOrdre());
        response.setCreatedAt(privilege.getCreatedAt());
        return response;
    }

    private PrivilegeDTO.PrivilegeResponse mapToPrivilegeResponseWithChildren(Privilege privilege) {
        PrivilegeDTO.PrivilegeResponse response = mapToPrivilegeResponse(privilege);

        if (privilege.getChildren() != null && !privilege.getChildren().isEmpty()) {
            List<PrivilegeDTO.PrivilegeResponse> children = privilege.getChildren().stream()
                    .sorted((p1, p2) -> p1.getOrdre().compareTo(p2.getOrdre()))
                    .map(this::mapToPrivilegeResponseWithChildren)
                    .collect(Collectors.toList());
            response.setChildren(children);
        } else {
            response.setChildren(new ArrayList<>());
        }

        return response;
    }
}