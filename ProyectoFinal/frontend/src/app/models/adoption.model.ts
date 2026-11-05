export interface PostularAdopcionPayload {
  animalId: string;
  nombreAdoptante: string;
  emailAdoptante: string;
  telefonoAdoptante: string;
  tipoVivienda: 'CASA_CON_JARDIN' | 'DEPARTAMENTO_CON_BALCON' | 'DEPARTAMENTO_SIN_BALCON';
  tieneOtrasMascotas: boolean;
  motivo: string;
  aceptaTerminosLey30407: boolean;
}

export interface SolicitudAdopcionResponse {
  id: string;
  animalId: string;
  nombreAnimal: string;
  nombreAdoptante: string;
  emailAdoptante: string;
  estado: 'RECIBIDA' | 'EN_REVISION' | 'APROBADA' | 'RECHAZADA' | 'CERRADA_POR_OTRA_ADOPCION';
  tipoVivienda: string;
  tieneOtrasMascotas: boolean;
  codigoCertificadoQr: string | null;
  estadoNotificacionExterna: string;
  externalMessageId: string | null;
  fechaSolicitud: string;
}

export interface VerificacionCertificadoQrResponse {
  valido: boolean;
  codigoCertificadoQr: string;
  estadoCertificado: string;
  nombreAnimal: string;
  especie: string;
  nombreAlbergue: string;
  nombreAdoptante: string;
  fechaSolicitud: string;
  mensajeVerificacion: string;
}

