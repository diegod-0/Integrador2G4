import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { CatalogContainerComponent } from './catalog-container.component';
import { AnimalCatalogService } from '../../services/animal-catalog.service';
import { GeolocationService } from '../../services/geolocation.service';

describe('CatalogContainerComponent', () => {
  let component: CatalogContainerComponent;
  let fixture: ComponentFixture<CatalogContainerComponent>;
  let catalogService: AnimalCatalogService;
  let geoService: GeolocationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [CatalogContainerComponent],
      providers: [AnimalCatalogService, GeolocationService],
    });

    fixture = TestBed.createComponent(CatalogContainerComponent);
    component = fixture.componentInstance;
    catalogService = TestBed.inject(AnimalCatalogService);
    geoService = TestBed.inject(GeolocationService);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    TestBed.resetTestingModule();
  });

  it('debe crearse correctamente y mostrar el catálogo completo por defecto', () => {
    expect(component).toBeTruthy();
    const state = catalogService.viewState();
    expect(state.status).toBe('success');
    if (state.status === 'success') {
      expect(state.data.length).toBe(6);
    }
  });

  it('debe delegar el filtro de especie a AnimalCatalogService', () => {
    const spy = vi.spyOn(catalogService, 'updateEspecie');
    component.onEspecieChange('PERRO');
    expect(spy).toHaveBeenCalledWith('PERRO');
  });

  it('debe delegar el filtro de tamaño a AnimalCatalogService', () => {
    const spy = vi.spyOn(catalogService, 'updateTamano');
    component.onTamanoChange('GRANDE');
    expect(spy).toHaveBeenCalledWith('GRANDE');
  });

  it('debe solicitar geolocalización al activar "Cerca de mí" (Escenario 1 BDD)', () => {
    const spy = vi.spyOn(geoService, 'requestCurrentPosition');
    component.onCercaniaToggle(true);
    expect(catalogService.filtroActual().ordenarPorCercania).toBe(true);
    expect(spy).toHaveBeenCalled();
  });

  it('debe reordenar por cercanía cuando GeolocationService adquiere coordenadas', () => {
    component.onCercaniaToggle(true);
    geoService.setManualCoordinates({ latitude: -12.046374, longitude: -77.042793 });
    fixture.detectChanges();

    const state = catalogService.viewState();
    expect(state.status).toBe('success');
    if (state.status === 'success' && state.data.length > 1) {
      const distancias = state.data.map((a) => a.distanciaKm ?? 0);
      const ordenado = [...distancias].sort((a, b) => a - b);
      expect(distancias).toEqual(ordenado);
    }
  });

  it('debe resetear el servicio de geolocalización al desactivar "Cerca de mí"', () => {
    const spy = vi.spyOn(geoService, 'reset');
    component.onCercaniaToggle(true);
    component.onCercaniaToggle(false);
    expect(spy).toHaveBeenCalled();
    expect(catalogService.filtroActual().ordenarPorCercania).toBe(false);
  });

  it('debe volver al orden por defecto si el permiso GPS es denegado (Escenario 3 BDD)', () => {
    const mockGeolocation = {
      getCurrentPosition: vi.fn(
        (_success: PositionCallback, error?: PositionErrorCallback | null) => {
          error?.({
            code: 1,
            PERMISSION_DENIED: 1,
            POSITION_UNAVAILABLE: 2,
            TIMEOUT: 3,
            message: 'Permiso denegado',
          } as GeolocationPositionError);
        },
      ),
    };

    Object.defineProperty(navigator, 'geolocation', {
      value: mockGeolocation,
      configurable: true,
    });

    component.onCercaniaToggle(true);
    fixture.detectChanges();

    expect(catalogService.filtroActual().ordenarPorCercania).toBe(false);
    const state = catalogService.viewState();
    expect(state.status).toBe('success'); // vuelve al orden alfabético por defecto, no falla
  });
});
