export interface Albergue {
  id: string;
  nombre: string;
  direccion: string;
  telefono: string;
  capacidadMax: number;
  ocupacionActual: number;
  porcentajeOcupacion: number;
  longitud: number;
  latitud: number;
  distanciaMetros: number | null;
}

export type TipoDonacion = 'MONETARIA' | 'ALIMENTO' | 'MEDICINAS' | 'ACCESORIOS';

export interface RegistrarDonacionPayload {
  tipo: TipoDonacion;
  montoEstimado: number;
  descripcion: string;
}

export interface DonacionResponseDto {
  id: string;
  albergueId: string;
  tipo: TipoDonacion;
  montoEstimado: number;
  descripcion: string;
  emailDonante?: string;
  fechaRegistro: string;
}
