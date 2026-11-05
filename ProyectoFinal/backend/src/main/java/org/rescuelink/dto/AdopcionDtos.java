package org.rescuelink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.rescuelink.model.enums.EspecieAnimal;
import org.rescuelink.model.enums.EstadoSolicitud;

import java.time.Instant;
import java.util.UUID;

/**
 * Contratos de transferencia inmutables (Java Records) para el flujo de postulación,
 * dictamen de adopción, interoperabilidad de notificaciones y verificación de certificados QR.
 */
public final class AdopcionDtos {

    private AdopcionDtos() {}

    @Schema(description = "Solicitud ciudadana de postulación a adopción con validaciones de idoneidad (Ley 30407)")
    public record PostularAdopcionRequest(
            @NotNull(message = "El identificador del animal es obligatorio")
            UUID animalId,

            @NotBlank(message = "El nombre completo del adoptante es obligatorio")
            @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
            String nombreAdoptante,

            @NotBlank(message = "El correo electrónico del adoptante es obligatorio")
            @Email(message = "El formato del correo electrónico no es válido")
            String emailAdoptante,

            @NotBlank(message = "El teléfono de contacto es obligatorio")
            @Pattern(regexp = "^(\\+51)?9\\d{8}$", message = "El teléfono debe ser un celular válido de 9 dígitos")
            String telefonoAdoptante,

            @NotBlank(message = "El tipo de vivienda es obligatorio")
            String tipoVivienda,

            @NotNull(message = "Debe especificar si cuenta con otras mascotas")
            Boolean tieneOtrasMascotas,

            @NotBlank(message = "El motivo de adopción es obligatorio")
            @Size(min = 15, max = 1000, message = "El motivo de adopción debe detallar al menos 15 caracteres")
            String motivo,

            @AssertTrue(message = "Debe aceptar la declaración jurada de tenencia responsable (Ley N° 30407)")
            boolean aceptaTerminosLey30407
    ) {}

    @Schema(description = "Respuesta transaccional de postulación o dictamen con trazabilidad de interoperabilidad externa")
    public record SolicitudAdopcionResponse(
            UUID id,
            UUID animalId,
            String nombreAnimal,
            String nombreAdoptante,
            String emailAdoptante,
            EstadoSolicitud estado,
            String tipoVivienda,
            Boolean tieneOtrasMascotas,
            String codigoCertificadoQr,
            String estadoNotificacionExterna,
            String externalMessageId,
            Instant fechaSolicitud
    ) {}

    @Schema(description = "Respuesta pública de verificación criptográfica de Certificado Oficial de Adopción (QR)")
    public record VerificacionCertificadoQrResponse(
            boolean valido,
            String codigoCertificadoQr,
            String estadoCertificado,
            String nombreAnimal,
            EspecieAnimal especie,
            String nombreAlbergue,
            String nombreAdoptante,
            Instant fechaSolicitud,
            String mensajeVerificacion
    ) {}
}

