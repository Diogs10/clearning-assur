package sn.suntelecoms.assurway.clearingapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private int statusCode;
    private String status;
    private LocalDateTime timestamp;

    private ApiResponse(boolean success, String message, T data, int statusCode, String status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Succès", data, HttpStatus.OK.value(), "OK");
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, HttpStatus.OK.value(), "OK");
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "Ressource créée avec succès", data, HttpStatus.CREATED.value(), "CREATED");
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(true, message, data, HttpStatus.CREATED.value(), "CREATED");
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return new ApiResponse<>(true, "Requête acceptée", data, HttpStatus.ACCEPTED.value(), "ACCEPTED");
    }

    public static <T> ApiResponse<T> accepted(T data, String message) {
        return new ApiResponse<>(true, message, data, HttpStatus.ACCEPTED.value(), "ACCEPTED");
    }

    public static <T> ApiResponse<T> noContent() {
        return new ApiResponse<>(true, "Aucun contenu", null, HttpStatus.NO_CONTENT.value(), "NO_CONTENT");
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}