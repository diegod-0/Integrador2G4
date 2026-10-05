-- =============================================================================
-- RESCUELINK - R__create_spatial_views.sql
-- Avance de Proyecto Final 2 (APF2) - Migración Repetible (Repeatable Migration)
-- Vistas espaciales y analíticas de alta concurrencia
-- =============================================================================

-- =============================================================================
-- VISTA 1: Ocupación y Capacidad Disponible por Albergue
-- Utilizada por el algoritmo de asignación automatizada de emergencias
-- =============================================================================
CREATE OR REPLACE VIEW v_albergues_capacidad_disponible AS
SELECT 
    a.id AS albergue_id,
    a.nombre AS albergue_nombre,
    a.direccion,
    a.telefono,
    a.capacidad_max,
    COUNT(an.id) FILTER (WHERE an.estado NOT IN ('ADOPTADO') AND an.deleted_at IS NULL) AS animales_alojados,
    (a.capacidad_max - COUNT(an.id) FILTER (WHERE an.estado NOT IN ('ADOPTADO') AND an.deleted_at IS NULL)) AS cupos_libres,
    ROUND(
        (COUNT(an.id) FILTER (WHERE an.estado NOT IN ('ADOPTADO') AND an.deleted_at IS NULL)::DECIMAL / a.capacidad_max) * 100, 
        2
    ) AS porcentaje_ocupacion,
    a.ubicacion
FROM albergues a
LEFT JOIN animales an ON an.albergue_id = a.id
WHERE a.deleted_at IS NULL
GROUP BY a.id, a.nombre, a.direccion, a.telefono, a.capacidad_max, a.ubicacion;

COMMENT ON VIEW v_albergues_capacidad_disponible IS 'Calcula en tiempo real la disponibilidad de cupos en albergues para derivación de rescates';

-- =============================================================================
-- VISTA 2: Catálogo Público Consolidado para la Cara Web
-- Optimiza la vista pública de adopciones evitando múltiples JOINs en cliente
-- =============================================================================
CREATE OR REPLACE VIEW v_catalogo_adopcion_publico AS
SELECT 
    an.id AS animal_id,
    an.nombre AS nombre_animal,
    an.especie,
    an.edad_estimada,
    an.peso,
    an.nivel_energia,
    an.apto_departamento,
    an.estado,
    an.foto_perfil_url,
    an.foto_antes_url,
    an.foto_despues_url,
    alb.id AS albergue_id,
    alb.nombre AS albergue_nombre,
    alb.direccion AS albergue_direccion,
    ST_X(alb.ubicacion::geometry) AS longitud,
    ST_Y(alb.ubicacion::geometry) AS latitud,
    an.created_at AS fecha_ingreso
FROM animales an
JOIN albergues alb ON an.albergue_id = alb.id
WHERE an.estado IN ('EN_CATALOGO', 'APTO_ADOPCION')
  AND an.deleted_at IS NULL
  AND alb.deleted_at IS NULL;

COMMENT ON VIEW v_catalogo_adopcion_publico IS 'Vista plana de alto rendimiento para el catálogo público y filtros del Matchmaker';
