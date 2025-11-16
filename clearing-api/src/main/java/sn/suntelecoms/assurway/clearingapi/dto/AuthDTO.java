package sn.suntelecoms.assurway.clearingapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

public class AuthDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Le username est obligatoire")
        private String username;

        @NotBlank(message = "Le mot de passe est obligatoire")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String access_token;
        private String refresh_token;
        private List<String> roles;
        private String token_type;
        private Integer expires_in;
        private String uuid;
        private String email;
        private String username;
        private List<PrivilegeDTO.PrivilegeResponse> privileges;
        private String firstName;
        private String lastName;
        private String telephone;
        private UUID id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        @NotBlank(message = "Le refresh token est obligatoire")
        private String refresh_token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenResponse {
        private String access_token;
        private String refresh_token;
        private String token_type;
        private Integer expires_in;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutRequest {
        private String refresh_token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponseWrapper {
        private LoginResponse data;
        private Integer responseCode;
    }
}