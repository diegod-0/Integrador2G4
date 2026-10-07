import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TicketRescate, TimelineStep } from '../models/emergency-report.model';
import { ViewState } from '../models/view-state.model';
import {
  TRACKING_STORAGE_KEY,
  trackingMockTickets,
} from '../mocks/tracking.mocks';

@Injectable({ providedIn: 'root' })
export class TrackingService {
  private readonly http = inject(HttpClient);
  private readonly _state = signal<ViewState<TicketRescate>>({ status: 'idle' });
  readonly state = this._state.asReadonly();

  load(codigo?: string): void {
    const code = codigo?.trim();

    if (!code) {
      this._state.set({
        status: 'empty',
        message: 'Ingresa un código de seguimiento para consultar el ticket.',
      });
      return;
    }

    this._state.set({ status: 'loading' });

    // Intento primario: Conexión HTTP real con Spring Boot / PostgreSQL
    this.http.get<any>(`/api/tickets/tracking/${code}`).subscribe({
      next: (response) => {
        if (response && response.data) {
          const t = response.data;
          const mapped: TicketRescate = this.mapBackendResponse(t);
          this._state.set({ status: 'success', data: mapped });
        } else {
          this.loadFromFallback(code);
        }
      },
      error: () => {
        // Fallback resiliente: Si el backend está apagado o es un ticket mock local
        this.loadFromFallback(code);
      }
    });
  }

  private loadFromFallback(code: string): void {
    try {
      const tickets = this.readTickets();
      const ticket = tickets.find(
        candidate => candidate.codigoSeguimiento.toLowerCase() === code.toLowerCase(),
      );

      if (!ticket) {
        this._state.set({
          status: 'empty',
          message: `No encontramos un ticket con el código ${code}.`,
        });
        return;
      }

      this._state.set({ status: 'success', data: ticket });
    } catch {
      this._state.set({
        status: 'error',
        message: 'No se pudo leer el almacenamiento local. Intenta nuevamente.',
      });
    }
  }

  private mapBackendResponse(t: any): TicketRescate {
    const estado = t.estado || 'PENDIENTE';
    const pasos: TimelineStep[] = [
      {
        estado: 'PENDIENTE',
        titulo: 'Emergencia Reportada',
        descripcion: 'Alerta registrada en la red RescueLink.',
        completado: true,
        actual: estado === 'PENDIENTE',
        fechaHora: t.createdAt ? new Date(t.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : undefined
      },
      {
        estado: 'ASIGNADO',
        titulo: 'Albergue Asignado',
        descripcion: t.albergueNombre ? `Asignado a ${t.albergueNombre}` : 'Esperando asignación de refugio.',
        completado: estado === 'ASIGNADO' || estado === 'EN_CAMINO' || estado === 'RESCATADO',
        actual: estado === 'ASIGNADO'
      },
      {
        estado: 'EN_CAMINO',
        titulo: 'Rescatista en Camino',
        descripcion: t.voluntarioNombre ? `Voluntario ${t.voluntarioNombre} en ruta.` : 'Voluntario desplazándose al lugar.',
        completado: estado === 'EN_CAMINO' || estado === 'RESCATADO',
        actual: estado === 'EN_CAMINO'
      },
      {
        estado: 'RESCATADO',
        titulo: 'Animal Asegurado y a Salvo',
        descripcion: 'Ingreso al refugio y evaluación veterinaria.',
        completado: estado === 'RESCATADO',
        actual: estado === 'RESCATADO'
      }
    ];

    return {
      id: t.id || 'id-mock',
      codigoSeguimiento: t.codigoTracking,
      reportanteNombre: t.nombreReportante || 'Ciudadano',
      reportanteTelefono: t.whatsappReportante || '+51 999 999 999',
      coordenadas: { latitude: t.latitud || -12.12, longitude: t.longitud || -77.03 },
      direccionReferencia: t.descripcion || 'Sin referencia',
      fotoUrl: t.fotoUrl || 'https://images.unsplash.com/photo-1543466835-00a7907e9de1',
      nivelUrgencia: t.gravedad === 'CRITICA' ? 'CRITICA' : 'MEDIA',
      estado: estado,
      albergueAsignado: t.albergueNombre,
      voluntarioAsignado: t.voluntarioNombre,
      fechaCreacion: t.createdAt || new Date().toISOString(),
      historial: pasos
    };
  }

  private readTickets(): TicketRescate[] {
    const raw = globalThis.localStorage?.getItem(TRACKING_STORAGE_KEY);

    if (!raw) {
      return trackingMockTickets;
    }

    const parsed: unknown = JSON.parse(raw);
    if (!Array.isArray(parsed)) {
      throw new Error('El almacenamiento no contiene una lista de tickets.');
    }

    return parsed.length > 0 ? (parsed as TicketRescate[]) : trackingMockTickets;
  }
}
