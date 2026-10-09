# Actividad de Laboratorio 7: Seguridad de la Información, Autenticación JWT y Defensas de Borde
**Curso Integrador II: Software (100000S12F) — Ciclo 2026-2**  
**Proyecto:** RescueLink — Sistema de información web para la coordinación, seguimiento espacial y gestión de adopciones de animales en riesgo  
**Estudiante:** Anghelo Mendoza  

---

## 1. Modelado de Amenazas STRIDE y Matriz de Seguridad
Evaluación estructurada de ciberseguridad sobre los activos de la plataforma:
* **Activos Críticos:** Credenciales de administradores y rescatistas, tokens JWT en tránsito, números de WhatsApp y nombres de reportantes, telemetría GPS en tiempo real y autenticidad del Certificado Digital de Adopción.
* **Matriz STRIDE y Evaluación DREAD:**
  - *Spoofing:* Suplantación de identidad de albergues mitigada mediante firma HMAC-SHA256 con claves robustas de 256 bits.
  - *Tampering:* Alteración ilegítima de estados de rescate prevenida con validaciones en máquina de estados y RBAC.
  - *Repudiation:* Repudio de triajes clínicos mitigado por Soft-Delete obligatorio y campos de autoría inmutables `created_by`.
  - *Information Disclosure:* Fuga de datos de contacto evitada limitando atributos en DTOs y rate limiting en catálogo.
  - *Denial of Service:* Inundación maliciosa de reportes neutralizada por limitación de tasa por IP y filtros de proximidad.
  - *Elevation of Privilege:* Manipulación de roles bloqueada mediante `@PreAuthorize` en controladores y validación estricta de Claims.

---

## 2. Autenticación, Autorización y Seguridad de la API (Spring Security 6 & JWT)
Configuración de la capa de seguridad en Spring Boot:
* **Gestión de Secretos:** Eliminación de credenciales hardcodeadas en código fuente; uso exclusivo de variables de entorno y archivo `.env` excluido del repositorio.
* **Hashing Adaptativo de Contraseñas:** Empleo de `DelegatingPasswordEncoder` con algoritmo predeterminado `BCryptPasswordEncoder(12)`, asegurando resistencia contra ataques offline por fuerza bruta y diccionarios acelerados por hardware.
* **Ciclo de Vida Stateless de JWT:**
  - *Access Token (15 min):* Portador de roles e identidad, residiendo exclusivamente en la memoria RAM del navegador en un Signal Store (cero uso de `localStorage`).
  - *Refresh Token (7 días):* Emitido en una Cookie con banderas de máxima seguridad: `HttpOnly = true` (inmune a XSS), `Secure = true` (solo HTTPS), `SameSite = Strict` (inmune a CSRF) y `Path = /api/auth/refresh`. Se aplica rotación estricta en cada consumo.
* **Cadena de Filtros y RBAC:** Configuración de `SecurityFilterChain` separando rutas públicas (`/api/auth/**`, `/api/tickets/reportar`, `/api/animales/adopcion`, `/actuator/health`) de rutas operativas restringidas a `ROLE_ADMIN` y `ROLE_VOLUNTARIO`.

---

## 3. Defensas de Borde, Rate Limiting y Sesiones Seguras en Cliente
Protección perimetral y gestión de sesión en el cliente web:
* **Rate Limiting con Bucket4j:** Implementación del algoritmo Token Bucket en memoria: límite estricto de 5 peticiones/min por IP en `/api/auth/login` (mitigando fuerza bruta) y 10 peticiones/min en `/api/tickets/reportar`, respondiendo HTTP 429 con cabecera `Retry-After`.
* **Sesiones Seguras en Angular:** Servicio `AuthStore` reactivo con Signals para almacenar el token efímero en memoria volátil. Al refrescar la página (F5), se dispara una petición silenciosa contra `/api/auth/refresh` aprovechando la cookie `HttpOnly` para renovar la sesión sin fricción.
* **Interceptores y Guardias Funcionales:**
  - `authInterceptor`: Inyecta el encabezado `Authorization: Bearer <token>` y captura códigos HTTP 401 para intentar la renovación automática de credenciales antes de fallar.
  - `authGuard`: Función `CanActivateFn` que protege rutas de la intranet redirigiendo a `/login` si no existen permisos válidos.