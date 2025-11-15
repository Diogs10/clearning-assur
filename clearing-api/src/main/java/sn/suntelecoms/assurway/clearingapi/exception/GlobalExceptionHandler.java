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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        

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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        

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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        return ResponseUtil.notFound(ex.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex, WebRequest request) {
        
        return ResponseUtil.conflict(ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        return ResponseUtil.badRequest(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        return ResponseUtil.unauthorized("Identifiants invalides");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        return ResponseUtil.unauthorized("Erreur d'authentification: " + ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        return ResponseUtil.forbidden("Accès refusé: Vous n'avez pas les permissions nécessaires");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.customError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Une erreur inattendue s'est produite",
                        List.of(ex.getMessage()),
                        request.getDescription(false).replace("uri=", "")
                ));
    }
}