import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { vi } from 'vitest';
import { ReportContainerComponent } from './report-container.component';
import { GeolocationService } from '../../services/geolocation.service';
import { EmergencyReportPayload } from '../../models/emergency-report.model';

describe('ReportContainerComponent', () => {
  let component: ReportContainerComponent;
  let fixture: ComponentFixture<ReportContainerComponent>;
  let routerNavigateSpy: ReturnType<typeof vi.fn>;
  let geoService: GeolocationService;
  let store: Record<string, string>;

  beforeEach(() => {
    vi.useFakeTimers();
    routerNavigateSpy = vi.fn();
    store = {};

    // Mock seguro y robusto para localStorage independiente del runtime de Node
    const mockStorage = {
      getItem: vi.fn((key: string) => store[key] ?? null),
      setItem: vi.fn((key: string, value: string) => { store[key] = value; }),
      removeItem: vi.fn((key: string) => { delete store[key]; }),
      clear: vi.fn(() => { store = {}; })
    };

    Object.defineProperty(window, 'localStorage', {
      value: mockStorage,
      writable: true,
      configurable: true
    });

    TestBed.configureTestingModule({
      imports: [ReportContainerComponent],
      providers: [
        { provide: Router, useValue: { navigate: routerNavigateSpy } },
        GeolocationService
      ]
    });

    fixture = TestBed.createComponent(ReportContainerComponent);
    component = fixture.componentInstance;
    geoService = TestBed.inject(GeolocationService);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
    TestBed.resetTestingModule();
  });

  it('debe crearse correctamente con estado inicial idle', () => {
    expect(component).toBeTruthy();
    expect(component.viewState().status).toBe('idle');
  });

  it('debe delegar la solicitud de GPS a GeolocationService', () => {
    const geoSpy = vi.spyOn(geoService, 'requestCurrentPosition');
    component.onGpsRequested();
    expect(geoSpy).toHaveBeenCalled();
  });

  it('debe delegar la selección de distrito a GeolocationService', () => {
    const districtSpy = vi.spyOn(geoService, 'setManualDistrict');
    component.onDistrictSelected('Miraflores');
    expect(districtSpy).toHaveBeenCalledWith('Miraflores');
  });

  it('debe simular latencia, persistir en localStorage y navegar al tracking', () => {
    const mockPayload: EmergencyReportPayload = {
      reporterName: 'Diego Claros',
      reporterPhone: '987654321',
      referenceAddress: 'Parque Kennedy, Miraflores',
      conditionDescription: 'Perrito herido',
      urgencyLevel: 'CRITICA',
      imageUrl: 'data:image/png;base64,mock',
      coordinates: { latitude: -12.1192, longitude: -77.0290, accuracyMeters: 10 }
    };

    component.onReportSubmitted(mockPayload);

    expect(component.viewState().status).toBe('loading');

    // Avanzar reloj simulado (400 ms)
    vi.advanceTimersByTime(400);

    expect(component.viewState().status).toBe('success');

    // Verificar persistencia en localStorage simulado
    expect(store['rescuelink_tickets']).toBeDefined();
    const storedTickets = JSON.parse(store['rescuelink_tickets']);
    expect(storedTickets.length).toBeGreaterThanOrEqual(1);
    expect(storedTickets[0].reportanteNombre).toBe('Diego Claros');
    expect(storedTickets[0].codigoSeguimiento).toMatch(/^TICK-\d{4}$/);

    // Verificar redirección del Router
    expect(routerNavigateSpy).toHaveBeenCalledWith(['/tracking', storedTickets[0].codigoSeguimiento]);
  });
});
