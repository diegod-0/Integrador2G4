package org.rescuelink.adapter;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rescuelink.port.NotificationPort.AdoptionNotificationCommand;
import org.rescuelink.port.NotificationPort.NotificationDeliveryResult;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ISO 25010 Compatibilidad / Interoperabilidad: ResendNotificationAdapter + WireMock")
class ResendNotificationAdapterWireMockTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    @DisplayName("1. Contrato exitoso (200 OK): envía JSON válido, Authorization Bearer e Idempotency-Key")
    void debeEntregarNotificacionCumpliendoContratoEIdempotencia() {
        stubFor(post(urlEqualTo("/emails"))
                .withHeader("Authorization", equalTo("Bearer re_live_test_token"))
                .withHeader("Idempotency-Key", equalTo("idem-key-001"))
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": \"msg_resend_998877\"}")));

        ResendNotificationAdapter adapter = new ResendNotificationAdapter(
                wireMockServer.baseUrl(),
                "re_live_test_token",
                "adopciones@rescuelink.org",
                true,
                1500,
                1500
        );

        AdoptionNotificationCommand cmd = new AdoptionNotificationCommand(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "adoptante@ejemplo.pe",
                "Lucía Paredes",
                "Rocky",
                "APROBADA",
                "CERT-RL-A1B2C3D4E5F6",
                "idem-key-001"
        );

        NotificationDeliveryResult result = adapter.enviarNotificacionAdopcion(cmd);

        assertThat(result.entregado()).isTrue();
        assertThat(result.estadoEntrega()).isEqualTo("DELIVERED");
        assertThat(result.externalMessageId()).isEqualTo("msg_resend_998877");
        assertThat(result.httpStatusCode()).isEqualTo(200);

        verify(postRequestedFor(urlEqualTo("/emails"))
                .withHeader("Idempotency-Key", equalTo("idem-key-001"))
                .withRequestBody(matchingJsonPath("$.to", equalTo("adoptante@ejemplo.pe")))
                .withRequestBody(matchingJsonPath("$.from", equalTo("adopciones@rescuelink.org"))));
    }

    @Test
    @DisplayName("2. Manejo controlado de errores 4xx/5xx (503 Service Unavailable): aplica degradación elegante")
    void debeAplicarDegradacionEleganteAnteFallo503DelProveedorExterno() {
        stubFor(post(urlEqualTo("/emails"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"External mail cluster overloaded\"}")));

        ResendNotificationAdapter adapter = new ResendNotificationAdapter(
                wireMockServer.baseUrl(),
                "re_live_test_token",
                "adopciones@rescuelink.org",
                true,
                1500,
                1500
        );

        AdoptionNotificationCommand cmd = new AdoptionNotificationCommand(
                UUID.randomUUID(),
                "ciudadano@ejemplo.pe",
                "Marco Soto",
                "Luna",
                "RECIBIDA",
                null,
                "idem-key-503"
        );

        NotificationDeliveryResult result = adapter.enviarNotificacionAdopcion(cmd);

        assertThat(result.entregado()).isFalse();
        assertThat(result.estadoEntrega()).isEqualTo("DEGRADED_FALLBACK");
        assertThat(result.httpStatusCode()).isEqualTo(503);
    }

    @Test
    @DisplayName("3. Tolerancia a Timeouts de red: corta espera al superar readTimeoutMs y preserva operación")
    void debeCapturarTimeoutDeRedYRetornarEstadoDegradado504() {
        stubFor(post(urlEqualTo("/emails"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(1200)
                        .withBody("{\"id\": \"msg_delayed\"}")));

        ResendNotificationAdapter adapter = new ResendNotificationAdapter(
                wireMockServer.baseUrl(),
                "re_live_test_token",
                "adopciones@rescuelink.org",
                true,
                300,
                300
        );

        AdoptionNotificationCommand cmd = new AdoptionNotificationCommand(
                UUID.randomUUID(),
                "timeout@ejemplo.pe",
                "Elena Ríos",
                "Thor",
                "RECIBIDA",
                null,
                "idem-key-timeout"
        );

        NotificationDeliveryResult result = adapter.enviarNotificacionAdopcion(cmd);

        assertThat(result.entregado()).isFalse();
        assertThat(result.estadoEntrega()).isEqualTo("DEGRADED_FALLBACK");
        assertThat(result.httpStatusCode()).isEqualTo(504);
    }

    @Test
    @DisplayName("4. Modo Sandbox deshabilitado (enabled=false): simula entrega sin tráfico de red externo")
    void debeOperarEnModoSimuladoCuandoIntegracionExternaEstaDeshabilitada() {
        ResendNotificationAdapter adapter = new ResendNotificationAdapter(
                wireMockServer.baseUrl(),
                "re_sandbox_key",
                "adopciones@rescuelink.org",
                false,
                1000,
                1000
        );

        AdoptionNotificationCommand cmd = new AdoptionNotificationCommand(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "sandbox@rescuelink.org",
                "Usuario Sandbox",
                "Rocky",
                "RECIBIDA",
                null,
                null
        );

        NotificationDeliveryResult result = adapter.enviarNotificacionAdopcion(cmd);

        assertThat(result.entregado()).isTrue();
        assertThat(result.estadoEntrega()).isEqualTo("SIMULATED_LOCAL");
        verify(0, postRequestedFor(urlEqualTo("/emails")));
    }
}

