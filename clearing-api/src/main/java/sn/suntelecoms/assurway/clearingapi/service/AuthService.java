package sn.suntelecoms.assurway.clearingapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import sn.suntelecoms.assurway.clearingapi.dto.AuthDTO;
import sn.suntelecoms.assurway.clearingapi.dto.PrivilegeDTO;
import sn.suntelecoms.assurway.clearingapi.exception.BusinessException;
import sn.suntelecoms.assurway.clearingapi.exception.ResourceNotFoundException;
import sn.suntelecoms.assurway.clearingapi.model.Privilege;
import sn.suntelecoms.assurway.clearingapi.model.Role;
import sn.suntelecoms.assurway.clearingapi.model.User;
import sn.suntelecoms.assurway.clearingapi.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {

        try {
            String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", request.getUsername());
            body.add("password", request.getPassword());
            body.add("grant_type", "password");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            JsonNode tokenResponse = objectMapper.readTree(response.getBody());
            String accessToken = tokenResponse.get("access_token").asText();
            String refreshToken = tokenResponse.get("refresh_token").asText();
            int expiresIn = tokenResponse.get("expires_in").asInt();

            User user = userRepository.findByUsername(request.getUsername())
                    .or(() -> userRepository.findByEmail(request.getUsername()))
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", request.getUsername()));

            AuthDTO.LoginResponse loginResponse = new AuthDTO.LoginResponse();
            loginResponse.setAccess_token(accessToken);
            loginResponse.setRefresh_token(refreshToken);
            loginResponse.setToken_type("bearer");
            loginResponse.setExpires_in(expiresIn);
            loginResponse.setUuid(extractUuidFromToken(accessToken));
            loginResponse.setEmail(user.getEmail());
            loginResponse.setUsername(user.getUsername());
            loginResponse.setFirstName(user.getFirstName());
            loginResponse.setLastName(user.getLastName());
            loginResponse.setTelephone(user.getTelephone());
            loginResponse.setId(user.getId());

            List<String> roles = user.getRoles().stream()
                    .map(Role::getAuthority)
                    .collect(Collectors.toList());
            loginResponse.setRoles(roles);

            List<PrivilegeDTO.PrivilegeResponse> privileges = extractPrivilegesFromUser(user);
            loginResponse.setPrivileges(privileges);

            return loginResponse;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException("Identifiants invalides");
            }
            throw new BusinessException("Erreur lors de l'authentification: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de l'authentification: " + e.getMessage());
        }
    }

    public AuthDTO.RefreshTokenResponse refreshToken(AuthDTO.RefreshTokenRequest request) {

        try {
            String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", request.getRefresh_token());
            body.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            // Parser la réponse
            JsonNode tokenResponse = objectMapper.readTree(response.getBody());
            
            AuthDTO.RefreshTokenResponse refreshResponse = new AuthDTO.RefreshTokenResponse();
            refreshResponse.setAccess_token(tokenResponse.get("access_token").asText());
            refreshResponse.setRefresh_token(tokenResponse.get("refresh_token").asText());
            refreshResponse.setToken_type("bearer");
            refreshResponse.setExpires_in(tokenResponse.get("expires_in").asInt());

            return refreshResponse;

        } catch (HttpClientErrorException e) {
            throw new BusinessException("Token invalide ou expiré");
        } catch (Exception e) {
            throw new BusinessException("Erreur lors du rafraîchissement du token: " + e.getMessage());
        }
    }

    /**
     * Déconnexion
     */
    public void logout(AuthDTO.LogoutRequest request) {

        try {
            String logoutUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", request.getRefresh_token());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            
            restTemplate.exchange(
                logoutUrl,
                HttpMethod.POST,
                entity,
                String.class
            );


        } catch (Exception e) {
        }
    }

    private String extractUuidFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length > 1) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                JsonNode node = objectMapper.readTree(payload);
                if (node.has("jti")) {
                    return node.get("jti").asText();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private List<PrivilegeDTO.PrivilegeResponse> extractPrivilegesFromUser(User user) {
        List<Privilege> allPrivileges = new ArrayList<>();
        
        for (Role role : user.getRoles()) {
            allPrivileges.addAll(role.getPrivileges());
        }

        List<Privilege> rootPrivileges = allPrivileges.stream()
                .filter(p -> p.getParentId() == null)
                .distinct()
                .collect(Collectors.toList());

        return rootPrivileges.stream()
                .map(this::mapToPrivilegeResponseWithChildren)
                .collect(Collectors.toList());
    }

    private PrivilegeDTO.PrivilegeResponse mapToPrivilegeResponseWithChildren(Privilege privilege) {
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