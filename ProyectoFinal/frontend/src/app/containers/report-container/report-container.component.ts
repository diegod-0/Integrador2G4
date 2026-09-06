import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-report-container',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="container section-padding">
      <div class="module-header">
        <span class="badge badge-danger">HU01 · 8 Story Points</span>
        <h1>🚨 Reporte Ágil de Emergencias</h1>
        <p class="subtitle">Captura automática de coordenadas GPS, fotografía del animal y generación de ticket de auxilio.</p>
        <div class="lead-badge">
          <span>Responsable:</span> <strong>Diego Claros (Scrum Master & Lead Architect)</strong>
        </div>
      </div>

      <div class="card placeholder-card">
        <div class="card-icon">📍</div>
        <h3>Módulo Base Inicializado</h3>
        <p>El andamiaje de este contenedor está preparado. En la rama <code>feat/HU-01-reporte-agil-gps</code> se implementará el formulario reactivo con captura de geolocalización del navegador y subida de fotografías.</p>
      </div>
    </div>
  `,
  styles: [`
    .section-padding { padding-top: var(--space-2xl); padding-bottom: var(--space-2xl); }
    .module-header { margin-bottom: var(--space-xl); }
    .subtitle { color: var(--text-secondary); font-size: 1.1rem; margin-top: var(--space-xs); }
    .lead-badge { margin-top: var(--space-md); font-size: 0.95rem; color: var(--color-primary-light); }
    .placeholder-card { text-align: center; padding: var(--space-2xl); max-width: 640px; margin: 0 auto; }
    .card-icon { font-size: 3rem; margin-bottom: var(--space-md); }
    .placeholder-card h3 { margin-bottom: var(--space-sm); }
    .placeholder-card p { color: var(--text-secondary); font-size: 0.95rem; }
    code { background: rgba(255,255,255,0.1); padding: 0.2rem 0.4rem; border-radius: var(--radius-sm); color: var(--color-secondary); }
  `]
})
export class ReportContainerComponent {}
