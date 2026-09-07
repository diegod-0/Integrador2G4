import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { GeolocationService } from './geolocation.service';

describe('GeolocationService', () => {
  let service: GeolocationService;
  let mockGetCurrentPosition: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    mockGetCurrentPosition = vi.fn();

    // Asegurar que navigator.geolocation esté presente en el entorno de pruebas JSDOM
    Object.defineProperty(navigator, 'geolocation', {
      value: {
        getCurrentPosition: mockGetCurrentPosition,
      },
      writable: true,
      configurable: true,
    });

    TestBed.configureTestingModule({});
    service = TestBed.inject(GeolocationService);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe iniciar en estado idle y sin coordenadas', () => {
    expect(service.state().status).toBe('idle');
    expect(service.hasCoordinates()).toBe(false);
    expect(service.coordinates()).toBeNull();
  });

  it('debe actualizar a acquired cuando se selecciona un distrito de contingencia', () => {
    service.setManualDistrict('Miraflores');

    const state = service.state();
    expect(state.status).toBe('acquired');
    expect(service.hasCoordinates()).toBe(true);
    if (state.status === 'acquired') {
      expect(state.coords.latitude).toBeCloseTo(-12.1192, 3);
      expect(state.coords.longitude).toBeCloseTo(-77.0290, 3);
    }
  });

  it('debe transicionar a acquired al recibir coordenadas válidas de navigator.geolocation', () => {
    const mockPosition = {
      coords: {
        latitude: -12.1234,
        longitude: -77.0123,
        accuracy: 15,
        altitude: null,
        altitudeAccuracy: null,
        heading: null,
        speed: null,
      },
      timestamp: Date.now(),
    } as unknown as GeolocationPosition;

    mockGetCurrentPosition.mockImplementation((successCallback: PositionCallback) => {
      successCallback(mockPosition);
    });

    service.requestCurrentPosition();

    const state = service.state();
    expect(state.status).toBe('acquired');
    if (state.status === 'acquired') {
      expect(state.coords.latitude).toBe(-12.1234);
      expect(state.coords.longitude).toBe(-77.0123);
      expect(state.coords.accuracyMeters).toBe(15);
    }
  });

  it('debe capturar el error PERMISSION_DENIED y emitir el estado denied', () => {
    const mockError = {
      code: 1, // PERMISSION_DENIED
      message: 'User denied geolocation',
      PERMISSION_DENIED: 1,
      POSITION_UNAVAILABLE: 2,
      TIMEOUT: 3,
    } as GeolocationPositionError;

    mockGetCurrentPosition.mockImplementation(
      (_success: PositionCallback, errorCallback?: PositionErrorCallback | null) => {
        if (errorCallback) {
          errorCallback(mockError);
        }
      }
    );

    service.requestCurrentPosition();

    const state = service.state();
    expect(state.status).toBe('denied');
    if (state.status === 'denied') {
      expect(state.fallbackReason).toContain('Permiso GPS denegado');
    }
  });
});
