package org.rescuelink.dto;

import java.time.Instant;

/**
 * Envoltorio estándar de respuesta exitosa de la API.
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Operación exitosa", data, Instant.now());
    }
}
