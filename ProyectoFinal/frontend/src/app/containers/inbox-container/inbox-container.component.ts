import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RescueInboxService } from '../../services/rescue-inbox.service';
import { AuthService } from '../../services/auth.service';
import { InboxTicketSummary } from '../../models/rescue-inbox.model';

interface AlbergueOption {
  id: string;
  nombre: string;
}

interface RadioOption {
  metros: number;
  label: string;
}

const ALBERGUES_PILOTO: AlbergueOption[] = [
  { id: 'b0000000-0000-0000-0000-000000000001', nombre: 'Albergue Huellitas de Miraflores' },
  { id: 'b0000000-0000-0000-0000-000000000002', nombre: 'Refugio Esperanza Animal Surco' },
  { id: 'b0000000-0000-0000-0000-000000000003', nombre: 'Santuario Canino Los Olivos Norte' }
];

const RADIOS_COBERTURA: RadioOption[] = [
  { metros: 15000, label: '15 km · Radio Local Distrital' },
  { metros: 30000, label: '30 km · Lima Urbana' },
  { metros: 50000, label: '50 km · Lima Metropolitana (Recomendado)' },
  { metros: 100000, label: '100 km · Región Lima Ampliada' },
  { metros: 3000000, label: '3,000 km · Ver todos los tickets en BD' }
];

// ID semilla del voluntario Pedro Cueto
const VOLUNTARIO_PEDRO_ID = 'a0000000-0000-0000-0000-000000000002';

@Component({
  selector: 'app-inbox-container',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inbox-container.component.html',
  styleUrl: './inbox-container.component.css'
})
export class InboxContainerComponent implements OnInit {
  private readonly inboxService = inject(RescueInboxService);
  readonly authService = inject(AuthService);

  readonly albergues = ALBERGUES_PILOTO;
  selectedAlbergueId = ALBERGUES_PILOTO[0].id;

  readonly radios = RADIOS_COBERTURA;
  selectedRadioMetros = 50000;

  // Estados de datos (Signals)
  readonly tickets = signal<InboxTicketSummary[]>([]);
  readonly loading = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  // Formulario manual de login
  loginEmail = 'admin@rescuelink.org';
  loginPass = 'password123';
  loginLoading = signal<boolean>(false);

  ngOnInit(): void {
    if (this.authService.isAuthenticated) {
      this.cargarBandeja();
    }
  }

  cambiarAlbergue(albergueId: string): void {
    this.selectedAlbergueId = albergueId;
    this.cargarBandeja();
  }

  cambiarRadio(radioMetros: any): void {
    this.selectedRadioMetros = Number(radioMetros);
    this.cargarBandeja();
  }

  formatearDistancia(metros: number | null | undefined): string {
    if (metros === null || metros === undefined) return 'Distancia N/A';
    if (metros >= 1000) {
      return `${(metros / 1000).toFixed(1)} km`;
    }
    return `${Math.round(metros)} m`;
  }

  cargarBandeja(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.inboxService.obtenerBandeja(this.selectedAlbergueId, this.selectedRadioMetros).subscribe({
      next: (res) => {
        this.tickets.set(res.data || []);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 403 || err.status === 401) {
          this.errorMessage.set('Debe iniciar sesión para consultar la bandeja operativa protegida (HU09/HU10).');
        } else {
          this.errorMessage.set(err.error?.detail || 'Error al conectar con la bandeja operativa de PostgreSQL.');
        }
      }
    });
  }

  loginRapidoAdmin(): void {
    this.loginLoading.set(true);
    this.errorMessage.set(null);
    this.authService.loginAsDemoAdmin().subscribe({
      next: () => {
        this.loginLoading.set(false);
        this.successMessage.set('¡Sesión iniciada con éxito! Token JWT inyectado en solicitudes.');
        this.cargarBandeja();
        setTimeout(() => this.successMessage.set(null), 3000);
      },
      error: (err) => {
        this.loginLoading.set(false);
        this.errorMessage.set(err.error?.detail || 'Credenciales inválidas.');
      }
    });
  }

  loginManual(): void {
    this.loginLoading.set(true);
    this.errorMessage.set(null);
    this.authService.login(this.loginEmail, this.loginPass).subscribe({
      next: () => {
        this.loginLoading.set(false);
        this.successMessage.set('¡Sesión iniciada exitosamente!');
        this.cargarBandeja();
        setTimeout(() => this.successMessage.set(null), 3000);
      },
      error: (err) => {
        this.loginLoading.set(false);
        this.errorMessage.set(err.error?.detail || 'Credenciales inválidas.');
      }
    });
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.tickets.set([]);
  }

  asignarVoluntario(ticket: InboxTicketSummary): void {
    this.successMessage.set(null);
    this.errorMessage.set(null);

    this.inboxService.asignarRescate(ticket.ticketId, {
      voluntarioId: VOLUNTARIO_PEDRO_ID,
      albergueId: this.selectedAlbergueId
    }).subscribe({
      next: () => {
        this.successMessage.set(`¡Ticket ${ticket.codigoTracking} asignado a Pedro Cueto exitosamente!`);
        this.cargarBandeja();
        setTimeout(() => this.successMessage.set(null), 3500);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.detail || 'Error al asignar rescate.');
      }
    });
  }

  cambiarEstado(ticket: InboxTicketSummary, nuevoEstado: 'EN_CAMINO' | 'RESCATADO'): void {
    this.successMessage.set(null);
    this.errorMessage.set(null);

    this.inboxService.actualizarEstado(ticket.ticketId, { nuevoEstado }).subscribe({
      next: () => {
        this.successMessage.set(`¡Estado del ticket ${ticket.codigoTracking} actualizado a ${nuevoEstado}!`);
        this.cargarBandeja();
        setTimeout(() => this.successMessage.set(null), 3500);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.detail || 'Error al actualizar estado.');
      }
    });
  }
}
