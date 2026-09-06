import { GeoCoordinates } from './emergency-report.model';

export type AnimalEspecie = 'PERRO' | 'GATO';
export type AnimalTamano = 'PEQUENO' | 'MEDIANO' | 'GRANDE';
export type AnimalSexo = 'MACHO' | 'HEMBRA';

export interface AnimalAdopcion {
  id: string;
  nombre: string;
  especie: AnimalEspecie;
  raza: string;
  edadAnios: number;
  tamano: AnimalTamano;
  sexo: AnimalSexo;
  fotoUrl: string;
  descripcion: string;
  albergueNombre: string;
  coordenadas: GeoCoordinates;
  distrito: string;
  distanciaKm?: number; // Calculada dinámicamente con GPS
  
  // Parámetros de compatibilidad Matchmaker
  energia: 'TRANQUILO' | 'MODERADO' | 'ACTIVO';
  espacioRequerido: 'DEPARTAMENTO' | 'CASA_PATIO';
  tiempoRequerido: 'BAJO' | 'MEDIO' | 'ALTO';
}

export interface MatchmakerAnswers {
  tipoVivienda: 'DEPARTAMENTO' | 'CASA_PATIO';
  tiempoDisponible: 'BAJO' | 'MEDIO' | 'ALTO';
  nivelEnergia: 'TRANQUILO' | 'MODERADO' | 'ACTIVO';
}
