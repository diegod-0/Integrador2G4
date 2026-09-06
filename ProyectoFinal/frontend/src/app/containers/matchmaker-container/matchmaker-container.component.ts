import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-matchmaker-container',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="container section-padding">
      <div class="module-header">
        <span class="badge badge-accent">HU04 · 5 Story Points</span>
        <h1>✨ Asistente de Compatibilidad Matchmaker</h1>
        <p class="subtitle">Test interactivo de 3 pasos para descubrir qué mascota se adapta a tu estilo de vida.</p>
        <div class="lead-badge">
          <span>Responsable:</span> <strong>Elsa Riquelme (QA Lead / UX Developer)</strong>
        </div>
      </div>

      <div class="card placeholder-card">
        <div class="card-icon">🎯</div>
        <h3>Módulo Base Inicializado</h3>
        <p>En la rama <code>feat/HU-04-matchmaker-wizard</code> se implementará el formulario Wizard de 3 preguntas (tipo de vivienda, tiempo disponible y nivel de energía) con navegación accesible y cálculo de afinidad.</p>
        <div class="action-footer">
          <a routerLink="/catalogo" class="btn btn-outline">Ver Todo el Catálogo</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .section-padding { padding-top: var(--space-2xl); padding-bottom: var(--space-2xl); }
    .module-header { margin-bottom: var(--space-xl); }
    .subtitle { color: var(--text-secondary); font-size: 1.1rem; margin-top: var(--space-xs); }
    .badge-accent { background: rgba(139, 92, 246, 0.15); color: var(--color-accent); border: 1px solid rgba(139, 92, 246, 0.3); }
    .lead-badge { margin-top: var(--space-md); font-size: 0.95rem; color: var(--color-accent); }
    .placeholder-card { text-align: center; padding: var(--space-2xl); max-width: 680px; margin: 0 auto; }
    .card-icon { font-size: 3rem; margin-bottom: var(--space-md); }
    .placeholder-card h3 { margin-bottom: var(--space-sm); }
    .placeholder-card p { color: var(--text-secondary); font-size: 0.95rem; }
    .action-footer { margin-top: var(--space-xl); }
    code { background: rgba(255,255,255,0.1); padding: 0.2rem 0.4rem; border-radius: var(--radius-sm); color: var(--color-accent); }
  `]
})
export class MatchmakerContainerComponent {}
