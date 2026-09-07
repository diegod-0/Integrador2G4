import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { GeolocationService } from '../../services/geolocation.service';
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
  private readonly router = inject(Router);

  // Estado del flujo del contenedor mediante Signals
  readonly viewState = signal<ViewState<null>>({ status: 'idle' });

  // Señales expuestas hacia el Dumb Component
  readonly gpsState = this.geoService.state;
  readonly currentCoordinates = this.geoService.coordinates;
  readonly availableDistricts = computed(() =>
    this.geoService.availableDistricts.map(d => d.nombre)
  );

  onGpsRequested(): void {
    this.geoService.requestCurrentPosition();
  }

  onDistrictSelected(districtName: string): void {
    this.geoService.setManualDistrict(districtName);
  }

  onReportSubmitted(payload: EmergencyReportPayload): void {
    this.viewState.set({ status: 'loading' });

    // Simulación de latencia de red (350 ms)
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

      // Persistencia en localStorage para que el módulo de Pedro Cueto (HU02 /tracking) lo lea de inmediato
      try {
        const raw = localStorage.getItem('rescuelink_tickets');
        const existing: TicketRescate[] = raw ? JSON.parse(raw) : [];
        existing.unshift(newTicket);
        localStorage.setItem('rescuelink_tickets', JSON.stringify(existing));
      } catch (e) {
        console.warn('No se pudo persistir en localStorage:', e);
      }

      this.viewState.set({ status: 'success', data: null });

      // Redirección reactiva al Rescue Tracker
      this.router.navigate(['/tracking', ticketCode]);
    }, 350);
  }
}
