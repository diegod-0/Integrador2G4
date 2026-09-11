# 00. Visión General del Proyecto
**RescueLink — Sistema de información web para la coordinación, seguimiento espacial y gestión de adopciones de animales en riesgo**
**Curso Integrador II: Software (100000S12F) — Ciclo 2026-2 Agosto**

---

## 1. Naturaleza y Concepto del Sistema

RescueLink es una **plataforma web centralizada multi-actor** que conecta a la ciudadanía con la red de albergues de la ciudad. El sistema opera como un ecosistema unificado de **dos caras** complementarias, diseñado bajo principios de empatía, transparencia y excelencia en la experiencia de usuario (UX):

```
                              [ PLATAFORMA RESCUELINK ]
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 ▼                                               ▼
         [ CARA PÚBLICA ]                               [ CARA PRIVADA ]
    (Cualquier ciudadano entra)                    (Albergues y Voluntarios con login)
                 │                                               │
   ├── 1. Reporte de Emergencias (GPS + foto)     ├── 1. Bandeja de Rescates (Aceptar/Rechazar)
   │     • Reporte ágil sin login previo          │     • Control de capacidad y duplicados
   │     • Rescue Tracker en vivo con WebSockets  ├── 2. Ficha Clínica y Cuarentena de Animales
   ├── 2. Catálogo Unificado de Adopción          ├── 3. Evaluación Comparativa de Adoptantes
   │     • Filtros por especie y tamaño           │     • Selección de la familia más idónea
   │     • Ordenamiento GPS "Cerca de mí"         └── 4. Emisión de Certificado Digital con QR
   │     • Matchmaker (Test de Compatibilidad)          • Documento PDF oficial de tenencia
   ├── 3. Directorio / Mapa de Albergues
   │     • Perfiles y recepción de donaciones
   └── 4. Muro de "Finales Felices"
         • Casos de éxito (Antes vs. Después)
```

---

## 2. Análisis Empresarial (Problema y Solución)

* **Problema identificado:**
  - Descoordinación crítica entre ciudadanos y rescatistas; reportes duplicados o ignorados en redes sociales.
  - Incertidumbre angustiante para quien reporta un animal, al no saber si fue atendido o abandonado a su suerte.
  - Albergues sobrepasados que no comunican su capacidad en tiempo real ni gestionan adopciones rigurosas.
  - Falta de incentivos y transparencia que desmotiva la adopción responsable y las donaciones ciudadanas.
* **Solución RescueLink (Cadena de Valor Integral de Extremo a Extremo):**
  $$\text{Reporte Ágil (GPS)} \longrightarrow \text{Seguimiento en Vivo (Tracker)} \longrightarrow \text{Recuperación Clínica} \longrightarrow \text{Matchmaking y Adopción (Certificado QR)}$$

---

## 3. Políticas Operativas y Experiencia de Usuario Premium

Para elevar la solución al estándar de una plataforma comercial moderna, se incorporan cuatro pilares funcionales de alto valor:

### 3.1 El "Rescue Tracker" en Vivo (Transparencia Total)
* **Objetivo:** Eliminar la ansiedad del ciudadano que reporta una emergencia en la vía pública.
* **Operación:** Al enviar el reporte, el usuario recibe un código de seguimiento único (`/tracking/TICK-4829`) con una línea de tiempo reactiva que se actualiza en tiempo real vía WebSockets:
  $$\text{[Reportado]} \longrightarrow \text{[Albergue Asignado]} \longrightarrow \text{[Rescatista en Camino]} \longrightarrow \text{[En Refugio a Salvo]}$$

### 3.2 El "Matchmaker" de Adopción (Test de Compatibilidad en 3 Pasos)
* **Objetivo:** Evitar que los adoptantes se frustren ante un catálogo abrumador y promover adopciones exitosas y duraderas.
* **Operación:** Un asistente interactivo en la cabecera del catálogo evalúa:
  1. *Tipo de vivienda:* Departamento sin patio vs. Casa con jardín.
  2. *Disponibilidad de tiempo:* Rutina sedentaria vs. Trabajo fuera todo el día.
  3. *Nivel de energía buscado:* Tranquilo/compañero de sillón vs. Activo/para salir a correr.
  Al finalizar, el catálogo se filtra automáticamente mostrando las mascotas con mayor afinidad a su estilo de vida.

