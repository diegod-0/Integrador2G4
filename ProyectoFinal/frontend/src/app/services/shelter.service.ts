import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Albergue, RegistrarDonacionPayload, DonacionResponseDto } from '../models/shelter.model';
import { ViewState } from '../models/view-state.model';

const MOCK_ALBERGUES: Albergue[] = [
  {
    id: 'b0000000-0000-0000-0000-000000000001',
    nombre: 'Albergue Huellitas de Miraflores',
    direccion: 'Av. José Pardo 450, Miraflores, Lima',
    telefono: '+5114458920',
    capacidadMax: 25,
    ocupacionActual: 1,
    porcentajeOcupacion: 4.0,
    longitud: -77.0315,
    latitud: -12.1221,
    distanciaMetros: null
  },
  {
    id: 'b0000000-0000-0000-0000-000000000002',
    nombre: 'Refugio Esperanza Animal Surco',
    direccion: 'Jr. Batalla de Ayacucho 310, Santiago de Surco, Lima',
    telefono: '+5112479130',
    capacidadMax: 40,
    ocupacionActual: 1,
    porcentajeOcupacion: 2.5,
    longitud: -76.9950,
    latitud: -12.1460,
    distanciaMetros: null
  },
  {
    id: 'b0000000-0000-0000-0000-000000000003',
    nombre: 'Santuario Canino Los Olivos Norte',
    direccion: 'Av. Carlos Izaguirre 1250, Los Olivos, Lima',
    telefono: '+5115214890',
    capacidadMax: 30,
    ocupacionActual: 1,
    porcentajeOcupacion: 3.33,
    longitud: -77.0680,
    latitud: -11.9920,
    distanciaMetros: null
  }
];

@Injectable({ providedIn: 'root' })
export class ShelterService {
  private readonly http = inject(HttpClient);

  private readonly _state = signal<ViewState<Albergue[]>>({ status: 'idle' });
  readonly state = this._state.asReadonly();

  cargarAlbergues(): void {
    this._state.set({ status: 'loading' });

    this.http.get<{ success: boolean; data: Albergue[] }>('/api/albergues').subscribe({
      next: (res) => {
        const albergues = res?.data || [];
        this._state.set({ status: 'success', data: albergues });
      },
      error: () => {
        // Fallback resiliente con datos de prueba
        this._state.set({ status: 'success', data: MOCK_ALBERGUES });
      }
    });
  }

  registrarDonacion(albergueId: string, payload: RegistrarDonacionPayload): Observable<any> {
    return this.http.post<any>(`/api/albergues/${albergueId}/donaciones`, payload);
  }
}
