import { TicketRescate } from '../models/emergency-report.model';

/** Shared storage contract used by the report and tracker flows. */
export const TRACKING_STORAGE_KEY = 'rescuelink_tickets';

export const pendingTicketMock: TicketRescate = {
  id: 'mock-ticket-1024',
  codigoSeguimiento: 'TICK-1024',
  reportanteNombre: 'Mariana Silva',
  reportanteTelefono: '+51 999 102 400',
  coordenadas: { latitude: -12.0464, longitude: -77.0428, accuracyMeters: 8 },
  direccionReferencia: 'Av. Arequipa 1024, Lima',
  fotoUrl: 'https://images.example.test/rescue/tick-1024.jpg',
  nivelUrgencia: 'MEDIA',
  estado: 'PENDIENTE',
  fechaCreacion: '2026-09-08T09:15:00.000Z',
  historial: [
    {
      estado: 'PENDIENTE',
      titulo: 'Emergencia Reportada',
      descripcion:
        'Alerta registrada por ciudadano. Notificando a albergues y voluntarios en la zona.',
      completado: true,
      actual: true,
      fechaHora: '09:15',
    },
    {
      estado: 'ASIGNADO',
      titulo: 'Albergue Asignado',
      descripcion: 'Albergue con cupos disponibles aceptara la recepcion del caso.',
      completado: false,
      actual: false,
    },
    {
      estado: 'EN_CAMINO',
      titulo: 'Rescatista en Camino',
      descripcion: 'Voluntario desplazandose a las coordenadas GPS.',
      completado: false,
      actual: false,
    },
    {
      estado: 'RESCATADO',
      titulo: 'Animal Asegurado y a Salvo',
      descripcion: 'Ingreso al refugio y triaje medico veterinario.',
      completado: false,
      actual: false,
    },
  ],
};

export const enRouteTicketMock: TicketRescate = {
  id: 'mock-ticket-2048',
  codigoSeguimiento: 'TICK-2048',
  reportanteNombre: 'Jorge Ramos',
  reportanteTelefono: '+51 988 204 800',
  coordenadas: { latitude: -12.0722, longitude: -77.0966, accuracyMeters: 12 },
  direccionReferencia: 'Jiron de la Union 2048, Lima',
  fotoUrl: 'https://images.example.test/rescue/tick-2048.jpg',
  nivelUrgencia: 'CRITICA',
  estado: 'EN_CAMINO',
  albergueAsignado: 'Albergue RescueLink Centro',
  voluntarioAsignado: 'Lucia Torres',
  fechaCreacion: '2026-09-08T07:40:00.000Z',
  historial: [
    {
      estado: 'PENDIENTE',
      titulo: 'Emergencia Reportada',
      descripcion: 'Alerta registrada por ciudadano.',
      completado: true,
      actual: false,
      fechaHora: '07:40',
    },
    {
      estado: 'ASIGNADO',
      titulo: 'Albergue Asignado',
      descripcion: 'El caso fue aceptado por un albergue disponible.',
      completado: true,
      actual: false,
      fechaHora: '07:48',
    },
    {
      estado: 'EN_CAMINO',
      titulo: 'Rescatista en Camino',
      descripcion: 'La voluntaria se desplaza a la ubicacion reportada.',
      completado: true,
      actual: true,
      fechaHora: '08:02',
    },
    {
      estado: 'RESCATADO',
      titulo: 'Animal Asegurado y a Salvo',
      descripcion: 'Ingreso al refugio y triaje medico veterinario.',
      completado: false,
      actual: false,
    },
  ],
};

export const rescuedTicketMock: TicketRescate = {
  id: 'mock-ticket-3072',
  codigoSeguimiento: 'TICK-3072',
  reportanteNombre: 'Camila Ortiz',
  reportanteTelefono: '+51 977 307 200',
  coordenadas: { latitude: -12.1098, longitude: -77.0341, accuracyMeters: 5 },
  direccionReferencia: 'Parque Kennedy, Miraflores',
  fotoUrl: 'https://images.example.test/rescue/tick-3072.jpg',
  nivelUrgencia: 'BAJA',
  estado: 'RESCATADO',
  albergueAsignado: 'Albergue RescueLink Sur',
  voluntarioAsignado: 'Diego Perez',
  fechaCreacion: '2026-09-07T16:20:00.000Z',
  historial: [
    {
      estado: 'PENDIENTE',
      titulo: 'Emergencia Reportada',
      descripcion: 'Alerta registrada por ciudadano.',
      completado: true,
      actual: false,
      fechaHora: '16:20',
    },
    {
      estado: 'ASIGNADO',
      titulo: 'Albergue Asignado',
      descripcion: 'El caso fue aceptado por un albergue disponible.',
      completado: true,
      actual: false,
      fechaHora: '16:31',
    },
    {
      estado: 'EN_CAMINO',
      titulo: 'Rescatista en Camino',
      descripcion: 'El voluntario se desplazo a las coordenadas GPS.',
      completado: true,
      actual: false,
      fechaHora: '16:45',
    },
    {
      estado: 'RESCATADO',
      titulo: 'Animal Asegurado y a Salvo',
      descripcion: 'Ingreso al refugio y triaje medico veterinario completados.',
      completado: true,
      actual: true,
      fechaHora: '17:05',
    },
  ],
};

export const unresolvedTicketMock: TicketRescate = {
  id: 'mock-ticket-4096',
  codigoSeguimiento: 'TICK-4096',
  reportanteNombre: 'Luis Vega',
  reportanteTelefono: '+51 966 409 600',
  coordenadas: { latitude: -12.055, longitude: -77.01 },
  direccionReferencia: 'Referencia no confirmada',
  fotoUrl: '',
  nivelUrgencia: 'MEDIA',
  estado: 'NO_LOCALIZADO',
  fechaCreacion: '2026-09-06T11:00:00.000Z',
  historial: [
    {
      estado: 'PENDIENTE',
      titulo: 'Emergencia Reportada',
      descripcion: 'Alerta registrada por ciudadano.',
      completado: true,
      actual: false,
      fechaHora: '11:00',
    },
    {
      estado: 'NO_LOCALIZADO',
      titulo: 'Ubicacion no Confirmada',
      descripcion: 'No fue posible localizar al animal con la informacion disponible.',
      completado: true,
      actual: true,
      fechaHora: '11:35',
    },
  ],
};

/** Representative tickets for tracker rendering and localStorage tests. */
export const trackingMockTickets: TicketRescate[] = [
  pendingTicketMock,
  enRouteTicketMock,
  rescuedTicketMock,
  unresolvedTicketMock,
];

/** Empty storage fixture for loading/empty-state scenarios. */
export const trackingEmptyTickets: TicketRescate[] = [];

/** Route value that intentionally has no matching ticket in the fixtures. */
export const trackingMissingTicketCode = 'TICK-9999';
