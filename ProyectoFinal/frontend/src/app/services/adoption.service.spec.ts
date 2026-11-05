import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdoptionService } from './adoption.service';
import { PostularAdopcionPayload } from '../models/adoption.model';

describe('AdoptionService (ISO 25010 - Interoperabilidad, Idempotencia y Resiliencia)', () => {
  let service: AdoptionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    const store: Record<string, string> = {};
    const mockStorage = {
      getItem: (k: string) => store[k] ?? null,
      setItem: (k: string, v: string) => { store[k] = v; },
      removeItem: (k: string) => { delete store[k]; },
      clear: () => { Object.keys(store).forEach(k => delete store[k]); },
      length: 0,
      key: (i: number) => null,
    };
    try {
      Object.defineProperty(globalThis, 'localStorage', { value: mockStorage, configurable: true, writable: true });
    } catch (_) {}
    try {
      Object.defineProperty(window, 'localStorage', { value: mockStorage, configurable: true, writable: true });
    } catch (_) {}

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), AdoptionService],
    });
    service = TestBed.inject(AdoptionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe enviar postulación con cabecera Idempotency-Key hacia /api/adopciones/postular', () => {
    const payload: PostularAdopcionPayload = {
      animalId: '10000000-0000-0000-0000-000000000001',
      nombreAdoptante: 'Valeria Gómez',
      emailAdoptante: 'valeria@ejemplo.pe',
      telefonoAdoptante: '987654321',
      tipoVivienda: 'CASA_CON_JARDIN',
      tieneOtrasMascotas: false,
      motivo: 'Cuento con patio seguro y presupuesto veterinario.',
      aceptaTerminosLey30407: true,
    };

    let responseState = '';
    service.postularAdopcion(payload, 'Rocky').subscribe(res => {
      responseState = res.estado;
    });

    const req = httpMock.expectOne('/api/adopciones/postular');
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Idempotency-Key')).toBe(
      'idem-10000000-0000-0000-0000-000000000001-valeria@ejemplo.pe'
    );

    req.flush({
      success: true,
      message: 'OK',
      data: {
        id: 'sol-001',
        animalId: payload.animalId,
        nombreAnimal: 'Rocky',
        nombreAdoptante: payload.nombreAdoptante,
        emailAdoptante: payload.emailAdoptante,
        estado: 'RECIBIDA',
        tipoVivienda: payload.tipoVivienda,
        tieneOtrasMascotas: false,
        codigoCertificadoQr: 'CERT-RL-123456',
        estadoNotificacionExterna: 'DELIVERED',
        externalMessageId: 'msg-01',
        fechaSolicitud: new Date().toISOString(),
      },
    });

    expect(responseState).toBe('RECIBIDA');
  });

  it('debe aplicar degradación elegante (fallback local) si el backend responde error 503', () => {
    const payload: PostularAdopcionPayload = {
      animalId: '10000000-0000-0000-0000-000000000002',
      nombreAdoptante: 'Carlos Díaz',
      emailAdoptante: 'carlos@ejemplo.pe',
      telefonoAdoptante: '998877665',
      tipoVivienda: 'DEPARTAMENTO_CON_BALCON',
      tieneOtrasMascotas: true,
      motivo: 'Experiencia previa con mascotas rescatadas.',
      aceptaTerminosLey30407: true,
    };

    let fallbackNotification = '';
    service.postularAdopcion(payload, 'Luna').subscribe(res => {
      fallbackNotification = res.estadoNotificacionExterna;
    });

    const req = httpMock.expectOne('/api/adopciones/postular');
    req.flush('Service Unavailable', { status: 503, statusText: 'Service Unavailable' });

    expect(fallbackNotification).toBe('SIMULATED_FALLBACK');
  });
});

