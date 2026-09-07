import { Injectable, computed, signal } from '@angular/core';
import { GeoCoordinates, GeolocationState } from '../models/emergency-report.model';

export interface DistrictCoords {
  nombre: string;
  coords: GeoCoordinates;
}

export const LIMA_DISTRICTS: DistrictCoords[] = [
  { nombre: 'Miraflores', coords: { latitude: -12.1192, longitude: -77.0290, accuracyMeters: 500 } },
  { nombre: 'San Isidro', coords: { latitude: -12.0984, longitude: -77.0353, accuracyMeters: 500 } },
  { nombre: 'Santiago de Surco', coords: { latitude: -12.1466, longitude: -76.9942, accuracyMeters: 500 } },
  { nombre: 'San Borja', coords: { latitude: -12.1084, longitude: -77.0016, accuracyMeters: 500 } },
  { nombre: 'Los Olivos', coords: { latitude: -11.9614, longitude: -77.0694, accuracyMeters: 500 } },
  { nombre: 'Lima Cercado', coords: { latitude: -12.0464, longitude: -77.0428, accuracyMeters: 500 } },
  { nombre: 'San Miguel', coords: { latitude: -12.0772, longitude: -77.0858, accuracyMeters: 500 } },
  { nombre: 'Jesús María', coords: { latitude: -12.0722, longitude: -77.0483, accuracyMeters: 500 } },
  { nombre: 'La Molina', coords: { latitude: -12.0754, longitude: -76.9388, accuracyMeters: 500 } },
  { nombre: 'Barranco', coords: { latitude: -12.1497, longitude: -77.0211, accuracyMeters: 500 } },
];

@Injectable({
  providedIn: 'root'
})
export class GeolocationService {
  private readonly _state = signal<GeolocationState>({ status: 'idle' });
  readonly state = this._state.asReadonly();

  readonly hasCoordinates = computed(() => this._state().status === 'acquired');

  readonly coordinates = computed<GeoCoordinates | null>(() => {
    const current = this._state();
    return current.status === 'acquired' ? current.coords : null;
  });

  readonly availableDistricts = LIMA_DISTRICTS;

  /**
   * Solicita las coordenadas GPS mediante la API nativa de geolocalización.
   */
  requestCurrentPosition(): void {
    if (typeof navigator === 'undefined' || !('geolocation' in navigator)) {
      this._state.set({
        status: 'error',
        message: 'Tu navegador no soporta geolocalización GPS.'
      });
      return;
    }

    this._state.set({ status: 'requesting' });

    navigator.geolocation.getCurrentPosition(
      (position: GeolocationPosition) => {
        this._state.set({
          status: 'acquired',
          coords: {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            accuracyMeters: Math.round(position.coords.accuracy)
          }
        });
      },
      (error: GeolocationPositionError) => {
        if (error.code === error.PERMISSION_DENIED) {
          this._state.set({
            status: 'denied',
            fallbackReason: 'Permiso GPS denegado. Selecciona tu distrito manualmente para ubicar la emergencia.'
          });
        } else {
          this._state.set({
            status: 'error',
            message: error.message || 'No se pudo obtener la posición GPS actual.'
          });
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 30000
      }
    );
  }

  /**
   * Permite fijar un distrito de contingencia cuando el GPS está bloqueado o denegado.
   */
  setManualDistrict(districtName: string): void {
    const found = LIMA_DISTRICTS.find(d => d.nombre.toLowerCase() === districtName.toLowerCase());
    if (found) {
      this._state.set({
        status: 'acquired',
        coords: { ...found.coords }
      });
    }
  }

  /**
   * Fija coordenadas manuales directas.
   */
  setManualCoordinates(coords: GeoCoordinates): void {
    this._state.set({
      status: 'acquired',
      coords: {
        latitude: coords.latitude,
        longitude: coords.longitude,
        accuracyMeters: coords.accuracyMeters ?? 50
      }
    });
  }

  /**
   * Restablece el estado de geolocalización a idle.
   */
  reset(): void {
    this._state.set({ status: 'idle' });
  }
}
