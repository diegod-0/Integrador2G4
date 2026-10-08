package org.rescuelink.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.rescuelink.dto.ProblemDetailsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Manejador global centralizado de excepciones con estándar RFC 7807 (Problem Details).
 * Oculta trazas internas del servidor y expone contratos de error limpios y consistentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailsResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ProblemDetailsResponse.InvalidParam> invalidParams = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return new ProblemDetailsResponse.InvalidParam(fieldName, errorMessage);
                })
                .toList();

        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/validation-error",
                "Error de Validación de Parámetros",
                HttpStatus.BAD_REQUEST.value(),
                "Uno o más campos de la solicitud no cumplen con las reglas de validación sintáctica.",
                request.getRequestURI(),
                invalidParams
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetailsResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/invalid-parameter",
                "Parámetro Inválido",
                HttpStatus.BAD_REQUEST.value(),
                String.format("El parámetro '%s' tiene un formato inválido.", ex.getName()),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetailsResponse> handleBusiness(
            BusinessException ex,
            HttpServletRequest request
    ) {
        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/business-rule",
                "Regla de Negocio Incumplida",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetailsResponse> handleMissingParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/missing-parameter",
                "Parámetro Requerido Ausente",
                HttpStatus.BAD_REQUEST.value(),
                String.format("El parámetro obligatorio '%s' no fue proporcionado.", ex.getParameterName()),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetailsResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/invalid-body",
                "Cuerpo de Solicitud Inválido",
                HttpStatus.BAD_REQUEST.value(),
                "El cuerpo de la solicitud es inválido o contiene valores no reconocidos.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetailsResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/resource-not-found",
                "Recurso No Encontrado",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(DuplicateReportException.class)
    public ResponseEntity<ProblemDetailsResponse> handleDuplicateReport(
            DuplicateReportException ex,
            HttpServletRequest request
    ) {
        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/duplicate-report",
                "Conflicto de Emergencia Duplicada",
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(CapacityExceededException.class)
    public ResponseEntity<ProblemDetailsResponse> handleCapacityExceeded(
            CapacityExceededException ex,
            HttpServletRequest request
    ) {
        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/capacity-exceeded",
                "Capacidad de Albergue Superada",
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetailsResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/unauthorized",
                "Credenciales Inválidas",
                HttpStatus.UNAUTHORIZED.value(),
                "El correo electrónico o la contraseña proporcionados son incorrectos.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetailsResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/forbidden",
                "Acceso Denegado",
                HttpStatus.FORBIDDEN.value(),
                "No posee los privilegios suficientes para acceder a este recurso.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailsResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        ProblemDetailsResponse problem = ProblemDetailsResponse.of(
                "https://rescuelink.org/errors/internal-server-error",
                "Error Interno del Servidor",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ha ocurrido una anomalía inesperada. Por favor contacte con el administrador del sistema.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
