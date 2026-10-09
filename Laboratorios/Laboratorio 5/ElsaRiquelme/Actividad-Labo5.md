# Actividad de Laboratorio 5: Front-End Reactivo, Modelo Físico y Migraciones con Flyway
**Curso Integrador II: Software (100000S12F) — Ciclo 2026-2**  
**Proyecto:** RescueLink — Sistema de información web para la coordinación, seguimiento espacial y gestión de adopciones de animales en riesgo  
**Estudiante:** Elsa Riquelme  

---

## 1. Consolidación y Conexión del Front-End (Angular 19+ Standalone)
La capa cliente consolida una arquitectura modular desacoplada basada en componentes independientes sin `NgModule`:
* **Enrutamiento y Carga Diferida:** Se estructura el enrutador (`app.routes.ts`) con funciones `loadComponent` para optimizar la descarga de fragmentos JavaScript bajo demanda. Separa estrictamente la Cara Pública (`/reportar`, `/tracking/:codigo`, `/catalogo`, `/matchmaker`) de la Cara Privada protegida con guardias (`/admin/bandeja-rescates`, `/admin/triaje-clinico`).
* **Gestión de Estado Granular con Signals:** Implementación del patrón Store con reactividad nativa mediante `signal()`, señales derivadas computadas `computed()` y exposición de solo lectura `asReadonly()`. Esto elimina re-renderizados globales y sustituye el uso excesivo de `BehaviorSubject`.
* **Formularios Fuertemente Tipados:** Empleo de `NonNullableFormBuilder` con validaciones síncronas/asíncronas en el formulario de reporte ágil (expresiones regulares para WhatsApp E.164, validación de coordenadas $[-90, 90]$ y $[-180, 180]$, y restricción de archivos multimedia $< 5\text{ MB}$).
* **Sintaxis Moderna y Optimización de Vistas:** Uso de bloques de control `@if` y `@for (track)`, complementados con `@defer (on viewport)` y skeleton screens con `@placeholder (minimum 500ms)` para mitigar el Cumulative Layout Shift (CLS).
* **Inyección e Integración:** Inyección declarativa mediante la función nativa `inject()`, capa de datos simulados resiliente (*Mocking Layer*) con generador de fallas y configuración de proxy de desarrollo en `proxy.conf.json` hacia `http://localhost:8080`.

---

## 2. Diseño Físico, Reglas de Integridad y Calidad de Datos (PostgreSQL 17 + PostGIS)
El almacenamiento relacional implementa la Tercera Forma Normal (3FN) con soporte geoespacial geodésico SRID 4326 (WGS 84):
* **Esquema Relacional Normalizado:** Se definen las 7 tablas maestras y transaccionales con identificadores únicos subrogados `UUID`: `usuarios`, `albergues`, `tickets_rescate`, `animales`, `historial_clinico`, `solicitudes_adopcion` y `donaciones`.
* **Integridad Referencial y Restricciones de Dominio:** Restricciones `NOT NULL`, índices únicos sobre correos y códigos públicos (`TICK-XXXX`, hash QR de adopción), claves foráneas con `ON DELETE RESTRICT` en tablas clínicas e históricas, y restricciones `CHECK` semánticas para roles, estados de tickets, especies y pesos positivos.
* **Calidad de Datos y Soft-Delete:** Prohibición estricta de borrado físico (`DELETE`). Se implementa borrado lógico inmutable con `deleted_at TIMESTAMP WITH TIME ZONE NULL`, administrado a nivel de ORM mediante `@SQLDelete` y `@SQLRestriction("deleted_at IS NULL")`.

---

## 3. Indexación, Rendimiento y Migraciones Versionadas (Flyway)
La optimización del motor relacional responde directamente a los patrones de consulta de la plataforma:
* **Estrategia de Indexación Basada en Consultas:**
  - Índices espaciales `GiST` (`idx_albergues_ubicacion`, `idx_tickets_ubicacion`) para acelerar el cálculo de radios y distancias geodésicas.
  - Índices compuestos `B-Tree` (`idx_animales_catalogo` sobre `(estado, especie, deleted_at)`) para filtros rápidos del catálogo público.
  - Índices únicos `B-Tree` para búsqueda O(1) de tickets de seguimiento y verificación de certificados.
* **Control de Migraciones con Flyway:** Migraciones inmutables y versionadas en `db/migration/`: `V1__create_extensions_and_tables.sql`, `V2__create_indexes.sql` y `V3__seed_initial_data.sql`. Garantiza reproducibilidad en entornos locales, CI/CD y producción sin intervención manual en pgAdmin.
* **Consultas Geoespaciales Críticas:**
  - *Detección de Reportes Duplicados:* `ST_DWithin` geodésico con radio $\le 50\text{ m}$ y ventana temporal $\le 2\text{ h}$ sobre tickets activos.
  - *Asignación por Capacidad:* Consulta de proximidad con `ST_DistanceSphere` filtrando albergues cuya ocupación sea estrictamente menor a `capacidad_max`.