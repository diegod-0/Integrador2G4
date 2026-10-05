-- =============================================================================
-- RESCUELINK - V3__seed_initial_data.sql
-- Avance de Proyecto Final 2 (APF2) - Unidad 2 (Semanas 6-9)
-- Datos semilla iniciales para pruebas del jurado docente, albergues de Lima y catálogo
-- =============================================================================

-- =============================================================================
-- 1. USUARIOS INICIALES (Contraseñas con hash BCrypt: 'Password123!')
-- Hash: $2a$12$e8x/NfVvjLzM6e2gS8t8z.KjXb8YqZ4pW0uR5nK7mE3vA9xL1O2qS
-- =============================================================================
INSERT INTO usuarios (id, nombre, email, password_hash, rol, telefono)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Diego Claros (Admin)', 'admin@rescuelink.org', '$2a$12$e8x/NfVvjLzM6e2gS8t8z.KjXb8YqZ4pW0uR5nK7mE3vA9xL1O2qS', 'ROLE_ADMIN', '+51987654321'),
    ('a0000000-0000-0000-0000-000000000002', 'Pedro Cueto (Voluntario)', 'voluntario@rescuelink.org', '$2a$12$e8x/NfVvjLzM6e2gS8t8z.KjXb8YqZ4pW0uR5nK7mE3vA9xL1O2qS', 'ROLE_VOLUNTARIO', '+51987654322'),
    ('a0000000-0000-0000-0000-000000000003', 'Anghelo Mendoza (Veterinario)', 'veterinario@rescuelink.org', '$2a$12$e8x/NfVvjLzM6e2gS8t8z.KjXb8YqZ4pW0uR5nK7mE3vA9xL1O2qS', 'ROLE_VOLUNTARIO', '+51987654323'),
    ('a0000000-0000-0000-0000-000000000004', 'Elsa Riquelme (Ciudadana)', 'ciudadano@rescuelink.org', '$2a$12$e8x/NfVvjLzM6e2gS8t8z.KjXb8YqZ4pW0uR5nK7mE3vA9xL1O2qS', 'ROLE_CIUDADANO', '+51987654324')
ON CONFLICT (email) DO NOTHING;

-- =============================================================================
-- 2. ALBERGUES PILOTO EN LIMA METROPOLITANA
-- Coordenadas reales geodésicas SRID 4326: ST_SetSRID(ST_MakePoint(longitud, latitud), 4326)
-- =============================================================================
INSERT INTO albergues (id, nombre, direccion, telefono, capacidad_max, ubicacion)
VALUES
    (
        'b0000000-0000-0000-0000-000000000001',
        'Albergue Huellitas de Miraflores',
        'Av. José Pardo 450, Miraflores, Lima',
        '+5114458920',
        25,
        ST_SetSRID(ST_MakePoint(-77.0315, -12.1221), 4326)
    ),
    (
        'b0000000-0000-0000-0000-000000000002',
        'Refugio Esperanza Animal Surco',
        'Jr. Batalla de Ayacucho 310, Santiago de Surco, Lima',
        '+5112479130',
        40,
        ST_SetSRID(ST_MakePoint(-76.9950, -12.1460), 4326)
    ),
    (
        'b0000000-0000-0000-0000-000000000003',
        'Santuario Canino Los Olivos Norte',
        'Av. Carlos Izaguirre 1250, Los Olivos, Lima',
        '+5115214890',
        30,
        ST_SetSRID(ST_MakePoint(-77.0680, -11.9920), 4326)
    )
ON CONFLICT (nombre) DO NOTHING;

