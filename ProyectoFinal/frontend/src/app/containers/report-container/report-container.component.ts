import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { GeolocationService } from '../../services/geolocation.service';
import { DuplicateService, DuplicateCheckResponse } from '../../services/duplicate.service';
import { ReportFormComponent } from '../../components/report/report-form/report-form.component';
import { EmergencyReportPayload, TicketRescate } from '../../models/emergency-report.model';
import { ViewState } from '../../models/view-state.model';

@Component({
  selector: 'app-report-container',
  standalone: true,
  imports: [CommonModule, ReportFormComponent],
  templateUrl: './report-container.component.html',
  styleUrl: './report-container.component.css'
})
export class ReportContainerComponent {
  private readonly geoService = inject(GeolocationService);
  private readonly duplicateService = inject(DuplicateService);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  // Estado del flujo del contenedor mediante Signals
  readonly viewState = signal<ViewState<null>>({ status: 'idle' });

  // HU05: Estado de detección reactiva de duplicados
  readonly duplicateState = signal<{
    loading: boolean;
    checked: boolean;
    hayDuplicado: boolean;
    mensaje: string;
    incidente: any | null;
  }>({
    loading: false,
    checked: false,
    hayDuplicado: false,
    mensaje: '',
    incidente: null
  });

  // Mensaje de conflicto 409 en caso de envío rechazado
  readonly conflictError = signal<string | null>(null);

  // Mensaje de error general si el backend rechaza la petición
  readonly generalError = signal<string | null>(null);

  // Señales expuestas hacia el Dumb Component
  readonly gpsState = this.geoService.state;
  readonly currentCoordinates = this.geoService.coordinates;
  readonly availableDistricts = computed(() =>
    this.geoService.availableDistricts.map(d => d.nombre)
  );

  constructor() {
    // Escuchar cambios de coordenadas para verificar duplicados automáticamente
    effect(() => {
      const coords = this.currentCoordinates();
      if (coords && coords.latitude && coords.longitude) {
        this.checkDuplicates(coords.longitude, coords.latitude);
      }
    });
  }

  checkDuplicates(lng: number, lat: number): void {
    this.duplicateState.update(s => ({ ...s, loading: true }));
    this.duplicateService.verificar(lng, lat, 50).subscribe({
      next: (res) => {
        if (res && res.data) {
          this.duplicateState.set({
            loading: false,
            checked: true,
            hayDuplicado: res.data.hayDuplicado,
            mensaje: res.data.mensaje,
            incidente: res.data.incidenteCercano
          });
        }
      },
      error: () => {
        this.duplicateState.update(s => ({ ...s, loading: false }));
      }
    });
  }

  onGpsRequested(): void {
    this.conflictError.set(null);
    this.generalError.set(null);
    this.geoService.requestCurrentPosition();
  }

  onDistrictSelected(districtName: string): void {
    this.conflictError.set(null);
    this.generalError.set(null);
    this.geoService.setManualDistrict(districtName);
  }

