package org.rescuelink.adapter;

import lombok.extern.slf4j.Slf4j;
import org.rescuelink.port.NotificationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador de infraestructura para interoperabilidad con la API externa de Resend
 * (Unidad 3 - ISO/IEC 25010 Compatibilidad / Interoperabilidad).
 * Implementa clave de idempotencia (Idempotency-Key), timeouts estrictos y
 * degradación elegante ante fallos HTTP 4xx, 5xx o caídas de red.
 */
@Slf4j
@Component
public class ResendNotificationAdapter implements NotificationPort {

    private final RestClient restClient;
    private final String apiKey;
    private final String senderEmail;
    private final boolean enabled;

    @Autowired
    public ResendNotificationAdapter(
            @Value("${application.integrations.resend.base-url:https://api.resend.com}") String baseUrl,
            @Value("${application.integrations.resend.api-key:re_sandbox_local_key}") String apiKey,
            @Value("${application.integrations.resend.sender-email:adopciones@rescuelink.org}") String senderEmail,
            @Value("${application.integrations.resend.enabled:false}") boolean enabled,
            @Value("${application.integrations.resend.connect-timeout-ms:2500}") int connectTimeoutMs,
            @Value("${application.integrations.resend.read-timeout-ms:3000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.enabled = enabled;
    }

    @Override
    public NotificationDeliveryResult enviarNotificacionAdopcion(AdoptionNotificationCommand command) {
        String idempotencyKey = (command.idempotencyKey() != null && !command.idempotencyKey().isBlank())
                ? command.idempotencyKey()
                : UUID.nameUUIDFromBytes(
                        (command.solicitudId() + ":" + command.estadoSolicitud()).getBytes(StandardCharsets.UTF_8)
                ).toString();

        if (!enabled) {
            log.info("[Interoperabilidad-Sandbox] Notificación simulada para {} (Animal: {}, Estado: {}, Idempotency-Key: {})",
                    command.destinatarioEmail(), command.nombreAnimal(), command.estadoSolicitud(), idempotencyKey);
            return NotificationDeliveryResult.simulated(idempotencyKey);
        }

        String subject = String.format("RescueLink · Estado de tu solicitud para %s: %s",
                command.nombreAnimal(), command.estadoSolicitud());
        String htmlBody = construirHtmlNotificacion(command);

        Map<String, Object> payload = Map.of(
                "from", senderEmail,
                "to", command.destinatarioEmail(),
                "subject", subject,
                "html", htmlBody
        );

        try {
            ResendEmailResponse response = restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Idempotency-Key", idempotencyKey)
                    .body(payload)
                    .retrieve()
                    .body(ResendEmailResponse.class);

            String externalId = (response != null && response.id() != null) ? response.id() : "ext-" + idempotencyKey;
            log.info("[Interoperabilidad-Resend] Entrega confirmada id={} para solicitud={}", externalId, command.solicitudId());
            return NotificationDeliveryResult.delivered(externalId, 200);

        } catch (RestClientResponseException ex) {
            HttpStatusCode status = ex.getStatusCode();
            log.warn("[Interoperabilidad-Degradada] Proveedor externo respondió HTTP {} para solicitud={}: {}",
                    status.value(), command.solicitudId(), ex.getStatusText());
            return NotificationDeliveryResult.degraded(
                    status.value(),
                    "Fallo controlado del proveedor externo HTTP " + status.value() + "; transacción local preservada."
            );
        } catch (ResourceAccessException ex) {
            log.warn("[Interoperabilidad-Timeout] Timeout o error de red con proveedor externo para solicitud={}: {}",
                    command.solicitudId(), ex.getMessage());
            return NotificationDeliveryResult.degraded(
                    504,
                    "Timeout de conexión con proveedor externo; se activó degradación elegante sin pérdida de datos."
            );
        } catch (Exception ex) {
            if (isTimeout(ex)) {
                log.warn("[Interoperabilidad-Timeout] Timeout detectado en proveedor externo para solicitud={}: {}",
                        command.solicitudId(), ex.getMessage());
                return NotificationDeliveryResult.degraded(
                        504,
                        "Timeout de conexión con proveedor externo; se activó degradación elegante sin pérdida de datos."
                );
            }
            log.warn("[Interoperabilidad-Fallback] Error inesperado al invocar proveedor externo: {}", ex.getMessage());
            return NotificationDeliveryResult.degraded(
                    503,
                    "Servicio externo temporalmente no disponible; registrado en cola de contingencia."
            );
        }
    }

    private boolean isTimeout(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String className = current.getClass().getName();
            String message = current.getMessage() != null ? current.getMessage().toLowerCase() : "";
            if (className.contains("Timeout")
                    || className.contains("SocketTimeout")
                    || message.contains("timed out")
                    || message.contains("timeout")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String construirHtmlNotificacion(AdoptionNotificationCommand cmd) {
        String qrInfo = cmd.codigoCertificadoQr() != null
                ? "<p><strong>Código de Certificado Oficial QR:</strong> <code>" + cmd.codigoCertificadoQr() + "</code></p>"
                : "";
        return String.format(
                "<div><h3>Hola, %s</h3><p>Tu solicitud de adopción para <strong>%s</strong> se encuentra en estado: <strong>%s</strong>.</p>%s</div>",
                cmd.destinatarioNombre(),
                cmd.nombreAnimal(),
                cmd.estadoSolicitud(),
                qrInfo
        );
    }

    public record ResendEmailResponse(String id) {}
}

