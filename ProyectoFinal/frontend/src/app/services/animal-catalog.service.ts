
import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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

const ALBERGUES_METADATA: Record<string, { coordenadas: GeoCoordinates; distrito: string }> = {
  'b0000000-0000-0000-0000-000000000001': {
    coordenadas: { latitude: -12.1221, longitude: -77.0315 },
    distrito: 'Miraflores'
  },
  'b0000000-0000-0000-0000-000000000002': {
    coordenadas: { latitude: -12.1460, longitude: -76.9950 },
    distrito: 'Santiago de Surco'
  },
  'b0000000-0000-0000-0000-000000000003': {
    coordenadas: { latitude: -11.9920, longitude: -77.0680 },
    distrito: 'Los Olivos'
  }
};

function resolverAlbergueData(albergueId?: string, albergueNombre?: string): { coordenadas: GeoCoordinates; distrito: string } {
  if (albergueId && ALBERGUES_METADATA[albergueId]) {
    return ALBERGUES_METADATA[albergueId];
  }
  const nombre = (albergueNombre || '').toLowerCase();
  if (nombre.includes('surco')) {
    return ALBERGUES_METADATA['b0000000-0000-0000-0000-000000000002'];
  }
  if (nombre.includes('olivos')) {
    return ALBERGUES_METADATA['b0000000-0000-0000-0000-000000000003'];
  }
  return ALBERGUES_METADATA['b0000000-0000-0000-0000-000000000001'];
}

@Injectable({ providedIn: 'root' })
export class AnimalCatalogService {
  private readonly matchmakerService = inject(MatchmakerService);
  private readonly http = inject(HttpClient, { optional: true });
  private readonly userLocation = signal<GeoCoordinates | null>(null);
  private readonly filtros = signal<FiltroCatalogo>(FILTRO_INICIAL);
  private readonly rawAnimals = signal<AnimalAdopcion[]>(ANIMALS_MOCK);

  readonly filtroActual = this.filtros.asReadonly();

  constructor() {
    this.sincronizarConBackend();
  }

  private sincronizarConBackend(): void {
    if (!this.http) return;

    this.http.get<{ data?: any[] }>('http://localhost:8080/api/animales/adopcion').subscribe({
      next: (res) => {
        const items = res?.data ?? (Array.isArray(res) ? res : null);
        if (items && items.length > 0) {
          const mapeados: AnimalAdopcion[] = items.map((item, idx) => {
            const albData = resolverAlbergueData(item.albergueId, item.albergueNombre);
            return {
              id: item.id ? String(item.id) : `a${idx + 1}`,
              nombre: item.nombre,
              especie: item.especie === 'CANINO' ? 'PERRO' : item.especie === 'FELINO' ? 'GATO' : item.especie,
              raza: item.raza ?? (item.especie === 'CANINO' ? 'Mestizo' : 'Común'),
              edadAnios: item.edadEstimada ? parseInt(String(item.edadEstimada).replace(/\D/g, ''), 10) || 2 : 2,
              tamano: item.peso && item.peso > 18 ? 'GRANDE' : item.peso && item.peso > 8 ? 'MEDIANO' : 'PEQUENO',
              sexo: item.sexo ?? 'MACHO',
              fotoUrl: item.fotoPerfilUrl ?? item.fotoAntesUrl ?? 'https://images.unsplash.com/photo-1543466835-00a7907e9de1',
              descripcion: item.descripcion ?? `Mascota rescatada en adopción en ${item.albergueNombre}`,
              albergueNombre: item.albergueNombre ?? 'Albergue Aliado',
              coordenadas: item.coordenadas ?? albData.coordenadas,
              distrito: item.distrito ?? albData.distrito,
              energia: item.nivelEnergia === 'ALTO' ? 'ACTIVO' : item.nivelEnergia === 'BAJO' ? 'TRANQUILO' : 'MODERADO',
              espacioRequerido: item.aptoDepartamento ? 'DEPARTAMENTO' : 'CASA_PATIO',
              tiempoRequerido: item.nivelEnergia === 'ALTO' ? 'ALTO' : item.nivelEnergia === 'BAJO' ? 'BAJO' : 'MEDIO',
            };
          });
          this.rawAnimals.set(mapeados);
        }
      },
      error: () => {
        // Fallback transparente al catálogo sincronizado con BD
      }
    });
  }

  readonly viewState = computed<ViewState<AnimalAdopcion[]>>(() => {
    const ubicacion = this.userLocation();
    const filtro = this.filtros();

    let resultado: AnimalAdopcion[] = this.rawAnimals().map((animal) => ({
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
