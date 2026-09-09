import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { trackingMockTickets, TRACKING_STORAGE_KEY } from '../mocks/tracking.mocks';
import { TrackingService } from './tracking.service';

describe('TrackingService', () => {
  let service: TrackingService;
  let store: Record<string, string>;

  beforeEach(() => {
    vi.useFakeTimers();
    store = {};
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: vi.fn((key: string) => store[key] ?? null),
        setItem: vi.fn((key: string, value: string) => { store[key] = value; }),
      },
      configurable: true,
    });
    TestBed.configureTestingModule({});
    service = TestBed.inject(TrackingService);
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
    TestBed.resetTestingModule();
  });

  it('inicia en idle', () => {
    expect(service.state().status).toBe('idle');
  });

  it('resuelve un ticket seed después de simular 300 ms', () => {
    service.load('TICK-1024');

    expect(service.state().status).toBe('loading');
    vi.advanceTimersByTime(300);

    const state = service.state();
    expect(state.status).toBe('success');
    if (state.status === 'success') {
      expect(state.data).toBe(trackingMockTickets[0]);
    }
  });

  it('prioriza el ticket persistido sobre el fixture demo', () => {
    const localTicket = { ...trackingMockTickets[0], reportanteNombre: 'Persistido' };
    store[TRACKING_STORAGE_KEY] = JSON.stringify([localTicket]);

    service.load(localTicket.codigoSeguimiento);
    vi.advanceTimersByTime(300);

    const state = service.state();
    expect(state.status).toBe('success');
    if (state.status === 'success') {
      expect(state.data.reportanteNombre).toBe('Persistido');
    }
  });

  it('emite empty para un código inexistente', () => {
    service.load('TICK-9999');
    vi.advanceTimersByTime(300);

    expect(service.state().status).toBe('empty');
  });

  it('emite error cuando el JSON persistido es inválido', () => {
    store[TRACKING_STORAGE_KEY] = '{invalid';

    service.load('TICK-1024');
    vi.advanceTimersByTime(300);

    expect(service.state().status).toBe('error');
  });
});
