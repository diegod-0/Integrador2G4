# Actividad de Laboratorio 6: Replicación de Base de Datos, Arquitectura Back-End en Capas y Concurrencia
**Curso Integrador II: Software (100000S12F) — Ciclo 2026-2**  
**Proyecto:** RescueLink — Sistema de información web para la coordinación, seguimiento espacial y gestión de adopciones de animales en riesgo  
**Estudiante:** Pedro Cueto  

---

## 1. Replicación y Administración Avanzada de Base de Datos
Para garantizar alta disponibilidad, tolerancia a fallos y escalabilidad de lectura masiva:
* **Topología Primary-Replica con Streaming Replication:** Configuración de un nodo Primary (puerto 5432) para transacciones DDL y DML de escritura, y un nodo Standby en modo `hot_standby = on` (puerto 5433) que consume continuamente el flujo de registros Write-Ahead Logging (WAL).
* **Parámetros de Replicación:** Configuración de `wal_level = replica`, `max_wal_senders = 5`, slots de replicación físicos y autenticación cifrada con `scram-sha-256` en `pg_hba.conf`.
* **Ruteo Dinámico en Spring Boot:** Implementación de `AbstractRoutingDataSource` para inspeccionar `TransactionSynchronizationManager.isCurrentTransactionReadOnly()`, dirigiendo automáticamente métodos con `@Transactional(readOnly = true)` hacia la réplica y transacciones mutables hacia el primario.
* **Políticas de Administración y Respaldo:** Respaldos lógicos automáticos diarios con `pg_dump -Fc`, archivado continuo de segmentos WAL para Point-In-Time-Recovery (PITR) con RPO $< 5\text{ min}$, y mantenimiento periódico con `VACUUM ANALYZE` y reindexación concurrente de árboles `GiST`.

---

## 2. Arquitectura Back-End en Capas y Patrones de Acceso a Datos (Spring Boot 3.4)
Construcción sobre Spring Boot 3.4 y Java 21 LTS siguiendo separación estricta de responsabilidades:
* **Capas Arquitectónicas:** Controladores desacoplados de persistencia (`@RestController`), servicios de dominio (`@Service`) orquestadores de máquinas de estado, repositorios JPA y transferencia exclusiva con DTOs inmutables basados en **Java Records**.
* **Puertos y Adaptadores (Hexagonal):** Abstracciones para almacenamiento de imágenes (`StoragePort`), notificaciones por correo y WebSocket (`NotificationPort`), y generación documental (`DocumentGeneratorPort`).
* **Proyecciones de Lectura Ligeras:** Empleo de Spring Data JPA Projections basadas en Records para el catálogo de adopción, evitando la transferencia y sobrecosto de relaciones perezosas de entidades completas ($N+1$).
* **Problem Details (RFC 7807/9457):** Manejador global `@RestControllerAdvice` que estandariza las respuestas de error en formato estructurado sin filtrar trazas internas del servidor ni del motor de base de datos.
* **Manejo Riguroso del Tiempo:** Almacenamiento en UTC (`TIMESTAMPTZ` / `Instant`) en el backend y base de datos, con conversión explícita a la zona horaria civil `America/Lima` (UTC-5) para contratos y certificados.

---

## 3. Control de Concurrencia, Transaccionalidad y Auditoría
Gobernanza transaccional de operaciones críticas:
* **Transacciones ACID:** Nivel de aislamiento `READ_COMMITTED` gestionado declarativamente mediante `@Transactional`.
* **Control de Concurrencia Pesimista:** Bloqueo exclusivo a nivel de motor mediante `@Lock(LockModeType.PESSIMISTIC_WRITE)` con cláusula SQL `SELECT ... FOR UPDATE` en `AnimalRepository`. Impide que dos administradores aprueben simultáneamente la adopción del mismo animal en solicitudes concurrentes.
* **Trazabilidad y Auditoría Transversal:** Implementación de la clase base `@MappedSuperclass AuditableEntity` con Spring Data JPA Auditing (`@EnableJpaAuditing`), registrando automáticamente `created_at`, `updated_at` y el usuario responsable `created_by` a través de `SecurityContextHolder`.