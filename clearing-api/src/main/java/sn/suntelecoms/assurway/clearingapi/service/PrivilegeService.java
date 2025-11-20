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

    @Transactional
    public PrivilegeDTO.PrivilegeResponse createPrivilege(PrivilegeDTO.CreatePrivilegeRequest request) {

        if (privilegeRepository.existsByCode(request.getCode())) {
            throw new ResourceAlreadyExistsException("Privilège", "code", request.getCode());
        }

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

        return mapToPrivilegeResponse(savedPrivilege);
    }

    public Page<PrivilegeDTO.PrivilegeResponse> getAllPrivileges(int page, int size, String sort, String order) {
        int safePage = Math.max(0, page); 
        int safeSize = Math.max(1, size);
        Sort sortBy = order.equalsIgnoreCase("desc") 
            ? Sort.by(sort).descending() 
            : Sort.by(sort).ascending();

        Pageable pageable = PageRequest.of(safePage, safeSize, sortBy);
        Page<Privilege> privileges = privilegeRepository.findAll(pageable);

        return privileges.map(this::mapToPrivilegeResponse);
    }

    public List<PrivilegeDTO.PrivilegeResponse> getAllPrivilegesAsList() {
        List<Privilege> privileges = privilegeRepository.findAll();
        return privileges.stream()
                .map(this::mapToPrivilegeResponse)
                .collect(Collectors.toList());
    }

    public List<PrivilegeDTO.PrivilegeResponse> getAllRootPrivilegesHierarchical() {
        List<Privilege> rootPrivileges = privilegeRepository.findByParentIdIsNull();
        
        return rootPrivileges.stream()
                .map(this::mapToPrivilegeResponseWithChildren)
                .collect(Collectors.toList());
    }

    public PrivilegeDTO.PrivilegeResponse getPrivilegeById(UUID id) {
        Privilege privilege = privilegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Privilège", "id", id));
        return mapToPrivilegeResponseWithChildren(privilege);
    }

    @Transactional
    public PrivilegeDTO.PrivilegeResponse updatePrivilege(UUID id, PrivilegeDTO.UpdatePrivilegeRequest request) {

        Privilege privilege = privilegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Privilège", "id", id));

        if (request.getLibelle() != null) privilege.setLibelle(request.getLibelle());
        if (request.getLien() != null) privilege.setLien(request.getLien());
        if (request.getIcon() != null) privilege.setIcon(request.getIcon());
        if (request.getIsMenu() != null) privilege.setIsMenu(request.getIsMenu());
        if (request.getOrdre() != null) privilege.setOrdre(request.getOrdre());

        Privilege updatedPrivilege = privilegeRepository.save(privilege);

        return mapToPrivilegeResponse(updatedPrivilege);
    }

    @Transactional
    public void deletePrivilege(UUID id) {
        Privilege privilege = privilegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Privilège", "id", id));

        if (!privilege.getChildren().isEmpty()) {
            throw new BusinessException("Impossible de supprimer un privilège qui a des enfants");
        }

        privilegeRepository.deleteById(id);
    }

    public List<PrivilegeDTO.PrivilegeResponse> getMenuPrivileges() {
        List<Privilege> menuPrivileges = privilegeRepository.findAllMenuItems();
        return menuPrivileges.stream()
                .map(this::mapToPrivilegeResponse)
                .collect(Collectors.toList());
    }

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