import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DuplicateIncidentDetail {
  codigoTracking: string;
  estado: string;
  distanciaMetros: number;
  fotoUrl?: string;
  fechaReporte?: string;
}

export interface DuplicateCheckResponse {
  success: boolean;
  message: string;
  data: {
    hayDuplicado: boolean;
    mensaje: string;
    incidenteCercano: DuplicateIncidentDetail | null;
  };
}

@Injectable({ providedIn: 'root' })
export class DuplicateService {
  private readonly http = inject(HttpClient);

  verificar(lng: number, lat: number, radioMetros = 50): Observable<DuplicateCheckResponse> {
    const url = `/api/tickets/verificar-duplicado?lng=${lng}&lat=${lat}&radioMetros=${radioMetros}`;
    return this.http.get<DuplicateCheckResponse>(url);
  }
}
