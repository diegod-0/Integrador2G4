package org.rescuelink.dto;

import java.time.Instant;
import java.util.List;

/**
 * Estándar de error de API según RFC 7807 / RFC 9457 (Problem Details).
 * Garantiza respuestas de error estructuradas sin fugas de información sensible.
 */
public record ProblemDetailsResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        Instant timestamp,
        List<InvalidParam> invalidParams
) {
    public record InvalidParam(String field, String reason) {}

    public static ProblemDetailsResponse of(String type, String title, int status, String detail, String instance) {
        return new ProblemDetailsResponse(type, title, status, detail, instance, Instant.now(), null);
    }

    public static ProblemDetailsResponse of(String type, String title, int status, String detail, String instance, List<InvalidParam> invalidParams) {
        return new ProblemDetailsResponse(type, title, status, detail, instance, Instant.now(), invalidParams);
    }
}
