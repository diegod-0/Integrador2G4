package org.rescuelink.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rescuelink.dto.AdopcionDtos.PostularAdopcionRequest;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Evidencia de Verificación APF3 — Anghelo Mendoza (HU06 & ISO/IEC 25010
 * Funcionalidad).
 * Valida las reglas de negocio e idoneidad del contrato de postulación a
 * adopción
 * bajo la Ley N° 30407 y protección contra errores de entrada.
 */
@DisplayName("HU06 & ISO 25010 Funcionalidad: Validación de Contrato PostularAdopcionRequest")
class AdopcionControllerContractTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("1. Solicitud idónea completa: cumple todos los criterios de aceptación de HU06")
    void debeAceptarPostulacionCuandoCumpleTodosLosRequisitosDeIdoneidad() {
        PostularAdopcionRequest request = new PostularAdopcionRequest(
                UUID.randomUUID(),
                "Andrea Salazar",
                "andrea.salazar@ejemplo.pe",
                "+51987654321",
                "CASA_CON_JARDIN",
                true,
                "Cuento con patio cercado, presupuesto mensual y compromiso de esterilización.",
                true);

        Set<ConstraintViolation<PostularAdopcionRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("2. Protección Ley 30407: bloquea postulación si no acepta la declaración jurada obligatoria")
    void debeRechazarPostulacionSiNoAceptaDeclaracionJuradaLey30407() {
        PostularAdopcionRequest request = new PostularAdopcionRequest(
                UUID.randomUUID(),
                "Andrea Salazar",
                "andrea.salazar@ejemplo.pe",
                "987654321",
                "DEPARTAMENTO_CON_BALCON",
                false,
                "Motivo suficientemente detallado para pasar validación de longitud.",
                false);

        Set<ConstraintViolation<PostularAdopcionRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("aceptaTerminosLey30407"));
    }

    @Test
    @DisplayName("3. Protección contra errores: bloquea formato de teléfono inválido y motivo demasiado corto")
    void debeRechazarTelefonoInvalidoYMotivoCorto() {
        PostularAdopcionRequest request = new PostularAdopcionRequest(
                UUID.randomUUID(),
                "Andrea Salazar",
                "andrea.salazar@ejemplo.pe",
                "12345",
                "DEPARTAMENTO_SIN_BALCON",
                false,
                "Muy corto",
                true);

        Set<ConstraintViolation<PostularAdopcionRequest>> violations = validator.validate(request);
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
    }
}