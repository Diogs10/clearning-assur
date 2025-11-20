package sn.suntelecoms.assurway.clearingapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
// Renommage pour refléter une meilleure gestion complète des utilisateurs
public class KeycloakUserManagementService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    // --- Exceptions Simples pour la Démonstration ---
    // En production, ces exceptions devraient être définies dans un package dédié.
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class KeycloakAdminException extends RuntimeException {
        public KeycloakAdminException(String message) {
            super(message);
        }
        public KeycloakAdminException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    // ------------------------------------------------

    /**
     * Crée un nouvel utilisateur dans Keycloak, lui attribue un mot de passe et des rôles.
     * @param username Le nom d'utilisateur (requis).
     * @param email L'adresse email (requise).
     * @param firstName Le prénom.
     * @param lastName Le nom de famille.
     * @param password Le mot de passe initial.
     * @param roles Les rôles à assigner au niveau du realm.
     * @return L'ID unique de l'utilisateur créé dans Keycloak.
     */
    public String createUser(String username, String email, String firstName,
                             String lastName, String password, String... roles) {
        
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        try (Response response = usersResource.create(user)) {
            int status = response.getStatus();

            if (status == 201) {
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                log.info("Utilisateur créé dans Keycloak avec succès: ID='{}', Username='{}'", userId, username);

                UserResource userResource = usersResource.get(userId);
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(password);
                credential.setTemporary(false);
                userResource.resetPassword(credential);

                if (roles != null && roles.length > 0) {
                    assignRoles(userId, roles);
                }

                return userId;
            } else if (status == 409) {
                log.warn("Tentative de création d'un utilisateur existant: {}", username);
                throw new UserAlreadyExistsException("Un utilisateur avec cet email ou nom d'utilisateur existe déjà.");
            } else {
                throw new KeycloakAdminException("Échec de la création de l'utilisateur (Code: " + status + "): " + response.getStatusInfo());
            }
        } catch (UserAlreadyExistsException | KeycloakAdminException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la création de l'utilisateur", e);
            throw new KeycloakAdminException("Erreur lors de la création de l'utilisateur.", e);
        }
    }

    public void assignRoles(String userId, String... roleNames) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

            for (String roleName : roleNames) {
                RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                userResource.roles().realmLevel().add(Collections.singletonList(role));
            }
        } catch (jakarta.ws.rs.NotFoundException e) {
             log.warn("Tentative d'assignation de rôle à l'utilisateur {} avec un rôle inexistant.", userId, e);
             throw new KeycloakAdminException("Erreur: L'utilisateur ou un des rôles spécifiés est introuvable.", e);
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation des rôles à l'utilisateur {}", userId, e);
            throw new KeycloakAdminException("Erreur lors de l'assignation des rôles.", e);
        }
    }

    public List<UserRepresentation> getAllUsers() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            return realmResource.users().list();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la liste des utilisateurs", e);
            throw new KeycloakAdminException("Erreur lors de la récupération des utilisateurs.", e);
        }
    }

    public UserRepresentation getUserById(String userId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            return realmResource.users().get(userId).toRepresentation();
        } catch (jakarta.ws.rs.NotFoundException e) {
             log.warn("Utilisateur introuvable pour l'ID: {}", userId);
             throw new KeycloakAdminException("Utilisateur introuvable.", e);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur {}", userId, e);
            throw new KeycloakAdminException("Erreur lors de la récupération de l'utilisateur.", e);
        }
    }

    public void deleteUser(String userId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            realmResource.users().get(userId).remove();
        }  catch (jakarta.ws.rs.NotFoundException e) {
             log.warn("Tentative de suppression d'un utilisateur introuvable: {}", userId);
             throw new KeycloakAdminException("Utilisateur introuvable pour la suppression.", e);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur {}", userId, e);
            throw new KeycloakAdminException("Erreur lors de la suppression de l'utilisateur.", e);
        }
    }

    public void updateUser(String userId, String email, String firstName, String lastName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            // Mise à jour conditionnelle des champs
            if (email != null) user.setEmail(email);
            if (firstName != null) user.setFirstName(firstName);
            if (lastName != null) user.setLastName(lastName);

            userResource.update(user);
        } catch (jakarta.ws.rs.NotFoundException e) {
             log.warn("Tentative de mise à jour d'un utilisateur introuvable: {}", userId);
             throw new KeycloakAdminException("Utilisateur introuvable pour la mise à jour.", e);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur {}", userId, e);
            throw new KeycloakAdminException("Erreur lors de la mise à jour de l'utilisateur.", e);
        }
    }

    public void resetPassword(String userId, String newPassword) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            userResource.resetPassword(credential);
        } catch (jakarta.ws.rs.NotFoundException e) {
             log.warn("Tentative de réinitialisation de mot de passe pour un utilisateur introuvable: {}", userId);
             throw new KeycloakAdminException("Utilisateur introuvable pour la réinitialisation du mot de passe.", e);
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation du mot de passe de l'utilisateur {}", userId, e);
            throw new KeycloakAdminException("Erreur lors de la réinitialisation du mot de passe.", e);
        }
    }
}