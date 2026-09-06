# Catálogo Maestro de Historias de Usuario por Integrante (Semestre 2026-2)
**Proyecto:** RescueLink — Plataforma Web de Rescate, Refugio y Adopción Animal  
**Marco de Trabajo:** Scrum Ágil / Gestión Integral en GitHub Projects  
**Cobertura Temporal:** 18 Semanas · 4 Hitos Evaluativos (APF1, APF2, APF3, PROY)  
**Curso:** Integrador II: Software (100000S12F) — Ciclo 2026-2 Agosto  

---

## 1. Matriz de Distribución Simétrica del Semestre (16 Historias de Usuario)

Para asegurar que los 4 integrantes mantengan una carga 100% equilibrada y auditable durante todo el ciclo académico, el Product Backlog se distribuye a razón de **1 Historia de Usuario por integrante en cada hito evaluativo**:

| Integrante | Rol en el Equipo | Hito 1: APF1 (Sem 1-5)<br>Front-End Increment | Hito 2: APF2 (Sem 6-9)<br>Back-End Spring Boot | Hito 3: APF3 (Sem 10-13)<br>Calidad e Interacción | Hito 4: PROY (Sem 14-18)<br>Operaciones & Cierre | Total HUs | Total Puntos |
|---|---|:---:|:---:|:---:|:---:|:---:|:---:|
| **Diego Claros** | Scrum Master / Lead Architect | **HU01** (8 SP) | **HU09** (3 SP) | **HU14** (3 SP) | **HU16** (2 SP) | **4 HUs** | **16 SP** |
| **Pedro Cueto** | Product Owner Simulado | **HU02** (5 SP) | **HU10** (4 SP) | **HU12** (2 SP) | **HU15** (3 SP) | **4 HUs** | **14 SP** |
| **Anghelo Mendoza** | Business Analyst | **HU03** (5 SP) | **HU05** (5 SP) | **HU06** (3 SP) | **HU13** (2 SP) | **4 HUs** | **15 SP** |
| **Elsa Riquelme** | QA Lead / UX Developer | **HU04** (3 SP + 2 QA) | **HU07** (2 SP) | **HU11** (3 SP) | **HU08** (2 SP) | **4 HUs** | **10 SP** (+ QA) |
| **TOTALES POR HITO** | | **21 SP (4 HUs)** | **14 SP (4 HUs)** | **11 SP (4 HUs)** | **9 SP (4 HUs)** | **16 HUs** | **55 SP** |

---

## 2. Historias Asignadas a Diego Claros (4 HUs)

### HU01: Reporte Ágil de Emergencias con Captura GPS y Foto
* **Hito:** APF1 (Sprint 1 · Sem 1-5) | **Estimación:** 8 Story Points | **Prioridad:** Must Have  
* **Rama Git:** `feat/HU-01-reporte-agil-gps` | **Destino:** `desarrollo-frontend` | **Revisora:** Elsa Riquelme  
* **Narrativa:** Como transeúnte que encuentra un animal herido en la calle, quiero reportarlo en < 30s capturando ubicación GPS automática y foto, para que los rescatistas acudan de inmediato sin pedirme registros engorrosos.  
* **Criterios Clave:** Happy path con geolocalización del navegador, validación de foto obligatoria y selector manual de contingencia ante permisos denegados.

### HU09: Autenticación Segura JWT y Gestión de Roles
* **Hito:** APF2 (Sprint 2 · Sem 6-9) | **Estimación:** 3 Story Points | **Prioridad:** Must Have  
* **Rama Git:** `feat/HU-09-auth-jwt-roles` | **Destino:** `desarrollo-backend` | **Revisora:** Elsa Riquelme  
* **Narrativa:** Como administrador o voluntario de albergue, quiero iniciar sesión con credenciales cifradas y recibir un token JWT, para acceder a las operaciones privadas según mi rol (`ROLE_ADMIN`, `ROLE_VOLUNTARIO`).  
* **Criterios Clave:** Cifrado con BCrypt, emisión de token Bearer con expiración de 2 horas y protección de rutas con Spring Security.

### HU14: Emisión de Certificado Oficial de Adopción en PDF con Código QR
* **Hito:** APF3 (Sprint 3 · Sem 10-13) | **Estimación:** 3 Story Points | **Prioridad:** Must Have  
* **Rama Git:** `feat/HU-14-certificado-pdf-qr` | **Destino:** `desarrollo-backend` | **Revisora:** Elsa Riquelme  
* **Narrativa:** Como administrador de albergue, quiero emitir un certificado formal de adopción en PDF con firma digital y código QR único, para otorgar validez legal y sanitaria al nuevo tenedor responsable.  
* **Criterios Clave:** Generación en backend con iText/OpenPDF, código QR con firma HMAC SHA-256 e inclusión de ficha médica consolidada.

