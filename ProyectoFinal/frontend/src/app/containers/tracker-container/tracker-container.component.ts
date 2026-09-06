import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-tracker-container',
  standalone: true,
  template: `
    <div class="container section-padding">
      <div class="module-header">
        <span class="badge badge-warning">HU02 · 5 Story Points</span>
        <h1>📍 Rescue Tracker en Tiempo Real</h1>
        <p class="subtitle">Seguimiento en vivo del estado y rescate de animales reportados.</p>
        <div class="lead-badge">
          <span>Responsable:</span> <strong>Pedro Cueto (Product Owner)</strong>
        </div>
      </div>

      <div class="card placeholder-card">
        <div class="card-icon">⏱️</div>
        <h3>Módulo Base Inicializado</h3>
        <p>Código recibido en URL: <code>{{ ticketCode() || 'TICK-DEMO-4829' }}</code></p>
        <p class="desc-text">En la rama <code>feat/HU-02-rescue-tracker</code> se implementará la línea de tiempo reactiva con estados (PENDIENTE ➔ ASIGNADO ➔ EN_CAMINO ➔ RESCATADO) conectada a <code>localStorage</code>.</p>
      </div>
    </div>
  `,
  styles: [`
    .section-padding { padding-top: var(--space-2xl); padding-bottom: var(--space-2xl); }
    .module-header { margin-bottom: var(--space-xl); }
    .subtitle { color: var(--text-secondary); font-size: 1.1rem; margin-top: var(--space-xs); }
    .lead-badge { margin-top: var(--space-md); font-size: 0.95rem; color: var(--color-warning); }
    .placeholder-card { text-align: center; padding: var(--space-2xl); max-width: 640px; margin: 0 auto; }
    .card-icon { font-size: 3rem; margin-bottom: var(--space-md); }
    .placeholder-card h3 { margin-bottom: var(--space-sm); }
    .desc-text { color: var(--text-secondary); font-size: 0.95rem; margin-top: var(--space-sm); }
    code { background: rgba(255,255,255,0.1); padding: 0.2rem 0.4rem; border-radius: var(--radius-sm); color: var(--color-primary); }
  `]
})
export class TrackerContainerComponent {
  private route = inject(ActivatedRoute);
  
  ticketCode(): string | null {
    return this.route.snapshot.paramMap.get('codigo');
  }
}
