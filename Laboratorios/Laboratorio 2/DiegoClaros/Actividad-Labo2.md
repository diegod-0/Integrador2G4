# Actividad de Laboratorio 2: Arquitectura, Repositorio y Experiencia de Usuario (UX)
**Curso Integrador II: Software (100000S12F) — Ciclo 2026-2**  
**Proyecto:** RescueLink — Sistema de información web para la coordinación, seguimiento espacial y gestión de adopciones de animales en riesgo  
**Estudiante:** Diego Claros  

---

## 1. Selección y Justificación Tecnológica (ADRs)
Para sustentar las decisiones técnicas, se formalizaron tres Registros de Decisión Arquitectónica (ADR):
* **ADR-001 (Framework Front-End):** Se evaluó Angular Standalone frente a React 19 + Vite y Vue 3. Se seleccionó **Angular Standalone con TypeScript y Signals** por ofrecer arquitectura empresarial integrada out-of-the-box (inyección de dependencias, enrutamiento tipado) y soporte nativo de optimizaciones WPO (bloques declarativos `@defer` y presupuestos de bundle en `angular.json`).
* **ADR-002 (Manejo de Estado en Cliente):** Se adoptaron **Angular Signals nativas** (`signal()`, `computed()`) combinadas con tipos discriminados (`ViewState<T>`) para controlar estados explícitos de interfaz (`idle`, `loading`, `success`, `empty`, `error`) con reactividad fina y sin sobrecosto de librerías externas.
* **ADR-003 (Backend y Base de Datos para Unidad 2):** Se definió **Spring Boot 3.4 (Java 21 LTS)** con **PostgreSQL 17 y PostGIS** debido a la necesidad de indexación espacial `GiST` y cálculos geodésicos en base de datos (`ST_DWithin`) para detectar duplicados y filtrar albergues cercanos.

---

## 2. Configuración del Repositorio y Entorno Local
* **Estructura del Proyecto en GitHub (`Integrador2G4`):** El repositorio cuenta con directivas de consistencia de código (`.editorconfig`), exclusiones rigurosas de temporales y librerías (`.gitignore`) y estructura modular separando la documentación (`APF-Avances/`) y el cliente web (`frontend/`).
* **Entorno de Ejecución Local Reproducible:**
  * **Prerrequisitos:** Node.js (v20+ o v22+ LTS) y Angular CLI 19+/21+.
  * **Puesta en Marcha:** Clonación del repositorio, cambio a la rama `desarrollo-frontend`, instalación determinista (`npm install`) y ejecución local (`npm start` en `http://localhost:4200/`).
  * **Alcance Técnico:** En esta fase (APF1) el sistema opera de forma autónoma con datos sintéticos locales inmutables (`mocks/`), sin requerir bases de datos ni servicios externos.

---

## 3. Gestión Colaborativa en GitHub y Reglas de Protección
* **Tablero Kanban (GitHub Projects):** Flujo de trabajo organizado en 5 columnas: `Backlog`, `Ready (Sprint 1)`, `In Progress`, `In Review` y `Done`.
* **Estrategia de Ramificación:**
  * `main`: Rama protegida para entregables estables y versiones selladas (`v1.0.0-apf1`).
  * `desarrollo-frontend`: Rama base de integración continua para el primer incremento.
  * `feat/HU-0X-nombre`: Ramas específicas para el desarrollo de cada funcionalidad.
* **Reglas de Protección (Branch Protection Rules):** Prohibición de commits directos y `git push --force` en ramas principales; requerimiento obligatorio de Pull Request con paso exitoso del build (`ng build`) y aprobación de al menos un revisor par según la matriz de pairing.

---

## 4. Flujo de Usuario (UX) y Maquetación de Interfaces
* **User Flow:** Mapeo del recorrido del ciudadano contemplando el camino exitoso (*Happy Path*) y flujos de excepción (fallo de permisos GPS, detección de reportes duplicados a < 50m y filtros sin coincidencias en adopción).
* **Maquetación de Vistas Clave (Prototipo Figma):**
  1. **Reporte de Emergencia (`/reportar`):** Captura de fotografía, datos mínimos del ciudadano, chip de confirmación GPS y mini-mapa interactivo para ajuste manual.
  2. **Rescue Tracker (`/tracking/:codigo`):** Línea de tiempo reactiva con hitos del rescate (`EN_CAMINO`, `EN_CLINICA`) y botón para copiar código.
  3. **Catálogo de Adopción (`/adopcion`):** Grid responsive con switch *"Cerca de mí"* que calcula distancias en km mediante la fórmula de Haversine.
  4. **Asistente Matchmaker:** Wizard modal de 3 preguntas con carga diferida declarativa (`@defer`).

---

## 5. Biblioteca de Componentes, Accesibilidad y Validación
* **Matriz de Estados UI:** Cada componente de la biblioteca fue diseñado contemplando estados deterministas: *Default*, *Hover / Foco*, *Seleccionado*, *Cargando (Skeleton/Spinner)* y *Vacío / Error*.
* **Criterios de Accesibilidad (WCAG 2.1 AA):**
  * Relación de contraste de color superior a 5.2:1 en textos principales.
  * Indicador de foco visible (`outline: 3px solid #2563EB`) y enlaces de salto (*Skip Links*) para navegación por teclado.
  * Semántica HTML5 nativa (`<main>`, `<section>`, `<nav>`) con atributos `label` asociados.
* **Prueba de Usabilidad de Guerrilla:** Evaluación moderada con usuaria externa que permitió incorporar mejoras antes de codificar:
  * Inclusión de chip visual con confirmación de precisión GPS.
  * Botón de copiado rápido del ticket de seguimiento al portapapeles.
  * Aclaración visual de que las distancias del catálogo se calculan desde la ubicación actual.
