import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-catalog-container',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="container section-padding">
      <div class="module-header">
        <span class="badge badge-info">HU03 · 5 Story Points</span>
        <h1>🐾 Catálogo Unificado de Adopción</h1>
        <p class="subtitle">Exploración de animales rescatados con ordenamiento geodésico ("Cerca de mí") y filtros avanzados.</p>
        <div class="lead-badge">
          <span>Responsable:</span> <strong>Anghelo Mendoza (Business Analyst)</strong>
        </div>
      </div>

      <div class="card placeholder-card">
        <div class="card-icon">🐕</div>
        <h3>Módulo Base Inicializado</h3>
        <p>En la rama <code>feat/HU-03-catalogo-cercania-gps</code> se implementará el grid reactivo de mascotas, el cálculo de distancias en kilómetros con la fórmula de Haversine y los filtros combinados por especie y tamaño.</p>
        <div class="matchmaker-banner">
          <p>¿No sabés qué mascota se adapta a tu rutina diaria?</p>
          <a routerLink="/matchmaker" class="btn btn-primary btn-sm">Hacer Test Matchmaker ✨</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .section-padding { padding-top: var(--space-2xl); padding-bottom: var(--space-2xl); }
    .module-header { margin-bottom: var(--space-xl); }
    .subtitle { color: var(--text-secondary); font-size: 1.1rem; margin-top: var(--space-xs); }
    .lead-badge { margin-top: var(--space-md); font-size: 0.95rem; color: var(--color-info); }
    .placeholder-card { text-align: center; padding: var(--space-2xl); max-width: 680px; margin: 0 auto; }
    .card-icon { font-size: 3rem; margin-bottom: var(--space-md); }
    .placeholder-card h3 { margin-bottom: var(--space-sm); }
    .placeholder-card p { color: var(--text-secondary); font-size: 0.95rem; }
    .matchmaker-banner { margin-top: var(--space-xl); padding-top: var(--space-md); border-top: 1px solid var(--border-color); display: flex; flex-direction: column; align-items: center; gap: 0.75rem; }
    .btn-sm { font-size: 0.85rem; padding: 0.4rem 1rem; }
    code { background: rgba(255,255,255,0.1); padding: 0.2rem 0.4rem; border-radius: var(--radius-sm); color: var(--color-primary); }
  `]
})
export class CatalogContainerComponent {}
