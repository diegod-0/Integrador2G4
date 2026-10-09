import { AnimalAdopcion } from '../models/animal.model';

/**
 * Catálogo sincronizado con la base de datos PostgreSQL 17 (V3__seed_initial_data.sql).
 * Todos los albergues, nombres, coordenadas y atributos corresponden a la Red RescueLink real.
 */
export const ANIMALS_MOCK: AnimalAdopcion[] = [
  {
    id: 'a1',
    nombre: 'Rocky',
    especie: 'PERRO',
    raza: 'Mestizo de Miraflores',
    edadAnios: 3,
    tamano: 'MEDIANO',
    sexo: 'MACHO',
    fotoUrl: 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=600&q=80',
    descripcion: 'Rescatado en Miraflores con pata vendada. Es sociable, noble y cariñoso.',
    albergueNombre: 'Albergue Huellitas de Miraflores',
    coordenadas: { latitude: -12.1221, longitude: -77.0315 },
    distrito: 'Miraflores',
    energia: 'MODERADO',
    espacioRequerido: 'DEPARTAMENTO',
    tiempoRequerido: 'MEDIO',
  },
  {
    id: 'a2',
    nombre: 'Luna',
    especie: 'GATO',
    raza: 'Atigrada Común',
    edadAnios: 1,
    tamano: 'PEQUENO',
    sexo: 'HEMBRA',
    fotoUrl: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=600&q=80',
    descripcion: 'Gatita tranquila y mimosa rescatada en Surco. Se adapta perfecto a departamentos.',
    albergueNombre: 'Refugio Esperanza Animal Surco',
    coordenadas: { latitude: -12.1460, longitude: -76.9950 },
    distrito: 'Santiago de Surco',
    energia: 'TRANQUILO',
    espacioRequerido: 'DEPARTAMENTO',
    tiempoRequerido: 'BAJO',
  },
  {
    id: 'a3',
    nombre: 'Thor',
    especie: 'PERRO',
    raza: 'Pastor Mestizo',
    edadAnios: 2,
    tamano: 'GRANDE',
    sexo: 'MACHO',
    fotoUrl: 'https://images.unsplash.com/photo-1587300003388-59208cc962cb?auto=format&fit=crop&w=600&q=80',
    descripcion: 'Joven juguetón y lleno de vitalidad. Ideal para hogares con jardín o personas activas.',
    albergueNombre: 'Santuario Canino Los Olivos Norte',
    coordenadas: { latitude: -11.9920, longitude: -77.0680 },
    distrito: 'Los Olivos',
    energia: 'ACTIVO',
    espacioRequerido: 'CASA_PATIO',
    tiempoRequerido: 'ALTO',
  },
];