### HU16: Portal Público de Verificación Criptográfica por QR
* **Hito:** PROY (Sprint 4 · Sem 14-18) | **Estimación:** 2 Story Points | **Prioridad:** Could Have  
* **Rama Git:** `feat/HU-16-verificacion-qr` | **Destino:** `desarrollo-frontend` | **Revisora:** Elsa Riquelme  
* **Narrativa:** Como autoridad fiscalizadora o ciudadano, quiero escanear el QR del certificado con la cámara de mi celular y acceder a `/verificar/:hash`, para certificar la autenticidad inmutable del rescate y la adopción.  
* **Criterios Clave:** Verificación criptográfica en línea, sello verde de validez oficial y bloqueo de certificados revocados o falsificados.

---

## 3. Historias Asignadas a Pedro Cueto (4 HUs)

### HU02: Rescue Tracker de Seguimiento en Vivo
* **Hito:** APF1 (Sprint 1 · Sem 1-5) | **Estimación:** 5 Story Points | **Prioridad:** Must Have  
* **Rama Git:** `feat/HU-02-rescue-tracker` | **Destino:** `desarrollo-frontend` | **Revisor:** Diego Claros  
* **Narrativa:** Como ciudadano que reportó una emergencia, quiero consultar mi código de ticket en una línea de tiempo reactiva, para saber con transparencia si un albergue ya fue asignado y cuándo el animal está a salvo.  
* **Criterios Clave:** Renderizado de estados (`EN_CAMINO`, `ATENDIDO`, `A_SALVO`), estado empty con ilustración amigable y alerta de reasignación por cupo.

### HU10: Bandeja Operativa de Rescates y Asignación de Refugios
* **Hito:** APF2 (Sprint 2 · Sem 6-9) | **Estimación:** 4 Story Points | **Prioridad:** Must Have  
* **Rama Git:** `feat/HU-10-bandeja-rescates` | **Destino:** `desarrollo-backend` | **Revisor:** Diego Claros  
* **Narrativa:** Como administrador de albergue, quiero recibir las alertas entrantes en un panel operativo ordenado por urgencia y distancia, para aceptar el caso o derivarlo automáticamente si el refugio está al límite.  
* **Criterios Clave:** Consulta espacial con PostGIS (`ST_DWithin`), bloqueo automático de albergues saturados y notificación en tiempo real.

### HU12: Control de Capacidad y Alertas de Sobrecupo
* **Hito:** APF3 (Sprint 3 · Sem 10-13) | **Estimación:** 2 Story Points | **Prioridad:** Should Have  
* **Rama Git:** `feat/HU-12-control-capacidad` | **Destino:** `desarrollo-backend` | **Revisor:** Diego Claros  
* **Narrativa:** Como gestor de albergue, quiero configurar la capacidad máxima de animales y recibir alertas al 90% de ocupación, para prevenir el hacinamiento y garantizar el bienestar animal según la Ley 30407.  
* **Criterios Clave:** Cálculo de porcentaje de ocupación en tiempo real y disparador de alerta preventiva al superar el umbral crítico.

### HU15: Despacho y Confirmación en Campo para Voluntarios
* **Hito:** PROY (Sprint 4 · Sem 14-18) | **Estimación:** 3 Story Points | **Prioridad:** Should Have  
* **Rama Git:** `feat/HU-15-despacho-voluntarios` | **Destino:** `desarrollo-frontend` | **Revisor:** Diego Claros  
* **Narrativa:** Como voluntario en ruta de rescate, quiero actualizar el estado del animal desde mi dispositivo móvil con un solo toque (`EN_CAMINO` $\rightarrow$ `RESCATADO`), para notificar instantáneamente al reportante y al albergue.  
* **Criterios Clave:** Botones de acción rápida con georreferenciación y actualización instantánea en el tracker del ciudadano.

---

## 4. Historias Asignadas a Anghelo Mendoza (4 HUs)

### HU03: Catálogo de Adopción Ordenado por Proximidad GPS
* **Hito:** APF1 (Sprint 1 · Sem 1-5) | **Estimación:** 5 Story Points | **Prioridad:** Must Have  
* **Rama Git:** `feat/HU-03-catalogo-cercania-gps` | **Destino:** `desarrollo-frontend` | **Revisor:** Pedro Cueto  
* **Narrativa:** Como persona interesada en adoptar, quiero ver los animales ordenados desde el albergue más cercano hasta el más lejano de mi ubicación, para visitarlo con facilidad y apoyar a mi comunidad.  
* **Criterios Clave:** Cálculo esférico con fórmula pura de Haversine, filtros combinados (especie, tamaño) y manejo de estado empty.

### HU05: Detección Preventiva de Duplicados en PostGIS
* **Hito:** APF2 (Sprint 2 · Sem 6-9) | **Estimación:** 5 Story Points | **Prioridad:** Should Have  
* **Rama Git:** `feat/HU-05-deteccion-duplicados` | **Destino:** `desarrollo-backend` | **Revisor:** Pedro Cueto  
* **Narrativa:** Como ciudadano que reporta en la calle, quiero recibir una alerta si otra persona reportó al mismo animal hace pocos minutos en mi misma cuadra, para evitar despachos dobles de los rescatistas.  
* **Criterios Clave:** Consulta geoespacial en PostgreSQL (`ST_DWithin` a radio de 50 metros y ventana temporal de 2 horas) y modal comparativo de confirmación.

