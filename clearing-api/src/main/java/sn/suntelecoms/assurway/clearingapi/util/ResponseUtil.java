package sn.suntelecoms.assurway.clearingapi.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import sn.suntelecoms.assurway.clearingapi.dto.ApiErrorResponse;
import sn.suntelecoms.assurway.clearingapi.dto.ApiResponse;
import sn.suntelecoms.assurway.clearingapi.dto.PaginatedResponse;

import java.util.List;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    public static <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> successPaginated(Page<T> page) {
        PaginatedResponse<T> paginatedData = new PaginatedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
        return ResponseEntity.ok(ApiResponse.success(paginatedData));
    }

    public static <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> successPaginated(Page<T> page, String message) {
        PaginatedResponse<T> paginatedData = new PaginatedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
        return ResponseEntity.ok(ApiResponse.success(paginatedData, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data, message));
    }

    public static ResponseEntity<ApiErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(message));
    }

    public static ResponseEntity<ApiErrorResponse> badRequest(String message, List<String> details) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(message, details));
    }

    public static ResponseEntity<ApiErrorResponse> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.unauthorized(message));
    }

    public static ResponseEntity<ApiErrorResponse> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiErrorResponse.forbidden(message));
    }

    public static ResponseEntity<ApiErrorResponse> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.notFound(message));
    }

    public static ResponseEntity<ApiErrorResponse> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.conflict(message));
    }

    public static ResponseEntity<ApiErrorResponse> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.internalServerError(message));
    }

    public static ResponseEntity<ApiErrorResponse> customError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiErrorResponse.customError(status, message));
    }

    public static ResponseEntity<ApiErrorResponse> customError(HttpStatus status, String message,
                                                               List<String> details, String path) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.customError(status, message, details, path));
    }
}
