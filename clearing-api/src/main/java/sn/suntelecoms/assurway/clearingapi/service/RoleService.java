package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.suntelecoms.assurway.clearingapi.dto.PrivilegeDTO;
import sn.suntelecoms.assurway.clearingapi.dto.RoleDTO;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceAlreadyExistsException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.model.Privilege;
import sn.suntelecoms.assurway.clearingapi.model.Role;
import sn.suntelecoms.assurway.clearingapi.repository.PrivilegeRepository;
import sn.suntelecoms.assurway.clearingapi.repository.RoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final KeycloakRoleManagementService keycloakRoleService;

    @Value("${keycloak.roles.auto-sync:false}")
    private boolean autoSyncEnabled;

    @Transactional
    public RoleDTO.RoleResponse createRole(RoleDTO.CreateRoleRequest request) {

        if (roleRepository.existsByAuthority(request.getAuthority())) {
            throw new ResourceAlreadyExistsException("Rôle", "authority", request.getAuthority());
        }

        Role role = new Role();
        role.setAuthority(request.getAuthority());

        Role savedRole = roleRepository.save(role);
        if (autoSyncEnabled) {
            syncRoleToKeycloak(savedRole.getAuthority());
        }

        return mapToRoleResponse(savedRole);
    }

    public Page<RoleDTO.RoleResponse> getAllRoles(int page, int size) {
        int safePage = Math.max(0, page); 
        Pageable pageable = PageRequest.of(safePage, size);
        Page<Role> rolesPage = roleRepository.findAll(pageable);        
        return rolesPage.map(this::mapToRoleResponse);
    }

    public List<RoleDTO.RoleResponse> getAllRolesAsList() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    public RoleDTO.RoleResponse getRoleById(UUID id) {
        Role role = roleRepository.findByIdWithPrivileges(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", "id", id));
        return mapToRoleResponse(role);
    }

    public Set<PrivilegeDTO.PrivilegeResponse> getRolePrivileges(UUID roleId) {
        Role role = roleRepository.findByIdWithPrivileges(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", "id", roleId));

        return role.getPrivileges().stream()
                .map(this::mapToPrivilegeResponse)
                .collect(Collectors.toSet());
    }

    @Transactional
    public RoleDTO.RoleResponse assignPrivilegesToRole(String roleAuthority, List<UUID> privilegeIds) {

        Role role = roleRepository.findByAuthorityWithPrivileges(roleAuthority)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", "authority", roleAuthority));

        Set<Privilege> privileges = new HashSet<>();
        for (UUID privilegeId : privilegeIds) {
            Privilege privilege = privilegeRepository.findById(privilegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Privilège", "id", privilegeId));
            privileges.add(privilege);
        }

        role.setPrivileges(privileges);
        Role updatedRole = roleRepository.save(role);

        return mapToRoleResponse(updatedRole);
    }

    @Transactional
    public void deleteRole(UUID id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rôle", "id", id);
        }

        String authority = null;
        if (autoSyncEnabled) {
            Role role = roleRepository.findById(id).orElse(null);
            if (role != null) {
                authority = role.getAuthority();
            }
        }

        roleRepository.deleteById(id);
        if (autoSyncEnabled && authority != null) {
            deleteRoleFromKeycloak(authority);
        }
    }

    private void syncRoleToKeycloak(String authority) {
        try {
            if (keycloakRoleService.roleExists(authority)) {
                log.debug("Le rôle '{}' existe déjà dans Keycloak", authority);
            } else {
                String description = "Rôle synchronisé automatiquement: " + authority;
                keycloakRoleService.createRole(authority, description);
            }
        } catch (Exception e) {
            log.error("✗ Échec de synchronisation du rôle '{}' vers Keycloak: {}", 
                     authority, e.getMessage(), e);
        }
    }

    private void deleteRoleFromKeycloak(String authority) {
        try {
            if (keycloakRoleService.roleExists(authority)) {
                keycloakRoleService.deleteRole(authority);
            } else {
                log.debug("Le rôle '{}' n'existe pas dans Keycloak", authority);
            }
        } catch (Exception e) {
            log.error("✗ Échec de suppression du rôle '{}' de Keycloak: {}", 
                     authority, e.getMessage(), e);
        }
    }

    private RoleDTO.RoleResponse mapToRoleResponse(Role role) {
        RoleDTO.RoleResponse response = new RoleDTO.RoleResponse();
        response.setId(role.getId());
        response.setAuthority(role.getAuthority());
        response.setCreatedAt(role.getCreatedAt());

        if (role.getPrivileges() != null) {
            response.setPrivileges(role.getPrivileges().stream()
                    .map(this::mapToPrivilegeResponse)
                    .collect(Collectors.toSet()));
        }

        return response;
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
}