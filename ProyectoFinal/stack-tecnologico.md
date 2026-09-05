# Stack Tecnológico
**RescueLink — Sistema Web de Rescate, Refugio y Adopción de Animales**
**Última actualización: agosto 2026**

---

## 1. Backend

| Componente | Tecnología | Versión | Detalle y Justificación |
|---|---|---|---|
| **Framework** | Spring Boot | **3.4.x** | Sobre Spring Framework 6.2; estándar empresarial actual y estable |
| **Lenguaje / runtime** | Java | **21 LTS** | Soporte a largo plazo, Virtual Threads y modern syntax de Java |
| **Seguridad** | Spring Security | **6.4** | JWT stateless, BCrypt para hash y control de acceso por roles |
| **Acceso a datos** | Spring Data JPA / Hibernate | — | Repository Pattern, DTOs y consultas optimizadas |
| **Auditoría transversal** | Spring Data JPA Auditing | — | Trazabilidad automática (`created_at`, `updated_at`, `created_by`) |
| **Documentación de API** | springdoc-openapi | 2.x | Swagger UI autogenerado bajo especificación OpenAPI 3 |
| **Migraciones de BD** | Flyway | — | Control de versiones del esquema DDL en el monorepo (`V1__...sql`) |
| **Manejo de errores** | RFC 7807 (Problem Details) | — | `@RestControllerAdvice` estandarizado para evitar fuga de información |
| **Rate limiting** | Bucket4j | — | Mitigación contra fuerza bruta en `/api/auth/login` |

---

## 2. Frontend

| Componente | Tecnología | Versión | Detalle y Justificación |
|---|---|---|---|
| **Framework** | Angular (Standalone Components) | **19.x LTS** | Arquitectura empresarial modular sin NgModules, control de flujo nativo (@if, @for) |
| **Lenguaje / Tipado** | TypeScript | **5.x** | Tipado estático estricto para contratos DTO consistentes con el backend |
| **Patrón arquitectónico** | Smart-Dumb Components + Signals Reactive State | — | Flujo de datos unidireccional (Inputs/Outputs), reactividad fina con `signal()` y `@defer` |
| **Mapas y geolocalización** | Leaflet / Mapbox GL JS | — | Visualización interactiva y captura de coordenadas (lat, lng) |
| **Comunicación en tiempo real** | STOMP sobre WebSockets | — | Suscripción a tópicos de eventos (cambios de estado de tickets) |
| **Métricas WPO** | Google Lighthouse | — | Auditoría continua de Core Web Vitals (LCP, INP, CLS) y presupuestos en `angular.json` |

---

## 3. Base de Datos y Almacenamiento

| Componente | Tecnología | Versión | Detalle y Justificación |
|---|---|---|---|
| **Base de datos relacional** | PostgreSQL | **17+** | Robustez transaccional ACID, soporte JSON y madurez |
| **Extensión geoespacial** | **PostGIS** | — | Tipos de datos espaciales (`GEOMETRY(Point, 4326)`) e índices `GiST` para búsquedas de cercanía |
| **Almacenamiento de archivos** | Cloudinary / MinIO | — | Almacenamiento cloud para imágenes en producción (plan gratuito) / MinIO local en Docker |
| **Patrón de almacenamiento** | Adapter Pattern | — | El core del backend usa la interfaz `StoragePort`, desacoplada del proveedor |

---

## 4. Testing y Calidad (ISO 25010)

| Componente | Tecnología | Uso y Justificación |
|---|---|---|
| **Pruebas unitarias/integración** | JUnit 5 + Mockito | Validación aislada de reglas de negocio en la capa de servicios |
| **Pruebas con base de datos real** | **Testcontainers** | Levanta PostgreSQL + PostGIS efímero en Docker durante la ejecución de tests |
| **Cobertura de código** | JaCoCo | Medición de cobertura para evidenciar *Testability* (objetivo: >70%) |
| **Pruebas de estrés y carga** | Apache JMeter | Medición de tiempo de respuesta, throughput y degradación bajo concurrencia |

---

## 5. DevOps e Infraestructura

| Componente | Tecnología | Uso y Justificación |
|---|---|---|
| **Contenerización** | Docker + Docker Compose | Entorno local reproducible (backend, PostgreSQL+PostGIS, frontend) |
| **CI/CD** | GitHub Actions | Automatización de compilación, linter y tests en cada Pull Request |
| **Control de versiones** | Git + GitHub (Monorepo) | Modelo trunk-based/GitFlow simplificado con branch protection en `main` |
| **Plataforma Cloud (PaaS)** | Render / Railway (Free tier) | Despliegue de la API y contenedor web con HTTPS automático |
| **Monitoreo y observabilidad** | Spring Boot Actuator + UptimeRobot | Endpoints `/health` y `/metrics`, y monitoreo de disponibilidad externa |

---

## 6. Cuadro de Decisiones Arquitectónicas

| Decisión | Alternativa descartada | Justificación técnica |
|---|---|---|
| **Spring Boot 3.4.x (Java 21 LTS)** | Spring Boot 4.x / Spring 7 | Versiones futuras no consolidadas; Spring Boot 3.4 con Java 21 es el estándar corporativo robusto |
| **Angular 19+ Standalone (TypeScript)** | React / Vue / JavaScript puro | Arquitectura empresarial completa y cohesiva, soporte nativo de Standalone Components, reactividad fina con Signals, optimización diferida con bloques `@defer` y presupuestos de bundle integrados en `angular.json` |
| **PostgreSQL + PostGIS** | MySQL / Mongo / Postgres plano | El requerimiento RF02 exige calcular distancias geoespaciales con precisión y performance mediante índices `GiST` |
| **Flyway Migrations** | Scripts manuales en pgAdmin | Permite reproducibilidad en CI/CD y despliegues sin intervención manual sobre el esquema |
| **PaaS Gratuito (Render/Railway)** | VPS manual sin automatizar | Menor overhead operativo, aprovisionamiento ágil y soporte nativo de variables de entorno y HTTPS |