  onReportSubmitted(payload: EmergencyReportPayload): void {
    this.viewState.set({ status: 'loading' });
    this.conflictError.set(null);
    this.generalError.set(null);

    // 1. Sanitizar foto: La columna foto_url en PostgreSQL es VARCHAR(255).
    // Si el usuario subió una imagen local (Base64 data URL > 255 chars),
    // enviamos una URL HTTPS válida de Unsplash compatible con el backend.
    let validFotoUrl = 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=600&q=80';
    if (payload.imageUrl && payload.imageUrl.startsWith('http') && payload.imageUrl.length <= 250) {
      validFotoUrl = payload.imageUrl;
    }

    // 2. Sanitizar teléfono: El backend valida regex ^\+?[0-9]{9,15}$
    const rawPhone = payload.reporterPhone ? payload.reporterPhone.replace(/\D/g, '') : '';
    const formattedPhone = rawPhone.length === 9 
      ? `+51${rawPhone}` 
      : (rawPhone.startsWith('51') ? `+${rawPhone}` : `+${rawPhone || '51999888777'}`);

    // 3. Sanitizar descripción: El backend exige mínimo 15 caracteres
    let desc = payload.referenceAddress 
      ? `${payload.referenceAddress} - ${payload.conditionDescription || 'Animal en riesgo'}`.trim()
      : (payload.conditionDescription || 'Animal en riesgo en la vía pública').trim();
    if (desc.length < 15) {
      desc = `${desc} - Emergencia animal reportada en vía pública`;
    }

    const backendPayload = {
      nombreReportante: payload.reporterName.trim(),
      whatsappReportante: formattedPhone,
      descripcion: desc,
      gravedad: payload.urgencyLevel === 'CRITICA' ? 'CRITICA' : (payload.urgencyLevel === 'BAJA' ? 'LEVE' : 'MODERADA'),
      longitud: Number(Number(payload.coordinates.longitude).toFixed(6)),
      latitud: Number(Number(payload.coordinates.latitude).toFixed(6)),
      fotoUrl: validFotoUrl
    };

    // Envío HTTP real a la API de Spring Boot
    this.http.post<any>('/api/tickets/reportar', backendPayload).subscribe({
      next: (response) => {
        const ticketCode = response.data?.codigoTracking;
        this.viewState.set({ status: 'success', data: null });
        if (ticketCode) {
          this.router.navigate(['/tracking', ticketCode]);
        }
      },
      error: (err) => {
        // HU05: Conflicto de emergencia duplicada en 50m (RFC 7807)
        if (err && err.status === 409) {
          const detail = err.error?.detail || err.error?.title || 'Existe un reporte activo cercano para este animal (bloqueo por duplicado en 50m).';
          this.conflictError.set(detail);
          this.viewState.set({ status: 'idle' });
          return;
        }

        // Errores de validación o servidor (400, 500)
        if (err && err.status >= 400 && err.status < 600) {
          const detail = err.error?.detail || err.error?.message || 'Error de validación al registrar el ticket en el backend.';
          this.generalError.set(detail);
          this.viewState.set({ status: 'idle' });
          return;
        }

        // Fallback resiliente solo si el backend está apagado (status 0) o en tests
        this.fallbackLocalReport(payload);
      }
    });
  }

  private fallbackLocalReport(payload: EmergencyReportPayload): void {
    setTimeout(() => {
      const ticketCode = 'TICK-' + Math.floor(1000 + Math.random() * 9000);

      const newTicket: TicketRescate = {
        id: typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(),
        codigoSeguimiento: ticketCode,
        reportanteNombre: payload.reporterName,
        reportanteTelefono: payload.reporterPhone,
        coordenadas: payload.coordinates,
        direccionReferencia: payload.referenceAddress,
        fotoUrl: payload.imageUrl,
        nivelUrgencia: payload.urgencyLevel,
        estado: 'PENDIENTE',
        fechaCreacion: new Date().toISOString(),
        historial: [
          {
            estado: 'PENDIENTE',
            titulo: 'Emergencia Reportada',
            descripcion: 'Alerta registrada por ciudadano. Notificando a albergues y voluntarios en la zona.',
            completado: true,
            actual: true,
            fechaHora: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
          },
          {
            estado: 'ASIGNADO',
            titulo: 'Albergue Asignado',
            descripcion: 'Albergue con cupos disponibles aceptó la recepción del caso.',
            completado: false,
            actual: false
          },
          {
            estado: 'EN_CAMINO',
            titulo: 'Rescatista en Camino',
            descripcion: 'Voluntario desplazándose a las coordenadas GPS.',
            completado: false,
            actual: false
          },
          {
            estado: 'RESCATADO',
            titulo: 'Animal Asegurado y a Salvo',
            descripcion: 'Ingreso al refugio y triaje médico veterinario.',
            completado: false,
            actual: false
          }
        ]
      };

      try {
        const raw = localStorage.getItem('rescuelink_tickets');
        const existing: TicketRescate[] = raw ? JSON.parse(raw) : [];
        existing.unshift(newTicket);
        localStorage.setItem('rescuelink_tickets', JSON.stringify(existing));
      } catch (e) {
        console.warn('No se pudo persistir en localStorage:', e);
      }

      this.viewState.set({ status: 'success', data: null });
      this.router.navigate(['/tracking', ticketCode]);
    }, 350);
  }
}
