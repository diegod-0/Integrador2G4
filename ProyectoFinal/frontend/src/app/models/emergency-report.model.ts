/**
 * Contratos de datos para el Reporte de Emergencias y Rescue Tracker
 * Compatible con los esquemas de PostgreSQL/PostGIS de la Fase APF2.
 */

export interface GeoCoordinates {
  latitude: number;
  longitude: number;
  accuracyMeters?: number;
}

export type GeolocationState =
  | { status: 'idle' }
  | { status: 'requesting' }
  | { status: 'acquired'; coords: GeoCoordinates }
  | { status: 'denied'; fallbackReason: string }
  | { status: 'error'; message: string };

export type UrgencyLevel = 'BAJA' | 'MEDIA' | 'CRITICA';

export type TicketEstado =
  | 'PENDIENTE'
  | 'ASIGNADO'
  | 'EN_CAMINO'
  | 'RESCATADO'
  | 'RECHAZADO'
  | 'NO_LOCALIZADO';

export interface EmergencyReportPayload {
  reporterName: string;
  reporterPhone: string;
  coordinates: GeoCoordinates;
  referenceAddress: string;
  imageUrl: string;
  conditionDescription?: string;
  urgencyLevel: UrgencyLevel;
}

export interface TimelineStep {
  estado: TicketEstado;
  titulo: string;
  descripcion: string;
  completado: boolean;
  actual: boolean;
  fechaHora?: string;
}

export interface TicketRescate {
  id: string;
  codigoSeguimiento: string; // ej. TICK-4829
  reportanteNombre: string;
  reportanteTelefono: string;
  coordenadas: GeoCoordinates;
  direccionReferencia: string;
  fotoUrl: string;
  nivelUrgencia: UrgencyLevel;
  estado: TicketEstado;
  albergueAsignado?: string;
  voluntarioAsignado?: string;
  fechaCreacion: string;
  historial: TimelineStep[];
}
