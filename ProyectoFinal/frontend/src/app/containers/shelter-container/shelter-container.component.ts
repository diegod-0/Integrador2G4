import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShelterService } from '../../services/shelter.service';
import { Albergue, RegistrarDonacionPayload, TipoDonacion } from '../../models/shelter.model';

@Component({
  selector: 'app-shelter-container',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './shelter-container.component.html',
  styleUrl: './shelter-container.component.css'
})
export class ShelterContainerComponent implements OnInit {
  private readonly shelterService = inject(ShelterService);

  readonly state = this.shelterService.state;
  readonly albergues = computed(() => {
    const s = this.state();
    return s.status === 'success' ? s.data : [];
  });
  readonly isLoading = computed(() => this.state().status === 'loading');

  // Modal / Formulario interactivo de donación
  readonly selectedAlbergue = signal<Albergue | null>(null);
  readonly donationLoading = signal<boolean>(false);
  readonly donationSuccess = signal<string | null>(null);
  readonly donationError = signal<string | null>(null);

  // Campos del formulario (vacíos por defecto)
  donationTipo: TipoDonacion = 'MONETARIA';
  donationMonto: number | null = null;
  donationDescripcion: string = '';

  ngOnInit(): void {
    this.shelterService.cargarAlbergues();
  }

  abrirDonacion(albergue: Albergue): void {
    this.selectedAlbergue.set(albergue);
    this.donationTipo = 'MONETARIA';
    this.donationMonto = null;
    this.donationDescripcion = '';
    this.donationSuccess.set(null);
    this.donationError.set(null);
  }

  cerrarDonacion(): void {
    this.selectedAlbergue.set(null);
  }

  enviarDonacion(): void {
    const albergue = this.selectedAlbergue();
    if (!albergue) return;

    if (!this.donationMonto || this.donationMonto <= 0) {
      this.donationError.set('Por favor, ingresa un monto o cantidad estimada mayor a 0.');
      return;
    }

    if (!this.donationDescripcion || !this.donationDescripcion.trim()) {
      this.donationError.set('Por favor, describe brevemente el aporte de la donación.');
      return;
    }

    this.donationLoading.set(true);
    this.donationSuccess.set(null);
    this.donationError.set(null);

    const payload: RegistrarDonacionPayload = {
      tipo: this.donationTipo,
      montoEstimado: Number(this.donationMonto),
      descripcion: this.donationDescripcion.trim()
    };

    this.shelterService.registrarDonacion(albergue.id, payload).subscribe({
      next: (res) => {
        this.donationLoading.set(false);
        this.donationSuccess.set(`¡Donación registrada con éxito en ${albergue.nombre}! (Código: ${res.data?.id ?? 'DON-OK'})`);
        setTimeout(() => this.cerrarDonacion(), 2500);
      },
      error: (err) => {
        this.donationLoading.set(false);
        this.donationError.set(err.error?.detail || 'Error al conectar con el servidor.');
      }
    });
  }
}
