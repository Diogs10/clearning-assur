package sn.suntelecoms.assurway.clearingapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.suntelecoms.assurway.clearingapi.dto.ApiResponse;
import sn.suntelecoms.assurway.clearingapi.dto.AuthDTO;
import sn.suntelecoms.assurway.clearingapi.service.AuthService;
import sn.suntelecoms.assurway.clearingapi.util.ResponseUtil;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API d'authentification et gestion des tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "Connexion", 
        description = "Authentifie un utilisateur et retourne un token JWT avec ses privilèges"
    )
    public ResponseEntity<AuthDTO.AuthResponseWrapper> login(
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        
        AuthDTO.LoginResponse loginResponse = authService.login(request);
        
        AuthDTO.AuthResponseWrapper wrapper = new AuthDTO.AuthResponseWrapper();
        wrapper.setData(loginResponse);
        wrapper.setResponseCode(200);
        
        return ResponseEntity.ok(wrapper);
    }

    @PostMapping("/refresh-token")
    @Operation(
        summary = "Rafraîchir le token", 
        description = "Génère un nouveau token d'accès à partir d'un refresh token"
    )
    public ResponseEntity<ApiResponse<AuthDTO.RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody AuthDTO.RefreshTokenRequest request) {
        
        AuthDTO.RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseUtil.success(response, "Token rafraîchi avec succès");
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Déconnexion", 
        description = "Révoque le refresh token et déconnecte l'utilisateur"
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody AuthDTO.LogoutRequest request) {
        
        authService.logout(request);
        return ResponseUtil.success(null, "Déconnexion réussie");
    }

    @GetMapping("/auth/validate")
    @Operation(
        summary = "Valider le token", 
        description = "Vérifie si le token JWT est valide"
    )
    public ResponseEntity<ApiResponse<Boolean>> validateToken() {
        return ResponseUtil.success(true, "Token valide");
    }
}