### 3.3 Certificado Digital Oficial de Adopción con Código QR (PDF)
* **Objetivo:** Brindar formalidad legal, sanitaria y administrativa al proceso de adopción.
* **Operación:** Al aprobarse la solicitud, el sistema genera automáticamente un **Certificado Oficial en formato PDF** descargable que incluye:
  - Ficha de identidad de la mascota (nombre, foto, especie, edad y fecha de rescate).
  - Historial consolidado de vacunación y desparasitación firmado digitalmente por el albergue.
  - **Código QR de Autenticidad:** Al ser escaneado con cualquier celular, redirige a una URL pública de verificación (`https://rescuelink.org/verificar/{codigo}`) certificando la validez del documento.

### 3.4 El Muro de "Finales Felices" (Evidencia Social: Antes vs. Después)
* **Objetivo:** Inspirar a la comunidad, fomentar donaciones y demostrar el impacto positivo del sistema.
* **Operación:** Sección interactiva en la página principal con un slider visual comparativo que muestra la foto del animal el día que fue rescatado de la calle frente a su foto actual disfrutando con su familia adoptante.

---

## 4. Políticas Operativas y Casos de Borde del Negocio

1. **Reporte Ágil sin Fricción:** El ciudadano reporta en menos de 30 segundos ingresando únicamente su **Nombre y WhatsApp** (sin contraseña obligatoria) para no desincentivar rescates urgentes.
2. **Detección de Duplicados:** Detección espacial-temporal en backend (radio de 50 metros y 2 horas). Si existe un ticket activo, se notifica al usuario para evitar despachos dobles.
3. **Control de Sobrecupo en Albergues:** PostGIS excluye automáticamente a los albergues cuya cantidad de animales activos iguale su `capacidad_max`, asignando la emergencia al refugio más cercano con cupos libres.
4. **Evaluación Comparativa de Adoptantes:** Múltiples postulantes pueden aplicar a una misma mascota. El albergue selecciona al más idóneo; al aprobarse, el animal pasa a `ADOPTADO` y las demás solicitudes se cierran automáticamente con notificación empática.

---

## 5. Actores del Sistema y Roles

| Actor | Rol en el Sistema | Responsabilidades y Flujo |
|---|---|---|
| **Ciudadano** | `ROLE_CIUDADANO` | Reporta emergencias con Nombre y WhatsApp, sigue el rescate en el Rescue Tracker, realiza el test Matchmaker, postula a adopciones y descarga su Certificado Digital |
| **Voluntario** | `ROLE_VOLUNTARIO` | Brazo operativo en campo. Recibe la alerta con GPS, contacta al reportante, actualiza el estado a `EN_CAMINO` y confirma el rescate hacia el refugio |
| **Administrador de Albergue** | `ROLE_ADMIN` | Controla cupos, gestiona tratamientos clínicos, publica animales en catálogo, dictamina solicitudes de adopción y emite certificados oficiales |

---

## 6. Alcance del Proyecto

**Dentro del Alcance (In-Scope):**
- Módulo público de reporte ágil (Nombre + WhatsApp) con captura GPS y foto.
- Detección espacial-temporal de casos duplicados (50 m / 2 h).
- Rescue Tracker en tiempo real con WebSockets (STOMP).
- Algoritmo de asignación con filtro de capacidad máxima en PostgreSQL + PostGIS.
- Intranet de operaciones: control de cuarentena y fichas médicas.
- Catálogo híbrido de adopción con ordenamiento GPS "Cerca de mí" y filtro por albergue.
- Asistente interactivo Matchmaker para compatibilidad de adopción.
- Generador de Certificados Oficiales de Adopción en PDF con código QR de verificación.
- Muro de "Finales Felices" con slider interactivo Antes/Después.
- Integración externa con Resend para correos transaccionales automáticos.

**Fuera del Alcance (Out-of-Scope para versión de curso):**
- Aplicación móvil nativa (foco en SPA web 100% responsive con WPO).
- Pasarela bancaria con dinero real (donaciones registradas simbólicamente).
- Ruteo vehicular multi-parada.

---

## 7. Estrategia de Repositorio

```
/frontend    -> SPA (Angular 19+ Standalone + TypeScript)
/backend     -> API RESTful (Spring Boot 3.4.x + Java 21 LTS)
/database    -> Migraciones Flyway inmutables (V1__init.sql, V2__indices.sql)
/docs        -> Artefactos PMBOK (Project Charter, WBS, RTM, Riesgos) y Figma
/.github     -> Workflows CI/CD (GitHub Actions)
```
