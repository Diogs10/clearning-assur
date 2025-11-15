package sn.suntelecoms.assurway.clearingapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Test", description = "Endpoints de test de l'authentification")
public class TestController {

    @GetMapping("/public/hello")
    @Operation(summary = "Endpoint public", description = "Accessible sans authentification")
    public Map<String, String> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Bienvenue sur l'API Clearing");
        response.put("status", "public");
        return response;
    }

    @GetMapping("/user/profile")
    @Operation(
        summary = "Profil utilisateur", 
        description = "Accessible aux utilisateurs authentifiés",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Map<String, Object> userProfile(@AuthenticationPrincipal Jwt jwt) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profil utilisateur");
        response.put("username", jwt.getClaimAsString("preferred_username"));
        response.put("email", jwt.getClaimAsString("email"));
        response.put("firstName", jwt.getClaimAsString("given_name"));
        response.put("lastName", jwt.getClaimAsString("family_name"));
        response.put("roles", jwt.getClaimAsMap("realm_access").get("roles"));
        response.put("userId", jwt.getSubject());
        
        return response;
    }

    @GetMapping("/admin/dashboard")
    @Operation(
        summary = "Dashboard admin", 
        description = "Accessible uniquement aux administrateurs",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminDashboard(@AuthenticationPrincipal Jwt jwt) {
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Dashboard administrateur");
        response.put("admin", jwt.getClaimAsString("preferred_username"));
        response.put("access", "full");
        
        return response;
    }

    @GetMapping("/user/token-info")
    @Operation(
        summary = "Informations du token", 
        description = "Affiche les détails du token JWT",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public Map<String, Object> tokenInfo(@AuthenticationPrincipal Jwt jwt) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("subject", jwt.getSubject());
        response.put("issuer", jwt.getIssuer());
        response.put("issuedAt", jwt.getIssuedAt());
        response.put("expiresAt", jwt.getExpiresAt());
        response.put("claims", jwt.getClaims());
        
        return response;
    }
}