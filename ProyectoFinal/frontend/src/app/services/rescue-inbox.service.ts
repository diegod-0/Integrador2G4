import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InboxTicketSummary, AsignarVoluntarioPayload, ActualizarEstadoPayload } from '../models/rescue-inbox.model';

@Injectable({ providedIn: 'root' })
export class RescueInboxService {
  private readonly http = inject(HttpClient);

  obtenerBandeja(albergueId: string, radioMetros = 50000): Observable<{ success: boolean; data: InboxTicketSummary[] }> {
    return this.http.get<{ success: boolean; data: InboxTicketSummary[] }>(
      `/api/admin/rescates/bandeja?albergueId=${albergueId}&radioMetros=${radioMetros}`
    );
  }

  asignarRescate(ticketId: string, payload: AsignarVoluntarioPayload): Observable<any> {
    return this.http.patch<any>(`/api/admin/rescates/${ticketId}/asignar`, payload);
  }

  actualizarEstado(ticketId: string, payload: ActualizarEstadoPayload): Observable<any> {
    return this.http.patch<any>(`/api/admin/rescates/${ticketId}/estado`, payload);
  }
}
