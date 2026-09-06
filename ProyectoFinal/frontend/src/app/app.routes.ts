import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'catalogo',
    pathMatch: 'full'
  },
  {
    path: 'reportar',
    loadComponent: () =>
      import('./containers/report-container/report-container.component').then(
        m => m.ReportContainerComponent
      ),
    title: 'Reportar Emergencia · RescueLink'
  },
  {
    path: 'tracking',
    loadComponent: () =>
      import('./containers/tracker-container/tracker-container.component').then(
        m => m.TrackerContainerComponent
      ),
    title: 'Rescue Tracker · RescueLink'
  },
  {
    path: 'tracking/:codigo',
    loadComponent: () =>
      import('./containers/tracker-container/tracker-container.component').then(
        m => m.TrackerContainerComponent
      ),
    title: 'Seguimiento de Ticket · RescueLink'
  },
  {
    path: 'catalogo',
    loadComponent: () =>
      import('./containers/catalog-container/catalog-container.component').then(
        m => m.CatalogContainerComponent
      ),
    title: 'Catálogo de Adopción · RescueLink'
  },
  {
    path: 'matchmaker',
    loadComponent: () =>
      import(
        './containers/matchmaker-container/matchmaker-container.component'
      ).then(m => m.MatchmakerContainerComponent),
    title: 'Test Matchmaker · RescueLink'
  },
  {
    path: '**',
    redirectTo: 'catalogo'
  }
];
