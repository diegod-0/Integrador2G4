import { TestBed } from '@angular/core/testing';
import { AnimalCatalogService } from './animal-catalog.service';

describe('AnimalCatalogService', () => {
  let service: AnimalCatalogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AnimalCatalogService);
  });

  it('debe filtrar exclusivamente perros al establecer especie: "PERRO"', () => {
    service.updateEspecie('PERRO');

    const state = service.viewState();

    expect(state.status).toBe('success');

    if (state.status === 'success') {
      expect(state.data.length).toBeGreaterThan(0);
      expect(state.data.every((animal) => animal.especie === 'PERRO')).toBe(true);
    }
  });

  it('debe ordenar de menor a mayor distancia al activar la cercanía', () => {
    service.setUserLocation({
      latitude: -12.046374,
      longitude: -77.042793,
    });

    service.toggleCercania(true);

    const state = service.viewState();

    expect(state.status).toBe('success');

    if (state.status === 'success' && state.data.length > 1) {
      expect(state.data[0].distanciaKm!).toBeLessThanOrEqual(state.data[1].distanciaKm!);
    }
  });

  it('debe emitir viewState "empty" cuando no hay coincidencias', () => {
    service.updateEspecie('GATO');
    service.updateTamano('GRANDE');

    const state = service.viewState();

    expect(state.status).toBe('empty');
  });
});
