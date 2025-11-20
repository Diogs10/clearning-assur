package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakRoleManagementService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class RoleAlreadyExistsException extends RuntimeException {
        public RoleAlreadyExistsException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class RoleNotFoundException extends RuntimeException {
        public RoleNotFoundException(String message) {
            super(message);
        }
    }

    public static class KeycloakRoleException extends RuntimeException {
        public KeycloakRoleException(String message) {
            super(message);
        }
        public KeycloakRoleException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public RoleRepresentation createRole(String roleName, String description) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du rôle est requis.");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            RolesResource rolesResource = realmResource.roles();

            if (roleExists(roleName)) {
                throw new RoleAlreadyExistsException("Le rôle '" + roleName + "' existe déjà.");
            }

            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            role.setDescription(description);

            rolesResource.create(role);

            return rolesResource.get(roleName).toRepresentation();

        } catch (RoleAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new KeycloakRoleException("Erreur lors de la création du rôle.", e);
        }
    }


    public void updateRole(String roleName, String newDescription) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du rôle est requis.");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            RoleResource roleResource = realmResource.roles().get(roleName);
            
            RoleRepresentation role = roleResource.toRepresentation();
            role.setDescription(newDescription);
            
            roleResource.update(role);

        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new RoleNotFoundException("Le rôle '" + roleName + "' n'existe pas.");
        } catch (Exception e) {
            throw new KeycloakRoleException("Erreur lors de la mise à jour du rôle.", e);
        }
    }

    public void deleteRole(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du rôle est requis.");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            realmResource.roles().deleteRole(roleName);

        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new RoleNotFoundException("Le rôle '" + roleName + "' n'existe pas.");
        } catch (Exception e) {
            throw new KeycloakRoleException("Erreur lors de la suppression du rôle.", e);
        }
    }

    public boolean roleExists(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return false;
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            realmResource.roles().get(roleName).toRepresentation();
            return true;
        } catch (jakarta.ws.rs.NotFoundException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}