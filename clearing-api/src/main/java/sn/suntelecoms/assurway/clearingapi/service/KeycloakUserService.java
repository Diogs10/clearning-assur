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
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

/**
 * Service pour gérer les utilisateurs dans Keycloak
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    /**
     * Créer un nouvel utilisateur dans Keycloak
     */
    public String createUser(String username, String email, String firstName, 
                           String lastName, String password, String... roles) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Créer la représentation de l'utilisateur
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Créer l'utilisateur
            Response response = usersResource.create(user);
            
            if (response.getStatus() == 201) {
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                log.info("Utilisateur créé avec succès: {}, ID: {}", username, userId);

                // Définir le mot de passe
                UserResource userResource = usersResource.get(userId);
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(password);
                credential.setTemporary(false);
                userResource.resetPassword(credential);

                // Assigner les rôles
                if (roles != null && roles.length > 0) {
                    assignRoles(userId, roles);
                }

                return userId;
            } else {
                log.error("Échec de la création de l'utilisateur: {}", response.getStatusInfo());
                throw new RuntimeException("Échec de la création de l'utilisateur: " + response.getStatusInfo());
            }
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'utilisateur: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la création de l'utilisateur", e);
        }
    }

    /**
     * Assigner des rôles à un utilisateur
     */
    public void assignRoles(String userId, String... roleNames) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

            for (String roleName : roleNames) {
                RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                userResource.roles().realmLevel().add(Collections.singletonList(role));
                log.info("Rôle '{}' assigné à l'utilisateur ID: {}", roleName, userId);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation des rôles: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'assignation des rôles", e);
        }
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<UserRepresentation> getAllUsers() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            return realmResource.users().list();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs", e);
        }
    }

    /**
     * Récupérer un utilisateur par son ID
     */
    public UserRepresentation getUserById(String userId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            return realmResource.users().get(userId).toRepresentation();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération de l'utilisateur", e);
        }
    }

    /**
     * Supprimer un utilisateur
     */
    public void deleteUser(String userId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            realmResource.users().get(userId).remove();
            log.info("Utilisateur supprimé: {}", userId);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur", e);
        }
    }

    /**
     * Mettre à jour un utilisateur
     */
    public void updateUser(String userId, String email, String firstName, String lastName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            if (email != null) user.setEmail(email);
            if (firstName != null) user.setFirstName(firstName);
            if (lastName != null) user.setLastName(lastName);

            userResource.update(user);
            log.info("Utilisateur mis à jour: {}", userId);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur", e);
        }
    }

    /**
     * Réinitialiser le mot de passe d'un utilisateur
     */
    public void resetPassword(String userId, String newPassword) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            userResource.resetPassword(credential);
            log.info("Mot de passe réinitialisé pour l'utilisateur: {}", userId);
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation du mot de passe: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la réinitialisation du mot de passe", e);
        }
    }
}