-- =============================================================================
-- 3. TICKETS DE RESCATE INICIALES (Flujo Rescue Tracker)
-- =============================================================================
INSERT INTO tickets_rescate (id, codigo_tracking, nombre_reportante, whatsapp_reportante, albergue_id, voluntario_id, descripcion, gravedad, estado, ubicacion, foto_url)
VALUES
    (
        'c0000000-0000-0000-0000-000000000001',
        'TICK-1025',
        'Carlos Benavides',
        '+51998877665',
        'b0000000-0000-0000-0000-000000000001',
        'a0000000-0000-0000-0000-000000000002',
        'Perrito mestizo atropellado cerca al parque Kennedy, tiene dificultad para caminar en pata trasera.',
        'CRITICA',
        'EN_CAMINO',
        ST_SetSRID(ST_MakePoint(-77.0305, -12.1215), 4326),
        'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=600&q=80'
    ),
    (
        'c0000000-0000-0000-0000-000000000002',
        'TICK-4829',
        'Mariana Sotelo',
        '+51988776655',
        'b0000000-0000-0000-0000-000000000002',
        NULL,
        'Cachorrita abandonada en caja de cartón en plaza mayor de Surco. Muy asustada.',
        'MODERADA',
        'PENDIENTE',
        ST_SetSRID(ST_MakePoint(-76.9930, -12.1440), 4326),
        'https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?auto=format&fit=crop&w=600&q=80'
    )
ON CONFLICT (codigo_tracking) DO NOTHING;

-- =============================================================================
-- 4. ANIMALES EN CATÁLOGO Y EN ADOPCIÓN (Para HU03 y HU04 Matchmaker)
-- =============================================================================
INSERT INTO animales (id, ticket_id, albergue_id, nombre, especie, edad_estimada, peso, nivel_energia, apto_departamento, estado, foto_perfil_url, foto_antes_url, foto_despues_url)
VALUES
    (
        'd0000000-0000-0000-0000-000000000001',
        NULL,
        'b0000000-0000-0000-0000-000000000001',
        'Rocky',
        'CANINO',
        'Adulto (3 años)',
        14.50,
        'MEDIO',
        TRUE,
        'EN_CATALOGO',
        'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=600&q=80',
        'https://images.unsplash.com/photo-1534361960057-19889db98a1e?auto=format&fit=crop&w=600&q=80',
        'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=600&q=80'
    ),
    (
        'd0000000-0000-0000-0000-000000000002',
        NULL,
        'b0000000-0000-0000-0000-000000000002',
        'Luna',
        'FELINO',
        'Joven (1 año)',
        3.80,
        'BAJO',
        TRUE,
        'EN_CATALOGO',
        'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=600&q=80',
        NULL,
        NULL
    ),
    (
        'd0000000-0000-0000-0000-000000000003',
        NULL,
        'b0000000-0000-0000-0000-000000000003',
        'Thor',
        'CANINO',
        'Joven (2 años)',
        22.00,
        'ALTO',
        FALSE,
        'EN_CATALOGO',
        'https://images.unsplash.com/photo-1587300003388-59208cc962cb?auto=format&fit=crop&w=600&q=80',
        NULL,
        NULL
    )
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 5. HISTORIAL CLÍNICO INICIAL (Triaje Veterinario HU09)
-- =============================================================================
INSERT INTO historial_clinico (id, animal_id, veterinario_id, diagnostico, tratamiento)
VALUES
    (
        'e0000000-0000-0000-0000-000000000001',
        'd0000000-0000-0000-0000-000000000001',
        'a0000000-0000-0000-0000-000000000003',
        'Triaje inicial: Contusión en miembro posterior derecho, deshidratación leve.',
        'Administración de AINEs por 5 días, hidratación con fluidos y reposo supervisado.'
    )
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 6. SOLICITUD DE ADOPCIÓN DE PRUEBA (Para HU10/HU11 - Elsa Riquelme)
-- =============================================================================
INSERT INTO solicitudes_adopcion (id, animal_id, adoptante_id, codigo_certificado_qr, estado, tipo_vivienda, tiene_otras_mascotas, motivo)
VALUES
    (
        'f0000000-0000-0000-0000-000000000001',
        'd0000000-0000-0000-0000-000000000001',
        'a0000000-0000-0000-0000-000000000004',
        'CERT-ADOP-2026-ROCKY-8921',
        'EN_REVISION',
        'DEPARTAMENTO_CON_BALCON',
        FALSE,
        'Deseo brindar un hogar responsable a Rocky, cuento con tiempo para paseos diarios y estabilidad económica.'
    )
ON CONFLICT (id) DO NOTHING;
