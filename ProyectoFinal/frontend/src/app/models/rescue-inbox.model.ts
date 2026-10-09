export interface InboxTicketSummary {
  ticketId: string;
  codigoTracking: string;
  descripcion: string;
  gravedad: 'LEVE' | 'MODERADA' | 'CRITICA';
  estado: string;
  longitud: number;
  latitud: number;
  distanciaMetros: number | null;
  fotoUrl?: string;
  albergueAsignadoNombre?: string | null;
  voluntarioNombre?: string | null;
  fechaReporte?: string;
}

export interface AsignarVoluntarioPayload {
  voluntarioId: string;
  albergueId: string;
}

export interface ActualizarEstadoPayload {
  nuevoEstado: 'EN_CAMINO' | 'RESCATADO' | 'NO_LOCALIZADO' | 'RECHAZADO';
}
