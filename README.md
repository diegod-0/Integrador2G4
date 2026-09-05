# Repositorio Académico — Curso Integrador II: Sistemas - Software

[![UTP](https://img.shields.io/badge/Universidad-UTP-red?style=for-the-badge)](https://www.utp.edu.pe/)
[![Curso](https://img.shields.io/badge/Curso-Integrador_II:_Software-0A66C2?style=for-the-badge)](https://www.utp.edu.pe/)
[![Ciclo](https://img.shields.io/badge/Ciclo-2026--I-green?style=for-the-badge)]()
[![Metodología](https://img.shields.io/badge/Metodología-Scrum_/_Agile-orange?style=for-the-badge)]()

---

## 👥 1. Equipo de Desarrollo (Grupo de Trabajo)

| Integrante | Código | Rol Principal en el Proyecto | Responsabilidad Técnica |
|---|:---:|---|---|
| **Diego Claros** | — | **Scrum Master / Lead Architect** | Core Setup, Arquitectura Base & Reporte Ágil GPS (HU01) |
| **Pedro Cueto** | — | **Product Owner Simulado** | Rescue Tracker en Tiempo Real con WebSockets (HU02) |
| **Anghelo Mendoza** | — | **Business Analyst** | Catálogo de Adopción con Proximidad Espacial PostGIS (HU03) |
| **Elsa Riquelme** | — | **QA Lead / UX Developer** | Asistente de Compatibilidad Matchmaker & Testing Integral (HU04) |

---

## 📁 2. Estructura del Repositorio (Monorepo)

El repositorio está organizado de forma modular para concentrar tanto las actividades académicas continuas (laboratorios y sesiones) como el desarrollo del proyecto integrador final:

```text
IntegradorMDS/
├── .gitignore                         # Reglas de exclusión de Git y archivos privados
├── README.md                          # Este documento (README maestro del curso)
│
├── Laboratorios/                      # Prácticas y entregables de laboratorio
│   ├── Laboratorio 1/                 # [DiegoClaros, PedroCueto, AngheloMendoza, ElsaRiquelme]
│   ├── Laboratorio 2/                 # [DiegoClaros, PedroCueto, AngheloMendoza, ElsaRiquelme]
│   ├── Laboratorio 3/                 # [DiegoClaros, PedroCueto, AngheloMendoza, ElsaRiquelme]
│   └── Laboratorio 4/                 # [DiegoClaros, PedroCueto, AngheloMendoza, ElsaRiquelme]
│
├── Sesiones/                          # Ejercicios y talleres aplicados en clase
│   ├── Sesion1/                       # [DiegoClaros, PedroCueto, AngheloMendoza, ElsaRiquelme]
│   ├── Sesion2/                       # [DiegoClaros, PedroCueto, AngheloMendoza, ElsaRiquelme]
│   ├── Sesion3/                       # [DiegoClaros, PedroCueto, AngheloMendoza, ElsaRiquelme]
│   └── Sesion4/                       # [DiegoClaros, PedroCueto, AngheloMendoza, ElsaRiquelme]
│
└── ProyectoFinal/                     # Módulo central del Proyecto Integrador: RescueLink
    ├── README.md                      # Documentación técnica completa de RescueLink
    ├── arquitectura.md                # Arquitectura Hexagonal, DTOs y Máquina de Estados
    ├── stack-tecnologico.md           # Justificación tecnológica (Angular 19, Spring Boot, PostGIS)
    ├── vision-general-proyecto.md     # Visión de negocio, problemática y modelo operativo
    └── HISTORIAS-DE-USUARIO-POR-INTEGRANTE.md # 16 Historias de Usuario con BDD (Gherkin)
```

---

## 🐾 3. Proyecto Integrador: RescueLink

**RescueLink** es una plataforma web centralizada multi-actor diseñada para conectar a la ciudadanía con la red de albergues y refugios de animales de la ciudad. Su propósito es erradicar la fragmentación del rescate animal y la desinformación en redes sociales mediante un flujo continuo y auditable:

$$\text{Reporte Callejero Ágil (GPS)} \longrightarrow \text{Seguimiento en Vivo (Tracker)} \longrightarrow \text{Recuperación Clínica} \longrightarrow \text{Matchmaking y Adopción (Certificado QR)}$$

### Acceso a la Documentación Técnica de RescueLink:
* 📖 [**README Técnico de RescueLink**](./ProyectoFinal/README.md)
* 🏛️ [**Especificación Arquitectónica**](./ProyectoFinal/arquitectura.md)
* ⚙️ [**Stack Tecnológico y Justificación**](./ProyectoFinal/stack-tecnologico.md)
* 🎯 [**Visión General del Negocio**](./ProyectoFinal/vision-general-proyecto.md)
* 📋 [**Historias de Usuario por Integrante (BDD / Gherkin)**](./ProyectoFinal/HISTORIAS-DE-USUARIO-POR-INTEGRANTE.md)

---

## 🛠️ 4. Stack Tecnológico Global

* **Frontend:** Angular 19+ (Standalone Components, Signals, Vanilla CSS, Vite/esbuild)
* **Backend:** Spring Boot 3.4.x (Java 21 LTS, Spring Security 6.4, JWT, OpenAPI 3)
* **Persistencia Espacial:** PostgreSQL 17 + PostGIS (Migraciones inmutables con Flyway)
* **Gobernanza:** Git Flow, Conventional Commits y GitHub Projects (Kanban Board)

---

## 🚀 5. Convenciones de Desarrollo y Commits

Para mantener un historial limpio, auditable y profesional, todos los integrantes del equipo siguen estrictamente el estándar de [Conventional Commits](https://www.conventionalcommits.org/):

| Prefijo | Propósito | Ejemplo |
|---|---|---|
| `feat:` | Nueva funcionalidad | `feat(reporte): add gps geolocation service` |
| `fix:` | Corrección de bug | `fix(tracker): resolve websocket reconnection issue` |
| `docs:` | Cambios en documentación | `docs(readme): update team structure table` |
| `style:` | Formato, estilos CSS | `style(catalog): implement responsive grid layout` |
| `refactor:` | Refactorización de código sin cambio funcional | `refactor(core): optimize signal state management` |
| `test:` | Adición o corrección de pruebas | `test(auth): add unit tests for jwt interceptor` |
