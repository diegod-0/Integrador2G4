-- =============================================================================
-- RESCUELINK - V4__fix_seed_user_passwords.sql
-- Corrección de datos semilla (APF2)
--
-- CONTEXTO
--   V3__seed_initial_data.sql insertó hashes BCrypt con formato válido, pero que
--   NO correspondían a ninguna contraseña conocida. Resultado: ningún integrante
--   del equipo podía iniciar sesión (401 Credenciales Inválidas).
--
--   Contraseña unificada de las cuentas semilla: 'password123'
--
-- ESTRATEGIA
--   - NO se edita V3: ya está aplicada y Flyway valida checksums
--     (validate-on-migrate: true). Editarla rompería el arranque en todas las
--     máquinas que ya migraron.
--   - Esta migración normaliza POR EMAIL los hashes de las cuentas semilla, de
--     modo que cualquier base que haya corrido V1..V3 quede con credenciales
--     válidas sin depender de pasos manuales (auto-reparación).
--   - El hash fue generado con BCryptPasswordEncoder(12), el mismo encoder del
--     proyecto (SecurityConfig#passwordEncoder).
--
-- NOTA PARA EL EQUIPO
--   La tabla de credenciales de GUIA-DESARROLLO-BACKEND-EQUIPO.md debe
--   actualizarse: los emails reales son los @rescuelink.org listados abajo.
-- =============================================================================

UPDATE usuarios
SET password_hash = '$2a$12$QCfabWvHKp/Xy78TiMgcG.l4oPTqVGgqHUdU1IhLVj2NEQsBqJjIa'
WHERE email IN (
    'admin@rescuelink.org',
    'veterinario@rescuelink.org',
    'ciudadano@rescuelink.org',
    'voluntario@rescuelink.org'
);
