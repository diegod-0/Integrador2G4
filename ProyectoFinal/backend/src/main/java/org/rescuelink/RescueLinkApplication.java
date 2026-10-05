package org.rescuelink;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

/**
 * Punto de entrada principal del servicio RescueLink Backend.
 * Gobernado bajo Spring Boot 3.4 y Java 21 LTS.
 */
@SpringBootApplication
@EnableJpaAuditing
public class RescueLinkApplication {

    /**
     * Fija la zona horaria del runtime en UTC (Coordinated Universal Time).
     * Garantiza consistencia temporal universal en todos los registros y logs de auditoría.
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(RescueLinkApplication.class, args);
    }
}
