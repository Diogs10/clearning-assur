package sn.suntelecoms.assurway.clearingapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import sn.suntelecoms.assurway.clearingapi.dto.ApiErrorResponse;
import sn.suntelecoms.assurway.clearingapi.util.ResponseUtil;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestion des erreurs de validation (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.error("Erreur de validation: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.customError(
                        HttpStatus.BAD_REQUEST,
                        "Erreurs de validation",
                        errors,
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    /**
     * Gestion des violations de contraintes
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        log.error("Violation de contrainte: {}", ex.getMessage());

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.customError(
                        HttpStatus.BAD_REQUEST,
                        "Violations de contraintes",
                        errors,
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    /**
     * Gestion des ressources non trouvées
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.error("Ressource non trouvée: {}", ex.getMessage());
        return ResponseUtil.notFound(ex.getMessage());
    }

    /**
     * Gestion des ressources déjà existantes
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex, WebRequest request) {
        
        log.error("Ressource déjà existante: {}", ex.getMessage());
        return ResponseUtil.conflict(ex.getMessage());
    }

    /**
     * Gestion des exceptions métier
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        log.error("Erreur métier: {}", ex.getMessage());
        return ResponseUtil.badRequest(ex.getMessage());
    }

    /**
     * Gestion des erreurs d'authentification (identifiants invalides)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        log.error("Identifiants invalides: {}", ex.getMessage());
        return ResponseUtil.unauthorized("Identifiants invalides");
    }

    /**
     * Gestion des erreurs d'authentification générales
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        log.error("Erreur d'authentification: {}", ex.getMessage());
        return ResponseUtil.unauthorized("Erreur d'authentification: " + ex.getMessage());
    }

    /**
     * Gestion des accès refusés (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        log.error("Accès refusé: {}", ex.getMessage());
        return ResponseUtil.forbidden("Accès refusé: Vous n'avez pas les permissions nécessaires");
    }

    /**
     * Gestion des RuntimeException génériques
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        log.error("Erreur runtime: {}", ex.getMessage(), ex);
        String message = ex.getMessage();

        if (message.contains("existe déjà") || message.contains("duplicate")) {
            return ResponseUtil.conflict(message);
        } else if (message.contains("non trouvé") || message.contains("not found")) {
            return ResponseUtil.notFound(message);
        } else if (message.contains("Impossible de supprimer")) {
            return ResponseUtil.badRequest(message);
        } else {
            return ResponseUtil.internalServerError("Erreur interne: " + message);
        }
    }

    /**
     * Gestion de toutes les autres exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("Erreur inattendue: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.customError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Une erreur inattendue s'est produite",
                        List.of(ex.getMessage()),
                        request.getDescription(false).replace("uri=", "")
                ));
    }
}