### HU06: Postulación a Adopción con Formulario de Idoneidad
* **Hito:** APF3 (Sprint 3 · Sem 10-13) | **Estimación:** 3 Story Points | **Prioridad:** Must Have  
* **Rama Git:** `feat/HU-06-postulacion-adopcion` | **Destino:** `desarrollo-frontend` | **Revisor:** Pedro Cueto  
* **Narrativa:** Como adoptante que eligió una mascota, quiero llenar una solicitud formal indicando tipo de vivienda, presupuesto y compromiso de esterilización, para que el albergue evalúe mi postulación con rigor.  
* **Criterios Clave:** Validación de declaración jurada bajo Ley 30407, consentimiento de visitas periódicas y acuse de recibo por correo.

### HU13: Evaluación Comparativa y Dictamen de Postulantes
* **Hito:** PROY (Sprint 4 · Sem 14-18) | **Estimación:** 2 Story Points | **Prioridad:** Should Have  
* **Rama Git:** `feat/HU-13-dictamen-adopcion` | **Destino:** `desarrollo-backend` | **Revisor:** Pedro Cueto  
* **Narrativa:** Como administrador de albergue, quiero comparar a los postulantes de un mismo animal y seleccionar al más idóneo, para adjudicar la adopción y notificar empáticamente a los postulantes no seleccionados.  
* **Criterios Clave:** Cuadro comparativo de idoneidad, transición automática del animal a estado `ADOPTADO` y cierre de solicitudes en lote.

---

## 5. Historias Asignadas a Elsa Riquelme (4 HUs)

### HU04: Asistente Interactivo de Compatibilidad (Matchmaker)
* **Hito:** APF1 (Sprint 1 · Sem 1-5) | **Estimación:** 3 Story Points (+ 2 SP QA) | **Prioridad:** Should Have  
* **Rama Git:** `feat/HU-04-matchmaker-wizard` | **Destino:** `desarrollo-frontend` | **Revisor:** Anghelo Mendoza  
* **Narrativa:** Como adoptante primerizo, quiero responder un cuestionario de 3 preguntas en la cabecera del catálogo, para que el sistema me recomiende las mascotas más compatibles con mi hogar y rutina.  
* **Criterios Clave:** Carga diferida declarativa con `@defer (on interaction)`, algoritmo de afinidad porcentual [0-100] y navegación por teclado accesible.

### HU07: Directorio Geográfico y Perfiles de Albergues Aliados
* **Hito:** APF2 (Sprint 2 · Sem 6-9) | **Estimación:** 2 Story Points | **Prioridad:** Could Have  
* **Rama Git:** `feat/HU-07-directorio-albergues` | **Destino:** `desarrollo-frontend` | **Revisor:** Anghelo Mendoza  
* **Narrativa:** Como ciudadano, quiero consultar el directorio público de albergues con su ubicación, cupos actuales y canales oficiales de donación, para canalizar ayuda de forma segura y transparente.  
* **Criterios Clave:** Perfil institucional verificado, datos bancarios simbólicos y mapa de ubicación interactivo.

### HU11: Ficha Clínica, Historial Médico y Cuarentena
* **Hito:** APF3 (Sprint 3 · Sem 10-13) | **Estimación:** 3 Story Points | **Prioridad:** Must Have  
* **Rama Git:** `feat/HU-11-ficha-clinica-cuarentena` | **Destino:** `desarrollo-backend` | **Revisor:** Anghelo Mendoza  
* **Narrativa:** Como médico veterinario o encargado de albergue, quiero registrar tratamientos, vacunas y altas médicas de cada animal, para certificar su estado de salud antes de habilitarlo para adopción.  
* **Criterios Clave:** Registro inmutable de eventos clínicos, cálculo automático de días restantes de cuarentena y bloqueo de adopción si el animal no tiene alta médica.

### HU08: Muro de Finales Felices (Slider Antes / Después)
* **Hito:** PROY (Sprint 4 · Sem 14-18) | **Estimación:** 2 Story Points | **Prioridad:** Could Have  
* **Rama Git:** `feat/HU-08-muro-finales-felices` | **Destino:** `desarrollo-frontend` | **Revisor:** Anghelo Mendoza  
* **Narrativa:** Como visitante de la plataforma, quiero ver una galería interactiva con un slider visual que compare la foto del rescate frente a su foto actual con su familia adoptiva, para inspirarme a adoptar responsablemente.  
* **Criterios Clave:** Componente interactivo tipo cortina (*image comparison slider*), optimización de imágenes WebP y citas testimoniales de las familias.
