-- =============================================================================
-- RESCUELINK - V2__create_indexes.sql
-- Avance de Proyecto Final 2 (APF2) - Unidad 2 (Semanas 6-9)
-- Estrategia de indexación basada en patrones de acceso: GiST y B-Tree Compuestos
-- =============================================================================

-- =============================================================================
-- 1. ÍNDICES ESPACIALES GiST (Generalized Search Tree)
-- Aceleran cálculos de geodistancia (ST_DWithin, ST_DistanceSphere)
-- =============================================================================

-- Acelera la asignación automatizada de emergencias al albergue más cercano
CREATE INDEX IF NOT EXISTS idx_albergues_ubicacion 
ON albergues USING GIST (ubicacion);

-- Acelera la detección espacial de reportes duplicados en radio de 50 metros
CREATE INDEX IF NOT EXISTS idx_tickets_ubicacion 
ON tickets_rescate USING GIST (ubicacion);

-- =============================================================================
-- 2. ÍNDICES B-TREE COMPUESTOS PARA CONSULTAS FRECUENTES
-- Optimizan filtros concurrentes del Catálogo de Adopción y Rescue Tracker
-- =============================================================================

-- Optimiza el catálogo público evitando table scans completos (Filtros por Estado + Especie + Soft-Delete)
CREATE INDEX IF NOT EXISTS idx_animales_catalogo 
ON animales (estado, especie, deleted_at);

-- Optimiza la consulta del Rescue Tracker y previene escaneo secuencial en tickets activos
CREATE INDEX IF NOT EXISTS idx_tickets_estado_created 
ON tickets_rescate (estado, created_at DESC) 
WHERE deleted_at IS NULL;

-- Optimiza la búsqueda de dictámenes pendientes en la Intranet de Adopciones
CREATE INDEX IF NOT EXISTS idx_solicitudes_revision 
ON solicitudes_adopcion (animal_id, estado) 
WHERE deleted_at IS NULL;

-- =============================================================================
-- 3. ÍNDICES B-TREE SOBRE CLAVES FORÁNEAS (JOIN PERFORMANCE)
-- Imprescindibles para prevenir bloqueos de tabla en eliminaciones e indexar JOINs
-- =============================================================================

-- Tickets de rescate
CREATE INDEX IF NOT EXISTS idx_tickets_albergue_id ON tickets_rescate (albergue_id);
CREATE INDEX IF NOT EXISTS idx_tickets_voluntario_id ON tickets_rescate (voluntario_id);

-- Animales
CREATE INDEX IF NOT EXISTS idx_animales_albergue_id ON animales (albergue_id);

-- Historial clínico
CREATE INDEX IF NOT EXISTS idx_historial_animal_id ON historial_clinico (animal_id);
CREATE INDEX IF NOT EXISTS idx_historial_veterinario_id ON historial_clinico (veterinario_id);

-- Solicitudes de adopción
CREATE INDEX IF NOT EXISTS idx_solicitudes_animal_id ON solicitudes_adopcion (animal_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_adoptante_id ON solicitudes_adopcion (adoptante_id);

-- Donaciones
CREATE INDEX IF NOT EXISTS idx_donaciones_albergue_id ON donaciones (albergue_id);
CREATE INDEX IF NOT EXISTS idx_donaciones_donante_id ON donaciones (donante_id);
