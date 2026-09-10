import { Injectable, signal } from '@angular/core';
import { TicketRescate } from '../models/emergency-report.model';
import { ViewState } from '../models/view-state.model';
import {
  TRACKING_STORAGE_KEY,
  trackingMockTickets,
} from '../mocks/tracking.mocks';

@Injectable({ providedIn: 'root' })
export class TrackingService {
  private readonly _state = signal<ViewState<TicketRescate>>({ status: 'idle' });
  readonly state = this._state.asReadonly();

  load(codigo?: string): void {
    this._state.set({ status: 'loading' });

    setTimeout(() => {
      try {
        const tickets = this.readTickets();
        const code = codigo?.trim();

        if (!code) {
          this._state.set({
            status: 'empty',
            message: 'Ingresa un código de seguimiento para consultar el ticket.',
          });
          return;
        }

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
    }, 300);
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
