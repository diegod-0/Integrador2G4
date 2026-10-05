-- =============================================================================
-- RESCUELINK - V1__create_extensions_and_tables.sql
-- Avance de Proyecto Final 2 (APF2) - Unidad 2 (Semanas 6-9)
-- Esquema relacional normalizado en 3FN con soporte espacial PostGIS (SRID 4326)
-- =============================================================================

-- 1. Habilitación de extensiones necesarias
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- TABLA: usuarios
-- Gestión de identidades, credenciales y roles para control RBAC
-- =============================================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ROLE_CIUDADANO', 'ROLE_VOLUNTARIO', 'ROLE_ADMIN')),
    telefono VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE usuarios IS 'Almacena usuarios autenticables del sistema con soporte RBAC y borrado lógico';
COMMENT ON COLUMN usuarios.password_hash IS 'Hash BCrypt (costo computacional 12)';
COMMENT ON COLUMN usuarios.deleted_at IS 'Marca temporal de soft-delete; NULL indica registro activo';

-- =============================================================================
-- TABLA: albergues
-- Refugios y centros de rescate con geolocalización de precisión
-- =============================================================================
CREATE TABLE IF NOT EXISTS albergues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(120) NOT NULL UNIQUE,
    direccion VARCHAR(200) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    capacidad_max INTEGER NOT NULL CHECK (capacidad_max > 0),
    ubicacion GEOMETRY(Point, 4326) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE albergues IS 'Albergues y refugios asociados a la red RescueLink con ubicación geográfica';
COMMENT ON COLUMN albergues.capacidad_max IS 'Capacidad máxima de animales albergables en simultáneo';
COMMENT ON COLUMN albergues.ubicacion IS 'Coordenadas WGS 84 (Point, SRID 4326: Longitud, Latitud)';

-- =============================================================================
-- TABLA: tickets_rescate
-- Reportes ágiles ciudadanos de animales en riesgo y máquina de estados del tracker
-- =============================================================================
CREATE TABLE IF NOT EXISTS tickets_rescate (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_tracking VARCHAR(30) NOT NULL UNIQUE,
    nombre_reportante VARCHAR(100) NOT NULL,
    whatsapp_reportante VARCHAR(25) NOT NULL,
    albergue_id UUID REFERENCES albergues(id),
    voluntario_id UUID REFERENCES usuarios(id),
    descripcion TEXT NOT NULL,
    gravedad VARCHAR(20) NOT NULL CHECK (gravedad IN ('LEVE', 'MODERADA', 'CRITICA')),
    estado VARCHAR(25) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'ASIGNADO', 'EN_CAMINO', 'RESCATADO', 'NO_LOCALIZADO', 'RECHAZADO')),
    ubicacion GEOMETRY(Point, 4326) NOT NULL,
    foto_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE tickets_rescate IS 'Emergencias reportadas por la ciudadanía para el Rescue Tracker en vivo';
COMMENT ON COLUMN tickets_rescate.codigo_tracking IS 'Código alfanumérico público de trazabilidad (ej. TICK-4829)';
COMMENT ON COLUMN tickets_rescate.estado IS 'Máquina de estados estricta del flujo operativo de rescate';

-- =============================================================================
-- TABLA: animales
-- Animales tutelados en albergues, historial de rescate y estado de adopción
-- =============================================================================
CREATE TABLE IF NOT EXISTS animales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID UNIQUE REFERENCES tickets_rescate(id),
    albergue_id UUID NOT NULL REFERENCES albergues(id),
    nombre VARCHAR(50) NOT NULL,
    especie VARCHAR(20) NOT NULL CHECK (especie IN ('CANINO', 'FELINO', 'OTRO')),
    edad_estimada VARCHAR(30) NOT NULL,
    peso DECIMAL(5,2) NOT NULL CHECK (peso > 0),
    nivel_energia VARCHAR(20) NOT NULL CHECK (nivel_energia IN ('BAJO', 'MEDIO', 'ALTO')),
    apto_departamento BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(25) NOT NULL DEFAULT 'CUARENTENA' CHECK (estado IN ('CUARENTENA', 'EN_TRATAMIENTO', 'APTO_ADOPCION', 'EN_CATALOGO', 'EN_EVALUACION', 'ADOPTADO')),
    foto_perfil_url VARCHAR(255) NOT NULL,
    foto_antes_url VARCHAR(255),
    foto_despues_url VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE animales IS 'Expediente de animales bajo custodia de los albergues para catálogo y adopción';
COMMENT ON COLUMN animales.ticket_id IS 'Enlace opcional al ticket de rescate origen que dio paso al ingreso';
COMMENT ON COLUMN animales.apto_departamento IS 'Parámetro clave para el algoritmo Matchmaker de compatibilidad habitacional';

-- =============================================================================
-- TABLA: historial_clinico
-- Registro inmutable de triajes veterinarios, diagnósticos y tratamientos
-- =============================================================================
CREATE TABLE IF NOT EXISTS historial_clinico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id UUID NOT NULL REFERENCES animales(id) ON DELETE RESTRICT,
    veterinario_id UUID NOT NULL REFERENCES usuarios(id),
    diagnostico TEXT NOT NULL,
    tratamiento TEXT NOT NULL,
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE historial_clinico IS 'Atenciones veterinarias inmutables asociadas a cada animal con restricción de borrado';
COMMENT ON COLUMN historial_clinico.animal_id IS 'Referencia al animal atendido (ON DELETE RESTRICT previene desamparo de historial)';

-- =============================================================================
-- TABLA: solicitudes_adopcion
-- Postulaciones de ciudadanos a adopción y emisión de certificado oficial
-- =============================================================================
CREATE TABLE IF NOT EXISTS solicitudes_adopcion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id UUID NOT NULL REFERENCES animales(id) ON DELETE RESTRICT,
    adoptante_id UUID NOT NULL REFERENCES usuarios(id),
    codigo_certificado_qr VARCHAR(100) UNIQUE,
    estado VARCHAR(30) NOT NULL DEFAULT 'RECIBIDA' CHECK (estado IN ('RECIBIDA', 'EN_REVISION', 'APROBADA', 'RECHAZADA', 'CERRADA_POR_OTRA_ADOPCION')),
    tipo_vivienda VARCHAR(50) NOT NULL,
    tiene_otras_mascotas BOOLEAN NOT NULL DEFAULT FALSE,
    motivo TEXT NOT NULL,
    fecha_solicitud TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE solicitudes_adopcion IS 'Solicitudes de adopción sujetas a dictamen y control de concurrencia pesimista';
COMMENT ON COLUMN solicitudes_adopcion.codigo_certificado_qr IS 'Hash criptográfico para validación pública mediante código QR';

-- =============================================================================
-- TABLA: donaciones
-- Registro de donaciones monetarias y en especie destinadas a los albergues
-- =============================================================================
CREATE TABLE IF NOT EXISTS donaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    albergue_id UUID NOT NULL REFERENCES albergues(id),
    donante_id UUID REFERENCES usuarios(id),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('MONETARIA', 'ALIMENTO', 'MEDICINAS', 'ACCESORIOS')),
    monto_estimado DECIMAL(10,2) NOT NULL CHECK (monto_estimado >= 0),
    descripcion TEXT NOT NULL,
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE donaciones IS 'Registro transparente de aportes económicos o en bienes materiales a los albergues';
