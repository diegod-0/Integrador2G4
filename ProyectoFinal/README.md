# RescueLink — Plataforma Web de Rescate, Refugio y Adopción Animal

[![Angular](https://img.shields.io/badge/Frontend-Angular_19+_Standalone-DD0031?logo=angular&logoColor=white)](https://angular.dev/)
[![TypeScript](https://img.shields.io/badge/Language-TypeScript_5.x-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring_Boot_3.4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Runtime-Java_21_LTS-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL_17_--_PostGIS-4169E1?logo=postgresql&logoColor=white)](https://postgis.net/)
[![UTP](https://img.shields.io/badge/Curso-Integrador_II:_Software-red)](https://www.utp.edu.pe/)

---

## 🐾 1. Visión General del Proyecto

**RescueLink** es una plataforma web centralizada multi-actor diseñada para conectar a la ciudadanía con la red de albergues y refugios de animales de la ciudad. El sistema erradica la fragmentación del rescate animal y la desinformación en redes sociales mediante un flujo continuo y auditable de cuatro fases:

$$\text{Reporte Callejero Ágil (GPS)} \longrightarrow \text{Seguimiento en Vivo (Tracker)} \longrightarrow \text{Recuperación Clínica} \longrightarrow \text{Matchmaking y Adopción (Certificado QR)}$$

### Arquitectura de Dos Caras
1. **Cara Pública (Ciudadanía):**
   - **Reporte Ágil de Emergencias:** Captura de foto, coordenadas GPS automáticas y contacto (Nombre + WhatsApp) en $< 30\text{ s}$ sin necesidad de crear contraseñas.
   - **Rescue Tracker en Vivo:** Seguimiento reactivo del estado del rescate en tiempo real vía WebSockets.
   - **Catálogo Unificado de Adopción:** Filtros avanzados, ordenamiento geodésico por proximidad (*"📍 Cerca de mí"*) y asistente de afinidad (*Matchmaker* en 3 pasos).
   - **Directorio de Albergues y Finales Felices:** Mapa interactivo de refugios y muro comparativo Antes vs. Después.
2. **Cara Privada (Albergues y Voluntarios):**
   - **Bandeja de Rescates y Despacho:** Algoritmo espacial PostGIS que asigna el caso al albergue con cupo más cercano y comisiona a voluntarios de campo.
   - **Gestión Clínica y Cuarentena:** Control de triaje, tratamientos y vacunas antes de habilitar la adopción.
   - **Evaluación Comparativa de Solicitudes:** Selección de la familia más idónea y emisión del **Certificado Digital de Adopción en PDF con código QR de verificación**.

---

## 👥 2. Estructura y Asignación del Equipo de Trabajo (Sprint 1)

Para garantizar una carga de desarrollo 100% equitativa, trazable y verificable en las auditorías de GitHub, el equipo distribuye sus responsabilidades de la siguiente manera:

| Integrante | Rol Principal | Módulo Funcional Asignado | Historia de Usuario | Rama de Trabajo Git | Revisor Obligatorio (PR) | Guía Individual de Desarrollo |
|---|---|---|:---:|:---:|:---:|:---:|
| **Diego Claros** | Scrum Master / Lead Architect | Core Setup, Layout Base & Reporte Ágil con GPS | **HU01** (8 SP) | `feat/HU-01-reporte-agil-gps` | Elsa Riquelme | [Ver Guía Técnica](./guia-desarrollo-diego-claros.md) |
| **Pedro Cueto** | Product Owner Simulado | Seguimiento en Vivo con Rescue Tracker | **HU02** (5 SP) | `feat/HU-02-rescue-tracker` | Diego Claros | [Ver Guía Técnica](./guia-desarrollo-pedro-cueto.md) |
| **Anghelo Mendoza** | Business Analyst | Catálogo de Adopción y Orden Geodésico | **HU03** (5 SP) | `feat/HU-03-catalogo-cercania-gps` | Pedro Cueto | [Ver Guía Técnica](./guia-desarrollo-anghelo-mendoza.md) |
| **Elsa Riquelme** | QA Lead / UX Developer | Asistente de Compatibilidad Matchmaker & QA | **HU04** (3 SP) + QA (2 SP) | `feat/HU-04-matchmaker-wizard` | Anghelo Mendoza | [Ver Guía Técnica](./guia-desarrollo-elsa-riquelme.md) |

> [!TIP]
> Para conocer la matriz de gobernanza, flujo de Pull Requests y reglas de contribución, consulta [ASIGNACION-RESPONSABILIDADES-EQUIPO.md](./ASIGNACION-RESPONSABILIDADES-EQUIPO.md).  
> Para consultar los criterios de aceptación en formato BDD (Gherkin) de cada historia, consulta [HISTORIAS-DE-USUARIO-POR-INTEGRANTE.md](./HISTORIAS-DE-USUARIO-POR-INTEGRANTE.md).

---

## 🧭 3. Mapa de Documentación Técnica y Académica

| Documento Maestro | Descripción y Propósito | Entregable del Curso |
|---|---|:---:|
| [`01-APF1-planificacion-frontend.md`](./01-APF1-planificacion-frontend.md) | **Memoria Técnica APF1 (13 capítulos):** Team Charter, AS-IS, Oportunidad MVP, Requerimientos, Scrum, ADRs, WBS, Riesgos PMBOK, Prototipos y WPO. | **Unidad 1 (Semana 5 — 20%)** |
| [`02-APF2-backend-despliegue.md`](./02-APF2-backend-despliegue.md) | **Diseño Back-End y Persistencia:** Modelo Físico 3FN, Flyway, PostGIS, Seguridad JWT, OWASP y Despliegue PaaS v1. | **Unidad 2 (Semana 9 — 20%)** |
| [`03-APF3-calidad-funcionalidad-compatibilidad-interaccion.md`](./03-APF3-calidad-funcionalidad-compatibilidad-interaccion.md) | **Calidad ISO 25010 (Parte 1):** V&V, Interoperabilidad API externa (Resend), Coexistencia y Usabilidad SUS. | **Unidad 3 (Semana 13 — 20%)** |
| [`04-PROY-rendimiento-confiabilidad-mantenimiento.md`](./04-PROY-rendimiento-confiabilidad-mantenimiento.md) | **Calidad ISO 25010 (Parte 2):** Pruebas de estrés JMeter, Resiliencia RTO/RPO, JaCoCo $>75\%$ y Sustentación Final. | **Unidad 4 (Semana 18 — 40%)** |
| [`arquitectura.md`](./arquitectura.md) | **Especificación Arquitectónica:** Puertos y Adaptadores (Hexagonal), Máquinas de Estado, DTOs y Soft-Delete. | Transversal |
| [`stack-tecnologico.md`](./stack-tecnologico.md) | **Justificación Tecnológica:** Spring Boot 3.4, Java 21 LTS, Angular 19+ Standalone, PostGIS y Docker. | Transversal |
| [`vision-general-proyecto.md`](./vision-general-proyecto.md) | **Filosofía del Negocio:** Caso Firulais, políticas operativas y catálogo híbrido. | Transversal |

---

## 🛠️ 4. Stack Tecnológico Principal

```
RescueLink Monorepo
├── /frontend    -> Angular 19+ (Standalone Components, Signals, Vanilla CSS, Vite/esbuild)
├── /backend     -> Spring Boot 3.4.x (Java 21 LTS, Spring Security 6.4, JWT, OpenAPI 3)
├── /database    -> PostgreSQL 17 + PostGIS (Migraciones inmutables con Flyway)
├── /docs        -> Diagramas BPMN, prototipos de alta fidelidad Figma y evidencias de calidad
└── /.github     -> Workflows automatizados de CI/CD con GitHub Actions
```

---

## 🚀 5. Flujo de Trabajo para el Equipo de Desarrollo (Git Workflow)

### Reglas de Oro de Contribución:
1. **Rama Base de Integración:** Todo el desarrollo del Front-End (APF1) se integra sobre la rama `desarrollo-frontend`.
2. **Prohibido Push Directo:** Nadie hace push a `main` ni a `desarrollo-frontend`.
3. **Flujo de Ramas:**
   ```bash
   # 1. Asegurar sincronización con la rama base
   git checkout desarrollo-frontend
   git pull origin desarrollo-frontend

   # 2. Crear rama temática personal según HU asignada
   git checkout -b feat/HU-0X-nombre-funcionalidad

   # 3. Desarrollar, comitear bajo Conventional Commits y subir
   git push -u origin feat/HU-0X-nombre-funcionalidad
   ```
4. **Pull Requests y Code Review:** Cada PR debe ser revisado y aprobado por el compañero asignado en la matriz antes de fusionarse.
5. **Formato de Commits:** Utilizar estrictamente [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat(reporte): add geolocation service with signals`
   - `test(tracker): add unit tests for timeline transitions`
   - `fix(catalog): resolve distance sorting rounding issue`

---

## 💻 6. Cómo Ejecutar el Entorno Local (Quick Start)

### Prerrequisitos:
- **Node.js:** Versión 20.x o 22.x LTS.
- **Angular CLI:** Versión 19.x (`npm install -g @angular/cli`).
- **Git:** Cliente configurado con nombre y correo institucional.

### Puesta en Marcha del Front-End:
```bash
# Clonar el repositorio
git clone https://github.com/diegod-0/Integrador2G4.git
cd Integrador2G4/ProyectoFinal

# Cambiar a la rama de integración front-end
git checkout desarrollo-frontend

# Entrar al directorio del cliente web
cd frontend

# Instalar dependencias
npm install

# Iniciar servidor de desarrollo en http://localhost:4200
npm start
```

---

*Proyecto desarrollado para el Curso Integrador II: Software (100000S12F) — Universidad Tecnológica del Perú (UTP).*
