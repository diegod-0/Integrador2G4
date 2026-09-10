# Actividad de Laboratorio 4: Métricas y SLA
**Curso Integrador II: Software (100000S12F) — Ciclo 2026-2**  
**Proyecto:** RescueLink — Sistema Web de Rescate y Adopción Animal  
**Estudiante:** Anghelo Mendoza  

---

## 1. Separación Formal entre KPIs de Negocio y SLIs Técnicos
El proyecto distingue claramente las métricas operativas de las métricas de ingeniería técnica:

| Ámbito | Código | Nombre del Indicador | Método de Medición | Meta Cuantitativa |
|:---:|:---:|---|---|:---:|
| **Negocio** | KPI-01 | Tiempo medio de asignación de rescate | Timestamp(Asignado) - Timestamp(Reporte) | < 45 min en horario diurno |
| **Negocio** | KPI-02 | Tasa de detección de duplicados | Comparación de tickets duplicados y el total | Reducción >= 35% |
| **Negocio** | KPI-03 | Tasa de efectividad en adopción | Comparar la tasa de adopción con el total | >= 25% de postulaciones |
| **Técnico** | SLI-01 | Largest Contentful Paint (LCP) | Tiempo de render del elemento principal | < 2.5 s en móvil 4G |
| **Técnico** | SLI-02 | Interaction to Next Paint (INP) | Latencia ante clicks e interacciones UI | < 200 ms |
| **Técnico** | SLI-03 | Cumulative Layout Shift (CLS) | Puntuación de desplazamientos visuales | < 0.1 |
| **Técnico** | SLI-04 | Disponibilidad del Servicio Web | Identificar el tiempo total sin fallas | >= 99.0% |

---

## 2. Definición de Métricas Core Web Vitals
* **LCP (Largest Contentful Paint):** Mide el tiempo en que se renderiza el elemento de contenido más grande. Sirve para evaluar la percepción de velocidad de carga.
* **INP (Interaction to Next Paint):** Mide la latencia de todas las interacciones del usuario con la página. Sirve para evaluar la capacidad de respuesta de la interfaz.
* **CLS (Cumulative Layout Shift):** Mide la suma de todos los desplazamientos inesperados de diseño. Sirve para evaluar la estabilidad visual.

---

## 3. Definición de Objetivos (SLO) y Acuerdo de Nivel de Servicio (SLA)
* **SLO de Disponibilidad:** 99.0% de peticiones servidas exitosamente en una ventana móvil de 30 días.
* **Borrador Formal del Acuerdo de Nivel de Servicio (SLA):**
  > *"RescueLink se compromete ante los albergues afiliados y la comunidad a una disponibilidad mensual no menor al 99.0% y a un tiempo de carga inicial en dispositivos móviles menor a 2.5 segundos en el 95% de las sesiones. En caso de incumplimiento consecutivo, se activará el canal alterno vía WhatsApp Bot para asegurar la captura de rescates."*
