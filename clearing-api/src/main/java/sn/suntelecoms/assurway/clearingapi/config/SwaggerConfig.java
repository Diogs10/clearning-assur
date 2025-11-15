package sn.suntelecoms.assurway.clearingapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuration Swagger/OpenAPI avec authentification Keycloak
 */
@Configuration
public class SwaggerConfig {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Bean
    public OpenAPI customOpenAPI() {
        // URL d'autorisation et de token Keycloak
        String authUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/auth";
        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        return new OpenAPI()
                .info(new Info()
                        .title("Clearing API")
                        .version("1.0.0")
                        .description("API de gestion du clearing avec authentification Keycloak")
                        .contact(new Contact()
                                .name("SunTelecoms")
                                .email("contact@suntelecoms.sn")
                                .url("https://suntelecoms.sn"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8081").description("Serveur local"),
                        new Server().url("https://api.clearing.sn").description("Serveur production")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt")
                        .addList("oauth2"))
                .components(new Components()
                        // Schéma Bearer JWT
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .name("bearer-jwt")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("Entrez le token JWT obtenu depuis Keycloak"))
                        
                        // Schéma OAuth2 avec Keycloak
                        .addSecuritySchemes("oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Authentification OAuth2 avec Keycloak")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(authUrl)
                                                .tokenUrl(tokenUrl)
                                                .refreshUrl(tokenUrl))))
                );
    }
}