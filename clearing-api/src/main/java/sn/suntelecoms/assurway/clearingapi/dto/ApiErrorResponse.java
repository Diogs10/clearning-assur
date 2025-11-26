package sn.suntelecoms.assurway.clearingapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private boolean success;
    private String message;
    private int responseCode;
    private String status;
    private String error;
    private List<String> details;
    private String path;
    private LocalDateTime timestamp;

    private ApiErrorResponse(boolean success, String message, int responseCode, String status,
                             String error, List<String> details, String path) {
        this.success = success;
        this.message = message;
        this.responseCode = responseCode;
        this.status = status;
        this.error = error;
        this.details = details;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public static ApiErrorResponse badRequest(String message) {
        return new ApiErrorResponse(false, message, HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST", "Requête invalide", null, null);
    }

    public static ApiErrorResponse badRequest(String message, List<String> details) {
        return new ApiErrorResponse(false, message, HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST", "Requête invalide", details, null);
    }

    public static ApiErrorResponse unauthorized(String message) {
        return new ApiErrorResponse(false, message, HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED", "Non autorisé", null, null);
    }

    public static ApiErrorResponse forbidden(String message) {
        return new ApiErrorResponse(false, message, HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN", "Accès interdit", null, null);
    }

    public static ApiErrorResponse notFound(String message) {
        return new ApiErrorResponse(false, message, HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND", "Ressource non trouvée", null, null);
    }

    public static ApiErrorResponse conflict(String message) {
        return new ApiErrorResponse(false, message, HttpStatus.CONFLICT.value(),
                "CONFLICT", "Conflit", null, null);
    }

    public static ApiErrorResponse unprocessableEntity(String message) {
        return new ApiErrorResponse(false, message, HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "UNPROCESSABLE_ENTITY", "Entité non traitable", null, null);
    }

    public static ApiErrorResponse unprocessableEntity(String message, List<String> details) {
        return new ApiErrorResponse(false, message, HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "UNPROCESSABLE_ENTITY", "Entité non traitable", details, null);
    }

    public static ApiErrorResponse internalServerError(String message) {
        return new ApiErrorResponse(false, message, HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR", "Erreur interne du serveur", null, null);
    }

    public static ApiErrorResponse serviceUnavailable(String message) {
        return new ApiErrorResponse(false, message, HttpStatus.SERVICE_UNAVAILABLE.value(),
                "SERVICE_UNAVAILABLE", "Service indisponible", null, null);
    }

    public static ApiErrorResponse customError(HttpStatus status, String message) {
        return new ApiErrorResponse(false, message, status.value(),
                status.name(), status.getReasonPhrase(), null, null);
    }

    public static ApiErrorResponse customError(HttpStatus status, String message,
                                               List<String> details, String path) {
        return new ApiErrorResponse(false, message, status.value(),
                status.name(), status.getReasonPhrase(), details, path);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getresponseCode() { return responseCode; }
    public void setresponseCode(int responseCode) { this.responseCode = responseCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}