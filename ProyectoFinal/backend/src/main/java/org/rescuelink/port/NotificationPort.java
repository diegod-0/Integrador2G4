package org.rescuelink.port;

import java.util.UUID;

/**
 * Puerto de salida (Arquitectura Hexagonal) para la interoperabilidad con sistemas
 * externos de mensajería y notificaciones transaccionales (ISO/IEC 25010 - Interoperabilidad).
 */
public interface NotificationPort {

    /**
     * Envía una notificación transaccional al ciudadano adoptante sobre su postulación
     * o emisión de certificado oficial, garantizando idempotencia y degradación elegante.
     */
    NotificationDeliveryResult enviarNotificacionAdopcion(AdoptionNotificationCommand command);

    /**
     * Comando inmutable para el envío de notificación de adopción al servicio externo.
     */
    record AdoptionNotificationCommand(
            UUID solicitudId,
            String destinatarioEmail,
            String destinatarioNombre,
            String nombreAnimal,
            String estadoSolicitud,
            String codigoCertificadoQr,
            String idempotencyKey
    ) {}

    /**
     * Resultado auditado de la operación de interoperabilidad externa.
     */
    record NotificationDeliveryResult(
            boolean entregado,
            String estadoEntrega,
            String externalMessageId,
            int httpStatusCode,
            String detalle
    ) {
        public static NotificationDeliveryResult delivered(String externalId, int statusCode) {
            return new NotificationDeliveryResult(true, "DELIVERED", externalId, statusCode, "Notificación procesada exitosamente por proveedor externo.");
        }

        public static NotificationDeliveryResult simulated(String idempotencyKey) {
            return new NotificationDeliveryResult(true, "SIMULATED_LOCAL", "sim-" + idempotencyKey, 200, "Modo sandbox activo; entrega simulada localmente.");
        }

        public static NotificationDeliveryResult degraded(int statusCode, String motivo) {
            return new NotificationDeliveryResult(false, "DEGRADED_FALLBACK", null, statusCode, motivo);
        }
    }
}

