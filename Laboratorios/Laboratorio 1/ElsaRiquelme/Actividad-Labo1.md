# Actividad de Laboratorio 1: Planificación, Negocio y Gestión Ágil
**Curso Integrador II: Software (100000S12F) — Ciclo 2026-2**  
**Proyecto:** RescueLink — Sistema de información web para la coordinación, seguimiento espacial y gestión de adopciones de animales en riesgo  
**Estudiante:** Elsa Riquelme  

---

## 1. Organización del Equipo y Acuerdos de Trabajo
* **Integrantes del Equipo:** Diego Claros, Pedro Cueto, Anghelo Mendoza, Elsa Riquelme.
* **Canales de Comunicación:** Servidor de Discord categorizado (`#general`, `#desarrollo-frontend`) y reuniones virtuales por Google Meet.
* **Acuerdos de Respuesta (SLAs):** Máximo 3 horas para dudas bloqueantes de desarrollo y 12 horas para revisiones ordinarias.
* **Protocolo de Decisiones:** Búsqueda de consenso técnico en un máximo de 20 minutos; en caso de discrepancia, votación por mayoría simple (3 de 4 integrantes).

---

## 2. Diagnóstico del Proceso Actual (AS-IS)
* **Problemática Central:** El rescate y la adopción animal operan de forma descoordinada y desarticulada mediante publicaciones en redes sociales y mensajería informal.
* **Puntos Críticos de Fricción Identificados:**
  1. **Demoras Fatales:** Tiempos de respuesta de entre 4 y 24 horas; el 60% de los animales heridos fallece antes de recibir auxilio.
  2. **Duplicidad de Esfuerzos:** Más del 40% de salidas movilizan a dos o más rescatistas al mismo punto por publicaciones virales en grupos distintos.
  3. **Falta de Georreferenciación:** Referencias ambiguas provocan traslados en vano y pérdida de recursos.
  4. **Opacidad Total:** El ciudadano que reporta nunca sabe si el caso fue atendido o abandonado.
  5. **Adopciones a Ciegas:** Ausencia de filtros de afinidad provoca altas tasas de devolución y abandono recurrente.

---

## 3. Formulación de la Oportunidad y Delimitación del MVP
* **Análisis de Causa Raíz (5 Porqués):** Las organizaciones carecen de una herramienta operativa centralizada que gestione el ciclo de vida del rescate con geolocalización, trazabilidad pública y estados transparentes.
* **Propuesta de Valor (RescueLink):** Plataforma web accesible sin barreras de registro que permita reportar en menos de 30 segundos con GPS exacto, asigne alertas al refugio más cercano y ofrezca seguimiento en tiempo real (*Rescue Tracker*).
* **Alcance del MVP (Unidad 1 / APF1):**
  * **In-Scope:** Formulario ágil de reporte con foto y GPS, seguimiento reactivo por código de ticket, catálogo con switch *"Cerca de mí"*, asistente *Matchmaker* en 3 preguntas y manejo explícito de estados (`idle`, `loading`, `success`, `empty`, `error`).
  * **Out-of-Scope:** Pasarelas de pago reales, app móvil nativa y base de datos relacional concurrente (programada para la Unidad 2 con Spring Boot y PostGIS).
* **Marco Normativo:** Cumplimiento de la **Ley N° 30407** (Bienestar Animal) y **Ley N° 29733** (Protección de Datos Personales).

---

## 4. Requerimientos e Historias de Usuario (BDD / Gherkin)
* **Requerimientos Funcionales Clave:**
  * **RF01:** Reporte ciudadano de emergencias con foto y captura GPS en < 30 segundos.
  * **RF02:** Consulta del timeline de rescate en vivo mediante código único (*Rescue Tracker*).
  * **RF03:** Catálogo de adopción ordenado por cercanía física mediante el algoritmo de Haversine.
  * **RF04:** Asistente interactivo *Matchmaker* de compatibilidad en 3 preguntas.
* **Requerimientos No Funcionales:** Rendimiento móvil con LCP < 2.5s en 4G, accesibilidad WCAG 2.1 AA y diseño responsive sin scroll horizontal desde 320 px.
* **Reglas de Negocio:** RN01 (sin login obligatorio para reportar), RN02 (control de sobrecupo en albergues) y RN04 (alerta de posibles duplicados a < 50m y < 120 min).
* **Ejemplo BDD en Gherkin (HU01 - Reporte Ágil):**
  * *Dado que* el ciudadano ingresa a `/reportar`,
  * *Cuando* completa su nombre, WhatsApp válido de 9 dígitos, foto y acepta geolocalización GPS,
  * *Entonces* el sistema valida los datos, genera el ticket único y redirige al *Rescue Tracker*. Si deniega permisos de GPS, despliega un mapa interactivo con pin manual.

---

## 5. Product Backlog y Planificación Ágil (Scrum)
* **Backlog Integral del Semestre:** 16 Historias de Usuario (55 Story Points) organizadas en 4 épicas y priorizadas con MoSCoW.
* **Sprint 1 (Alcance APF1 - 21 Story Points):**
  * **HU01:** Reporte Ágil con Captura GPS y Foto (8 SP) — *Must Have*.
  * **HU02:** Rescue Tracker de Seguimiento en Vivo (5 SP) — *Must Have*.
  * **HU03:** Catálogo con Ordenamiento "Cerca de mí" (5 SP) — *Must Have*.
  * **HU04:** Matchmaker de Compatibilidad en 3 Pasos (3 SP) — *Should Have*.
* **Criterios de Preparación y Calidad:**
  * **Definition of Ready (DoR):** Cumplimiento de criterios INVEST, estimación en Story Points y mínimo 3 escenarios Gherkin verificables.
  * **Definition of Done (DoD):** Código TypeScript estricto, estados explícitos de UI, responsive desde 320 px, accesibilidad WCAG 2.1 AA, build de producción limpio (`ng build`), pruebas unitarias exitosas (`ng test`) y Pull Request aprobado por un par.

---

## 6. Cronograma de Hitos y Trazabilidad
* **Cronograma de Hitos (Semanas 1 a 18):**
  * **Hito 1 (Semana 5 - 20%):** APF1 — Diagnóstico AS-IS, MVP, Prototipo Figma y Primer Incremento Front-End.
  * **Hito 2 (Semana 9 - 20%):** APF2 — Base de datos relacional PostGIS, API Spring Boot 3.4 y Despliegue Cloud v1.
  * **Hito 3 (Semana 13 - 20%):** APF3 — Integración de correo transaccional, pruebas de usabilidad SUS y Despliegue v2.
  * **Hito 4 (Semana 18 - 40%):** PROY — Pruebas de estrés JMeter, resiliencia RTO/RPO y Sustentación Final.
* **Matriz de Trazabilidad:** Cada necesidad del diagnóstico AS-IS está directamente enlazada a un Requerimiento Funcional, una Historia BDD, tareas técnicas y criterios de verificación DoD, garantizando que no existan componentes huérfanos.
