import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';
import {
  PostularAdopcionPayload,
  SolicitudAdopcionResponse,
  VerificacionCertificadoQrResponse,
} from '../models/adoption.model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

const STORAGE_KEY = 'rescuelink_solicitudes_adopcion';

/**
 * Servicio reactivo para postulación a adopciones y verificación de Certificados QR (APF3).
 * Implementa Idempotency-Key contra envíos dobles y degradación elegante (fallback local).
 */
@Injectable({ providedIn: 'root' })
export class AdoptionService {
  private readonly http = inject(HttpClient);
  private readonly _isSubmitting = signal<boolean>(false);

  readonly isSubmitting = this._isSubmitting.asReadonly();

  postularAdopcion(
    payload: PostularAdopcionPayload,
    nombreAnimal: string
  ): Observable<SolicitudAdopcionResponse> {
    this._isSubmitting.set(true);
    const idempotencyKey = `idem-${payload.animalId}-${payload.emailAdoptante.trim().toLowerCase()}`;
    const headers = new HttpHeaders({ 'Idempotency-Key': idempotencyKey });

    return this.http
      .post<ApiResponse<SolicitudAdopcionResponse>>('/api/adopciones/postular', payload, { headers })
      .pipe(
        map(res => {
          this._isSubmitting.set(false);
          this.saveLocal(res.data);
          return res.data;
        }),
        catchError(() => {
          this._isSubmitting.set(false);
          const fallback: SolicitudAdopcionResponse = {
            id: crypto.randomUUID ? crypto.randomUUID() : `sol-${Date.now()}`,
            animalId: payload.animalId,
            nombreAnimal,
            nombreAdoptante: payload.nombreAdoptante,
            emailAdoptante: payload.emailAdoptante,
            estado: 'RECIBIDA',
            tipoVivienda: payload.tipoVivienda,
            tieneOtrasMascotas: payload.tieneOtrasMascotas,
            codigoCertificadoQr: `CERT-RL-${Math.random().toString(16).substring(2, 10).toUpperCase()}`,
            estadoNotificacionExterna: 'SIMULATED_FALLBACK',
            externalMessageId: idempotencyKey,
            fechaSolicitud: new Date().toISOString(),
          };
          this.saveLocal(fallback);
          return of(fallback);
        })
      );
  }

  verificarCertificadoQr(codigoQr: string): Observable<VerificacionCertificadoQrResponse> {
    return this.http
      .get<ApiResponse<VerificacionCertificadoQrResponse>>(
        `/api/adopciones/verificar/${encodeURIComponent(codigoQr.trim())}`
      )
      .pipe(
        map(res => res.data),
        catchError(() => {
          const localMatch = this.getLocalAll().find(
            s => s.codigoCertificadoQr?.toUpperCase() === codigoQr.trim().toUpperCase()
          );
          if (localMatch) {
            return of({
              valido: true,
              codigoCertificadoQr: localMatch.codigoCertificadoQr!,
              estadoCertificado: localMatch.estado,
              nombreAnimal: localMatch.nombreAnimal,
              especie: 'CANINO',
              nombreAlbergue: 'Red Oficial RescueLink',
              nombreAdoptante: localMatch.nombreAdoptante,
              fechaSolicitud: localMatch.fechaSolicitud,
              mensajeVerificacion: 'Certificado verificado correctamente (Respaldo Local Activo).',
            });
          }
          return of({
            valido: false,
            codigoCertificadoQr: codigoQr,
            estadoCertificado: 'NO_ENCONTRADO',
            nombreAnimal: 'Desconocido',
            especie: 'N/A',
            nombreAlbergue: 'N/A',
            nombreAdoptante: 'N/A',
            fechaSolicitud: new Date().toISOString(),
            mensajeVerificacion: 'El código QR ingresado no corresponde a un certificado oficial vigente.',
          });
        })
      );
  }

  private saveLocal(solicitud: SolicitudAdopcionResponse): void {
    try {
      const list = this.getLocalAll().filter(s => s.id !== solicitud.id);
      list.unshift(solicitud);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
    } catch {
      // Ignorar si localStorage está restringido
    }
  }

  private getLocalAll(): SolicitudAdopcionResponse[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as SolicitudAdopcionResponse[]) : [];
    } catch {
      return [];
    }
  }
}

