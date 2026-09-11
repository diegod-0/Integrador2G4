# Actividad de Laboratorio 3: Gestión Cuantitativa de Riesgos (PMBOK / ISO 31000)
**Curso Integrador II: Software (100000S12F) — Ciclo 2026-2**  
**Proyecto:** RescueLink — Sistema de información web para la coordinación, seguimiento espacial y gestión de adopciones de animales en riesgo  
**Estudiante:** Pedro Cueto  

---

## 1. Estructura de Desglose de Riesgos (RBS)
Se estructuraron los eventos inciertos del proyecto bajo una jerarquía cuantitativa en tres categorías:
* **1.0 Riesgos Técnicos:**
  * 1.1 Bloqueo o denegación de permisos en la Geolocation API del navegador.
  * 1.2 Regresión de rendimiento web WPO (Lighthouse Mobile < 85 puntos).
  * 1.3 Desbordamiento horizontal en pantallas móviles compactas de 320 px.
* **2.0 Riesgos Externos:**
  * 2.1 Conectividad intermitente en la vía pública (redes móviles 3G/4G degradadas).
  * 2.2 Carga de fotografías de alta resolución sin procesar que saturen memoria y datos.
* **3.0 Riesgos de Gestión y Proyecto:**
  * 3.1 Incumplimiento de fechas no prorrogables del calendario académico.
  * 3.2 Desalineación en contratos de esquemas mock frente a los futuros DTOs de Spring Boot.

---

## 2. Escalas Cuantitativas y Umbrales de Severidad
Se establecieron escalas discretas del 1 al 5 para medir la Probabilidad ($P$) y el Impacto ($I$):
* **Probabilidad ($P$):** 1 (Muy Baja, <10%), 2 (Baja, 10-30%), 3 (Media, 31-50%), 4 (Alta, 51-70%), 5 (Muy Alta, >70%).
* **Impacto ($I$):** 1 (Insignificante), 2 (Menor), 3 (Moderado), 4 (Mayor), 5 (Crítico).
* **Umbrales de Severidad ($P \times I$):**
  * **1 a 5 (Bajo - Verde):** Aceptación informada y monitoreo regular.
  * **6 a 11 (Moderado - Amarillo):** Mitigación preventiva estándar.
  * **12 a 19 (Alto - Naranja):** Plan de respuesta activo y revisión periódica en cada Daily.
  * **20 a 25 (Crítico - Rojo):** Acción inmediata; bloqueador de entrega o pase a producción.

---

## 3. Formulación Causa-Evento-Impacto y Mapa de Calor (5x5)
Cada riesgo se formuló bajo el estándar formal: *"Debido a [Causa], podría ocurrir [Evento], lo que generaría [Impacto]"*:

| ID | Riesgo Formulado (Causa $\rightarrow$ Evento $\rightarrow$ Impacto) | $P$ | $I$ | $P \times I$ | Nivel |
|:---:|---|:---:|:---:|:---:|:---:|
| **RSK01** | *Debido a* restricciones de seguridad del navegador, *podría ocurrir* la denegación de la Geolocation API, *lo que generaría* imposibilidad de georreferenciar el rescate en calle. | 4 | 4 | **16** | **Alto** |
| **RSK02** | *Debido a* fotos de alta resolución sin procesar, *podría ocurrir* saturación de datos y memoria, *lo que generaría* demoras y fallos en el envío del reporte. | 4 | 3 | **12** | **Alto** |
| **RSK03** | *Debido a* librerías de terceros descontroladas, *podría ocurrir* que el bundle inicial supere los 300 kB, *lo que generaría* una caída de Lighthouse Mobile bajo 85 puntos. | 3 | 4 | **12** | **Alto** |
| **RSK04** | *Debido a* discrepancias de esquemas entre mocks y el futuro backend, *podría ocurrir* incompatibilidad en Unidad 2, *lo que generaría* retrabajo de refactorización en componentes. | 3 | 5 | **15** | **Alto** |
| **RSK05** | *Debido a* mala recepción celular en campo, *podría ocurrir* un timeout en la petición, *lo que generaría* reportes perdidos y frustración del ciudadano. | 3 | 3 | **9** | **Moderado** |
| **RSK06** | *Debido a* estilos CSS fijos, *podría ocurrir* scroll horizontal en 320 px, *lo que generaría* penalización en la rúbrica de evaluación docente. | 2 | 4 | **8** | **Moderado** |

---

## 4. Estrategias de Respuesta, Triggers Medibles e Issues en GitHub
Para mitigar y controlar los riesgos críticos y altos, se definieron disparadores cuantitativos e issues en GitHub:
* **RSK01 (Fallo de Geolocalización):**
  * *Estrategia:* Mitigar.
  * *Disparador (Trigger):* Error `PERMISSION_DENIED` capturado por el servicio de geolocalización.
  * *Plan de Mitigación / Contingencia:* Despliegue automático de mapa interactivo con pin manual arrastrable; si el mapa falla, permitir selección por distrito y punto de referencia.
  * *Issue en GitHub:* `#issue-12-fallback-gps`.
* **RSK02 (Sobrecarga de Fotografías):**
  * *Estrategia:* Mitigar.
  * *Disparador (Trigger):* Archivo de imagen seleccionado superior a 2 MB o formato distinto a WebP/JPG.
  * *Plan de Mitigación / Contingencia:* Redimensión y compresión automática en cliente mediante `<canvas>` oculto antes de enviar (máx 1200px, calidad 0.8). Rechazo directo de archivos mayores a 5 MB con mensaje amigable.
  * *Issue en GitHub:* `#issue-15-client-compress`.
* **RSK03 (Regresión de Bundle y Caída WPO):**
  * *Estrategia:* Evitar.
  * *Disparador (Trigger):* Bundle inicial compilado mayor a 350 kB en `ng build`.
  * *Plan de Mitigación / Contingencia:* Carga diferida declarativa con bloques `@defer` en modales y directivas estrictas de `budgets` en `angular.json`. Reemplazo de librerías pesadas por SVG inline.
  * *Issue en GitHub:* `#issue-18-bundle-budget`.
* **RSK04 (Incompatibilidad de Contratos DTO):**
  * *Estrategia:* Evitar.
  * *Disparador (Trigger):* Modificación de interfaces TypeScript sin validación de arquitectura.
  * *Plan de Mitigación / Contingencia:* Especificación formal del contrato JSON bajo estándar OpenAPI 3.0 antes de codificar mocks, asegurando consistencia con Spring Boot.
  * *Issue en GitHub:* `#issue-22-dto-contract`.
