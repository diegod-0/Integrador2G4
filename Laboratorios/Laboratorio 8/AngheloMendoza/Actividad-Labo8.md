# Actividad de Laboratorio 8: Pruebas con Testcontainers, Contenerización Docker y Despliegue Cloud
**Curso Integrador II: Software (100000S12F) — Ciclo 2026-2**  
**Proyecto:** RescueLink — Sistema de información web para la coordinación, seguimiento espacial y gestión de adopciones de animales en riesgo  
**Estudiante:** Anghelo Mendoza  

---

## 1. Estrategia Integral de Pruebas (Testcontainers, Integración y E2E)
Aseguramiento de calidad bajo la norma ISO/IEC 25010 y pirámide de automatización de pruebas:
* **Pruebas Unitarias de Capa de Negocio:** Validación aislada de máquinas de estado de tickets y animales, cálculo de compatibilidad del Matchmaker y respuestas Problem Details con JUnit 5 y Mockito. Cobertura meta $> 75\%$ en JaCoCo.
* **Pruebas de Integración con Base de Datos Real Efímera (Testcontainers):**
  - Uso de contenedor Docker efímero con imagen `postgis/postgis:17-3.5-alpine` aprovisionado automáticamente en JUnit.
  - Spring Boot conecta su DataSource al puerto dinámico mediante `@DynamicPropertySource`.
  - Validación de funciones espaciales reales `ST_DWithin` y `ST_DistanceSphere` con Flyway ejecutando las migraciones completas.
* **Pruebas Funcionales End-to-End (E2E) sin Mocks con Playwright:** Automatización del flujo crítico (*Golden Path*) en navegador Chromium headless contra el backend real: envío de reporte ágil con GPS, verificación de redirección a `/tracking/:codigo`, confirmación visual del estado `PENDIENTE` y actualización reactiva por WebSocket.

---

## 2. Empaquetado en Contenedores y Orquestación Local (Docker Multi-Stage)
Estandarización de entornos reproducibles mediante contenedores optimizados:
* **Dockerfile Multi-Stage Front-End:**
  - *Etapa 1 (Build):* Node.js 22 Alpine compila la SPA (`ng build --configuration production`).
  - *Etapa 2 (Runtime):* Nginx 1.27 Alpine sirve los binarios estáticos en `/usr/share/nginx/html` con compresión Gzip, cabeceras de seguridad y ruteo SPA (`try_files`), produciendo una imagen ligera $< 35\text{ MB}$.
* **Dockerfile Multi-Stage Back-End:**
  - *Etapa 1 (Build):* Maven con Eclipse Temurin 21 compila el ejecutable JAR.
  - *Etapa 2 (Runtime):* Eclipse Temurin JRE 21 Alpine con extracción de capas (*Layertools*) y ejecución bajo usuario sin privilegios `appuser`, reduciendo la imagen a $< 200\text{ MB}$.
* **Orquestación con Docker Compose:** Definición de `docker-compose.yml` integrando el frontend en Nginx, la API Spring Boot, PostgreSQL primario (5432) y la réplica (5433) bajo una red virtual compartida (`rescuelink-network`), con comprobaciones de salud (`service_healthy`) y volúmenes persistentes.

---

## 3. Despliegue en la Nube (Cloud PaaS), Observabilidad y Pruebas de Humo
Puesta en marcha del entregable de Versión 1 en producción (Semana 9):
* **Despliegue Cloud en PaaS (Render / Railway):** Despliegue del contenedor de Spring Boot conectado a una instancia cloud administrada de PostgreSQL 17 + PostGIS, con migraciones automáticas de Flyway y frontend publicado bajo HTTPS automático con certificados SSL/TLS.
* **Observabilidad con Spring Boot Actuator:** Endpoints `/actuator/health` y `/actuator/metrics` exponiendo el estado de conexión de base de datos, memoria JVM y latencia de peticiones, monitoreados externamente mediante **UptimeRobot** con disponibilidad meta $\ge 99.5\%$.
* **Validación Post-Despliegue con Smoke Tests:** Script automatizado en Bash/curl que valida secuencialmente: salud general del servicio (HTTP 200), documentación interactiva Swagger UI, lectura pública de catálogo y bloqueo estricto con HTTP 401 en rutas privadas sin token.
* **Retrospectiva del Sprint 2:** Cumplimiento del 100% de la velocidad comprometida (14 Story Points entre HU05, HU07, HU09 y HU10), consolidando el incremento del APF2.