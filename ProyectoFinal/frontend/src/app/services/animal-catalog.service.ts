
import { Injectable, computed, inject, signal } from '@angular/core';
import { AnimalAdopcion, FiltroCatalogo } from '../models/animal.model';
import { GeoCoordinates } from '../models/emergency-report.model';
import { ViewState } from '../models/view-state.model';
import { calculateHaversineDistance } from '../utils/haversine';
import { ANIMALS_MOCK } from '../mocks/animals.mock';
import { MatchmakerService } from './matchmaker.service';

const FILTRO_INICIAL: FiltroCatalogo = {
  especie: 'TODOS',
  tamano: 'TODOS',
  ordenarPorCercania: false,
};

@Injectable({ providedIn: 'root' })
export class AnimalCatalogService {
  private readonly matchmakerService = inject(MatchmakerService);
  private readonly userLocation = signal<GeoCoordinates | null>(null);
  private readonly filtros = signal<FiltroCatalogo>(FILTRO_INICIAL);
  readonly filtroActual = this.filtros.asReadonly();

  readonly viewState = computed<ViewState<AnimalAdopcion[]>>(() => {
    const ubicacion = this.userLocation();
    const filtro = this.filtros();

    let resultado: AnimalAdopcion[] = ANIMALS_MOCK.map((animal) => ({
      ...animal,
      distanciaKm: ubicacion
        ? calculateHaversineDistance(ubicacion, animal.coordenadas)
        : undefined,
    }));

    if (filtro.especie !== 'TODOS') {
      resultado = resultado.filter((a) => a.especie === filtro.especie);
    }
    if (filtro.tamano !== 'TODOS') {
      resultado = resultado.filter((a) => a.tamano === filtro.tamano);
    }

    if (this.matchmakerService.isWizardActive()) {
      const scores = this.matchmakerService.topScores();
      resultado = resultado
        .map((animal) => ({ ...animal, compatibilityScore: scores[animal.id] }))
        .sort((a, b) => (b.compatibilityScore ?? 0) - (a.compatibilityScore ?? 0));
    } else if (filtro.ordenarPorCercania && ubicacion) {
      resultado = [...resultado].sort((a, b) => (a.distanciaKm ?? 0) - (b.distanciaKm ?? 0));
    } else {
      resultado = [...resultado].sort((a, b) => a.nombre.localeCompare(b.nombre));
    }

    if (resultado.length === 0) {
      return {
        status: 'empty',
        message:
          'No se encontraron mascotas con estos filtros. Prueba ampliando el radio de distancia.',
      };
    }

    return { status: 'success', data: resultado };
  });

  setUserLocation(coords: GeoCoordinates | null): void {
    this.userLocation.set(coords);
  }

  updateEspecie(especie: FiltroCatalogo['especie']): void {
    this.filtros.update((f) => ({ ...f, especie }));
  }

  updateTamano(tamano: FiltroCatalogo['tamano']): void {
    this.filtros.update((f) => ({ ...f, tamano }));
  }

  toggleCercania(activo: boolean): void {
    this.filtros.update((f) => ({ ...f, ordenarPorCercania: activo }));

    if (!activo) {
      // Escenario 3 de HU03: si se desactiva o falla el permiso, vuelve a orden por defecto
      return;
    }
  }

  clearMatchmaker(): void {
    this.matchmakerService.clearAffinity();
  }
}
