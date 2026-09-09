import { Component, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

type Housing = 'DEPARTAMENTO' | 'CASA_PATIO';
type Time = 'BAJO' | 'MEDIO' | 'ALTO';
type Energy = 'TRANQUILO' | 'MODERADO' | 'ACTIVO';
type Species = 'PERRO' | 'GATO';
type Answers = {
  housing: Housing;
  time: Time;
  energy: Energy;
  species: Species;
};

@Component({
  selector: 'app-matchmaker-container',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="container section-padding">
      <div class="module-header">
        <span class="badge badge-accent">HU04 · 5 Story Points</span>
        <h1>✨ Asistente de Compatibilidad Matchmaker</h1>
        <p class="subtitle">Test para pasos para descubrir qué mascota se adapta a tu estilo de vida.</p>

      </div>

      


      @if (!recommendation()) {
        <div class="card wizard-card">
          <div class="progress-row">
            <span>Paso {{ step() }} de {{ totalSteps }}</span>
            <span>{{ progress() }}%</span>
          </div>
          <div class="progress-bar"><span [style.width.%]="progress()"></span></div>

          @if (step() === 1) {
            <fieldset>
              <legend>¿Dónde vives?</legend>
              <p class="question-help">Elige la opción que más se parece a tu hogar.</p>
              <div class="option-grid">
                <button type="button" class="option" [class.selected]="answers().housing === 'DEPARTAMENTO'" (click)="select('housing', 'DEPARTAMENTO')">
                  <span class="option-icon">🏢</span><strong>Departamento</strong><small>Espacio más reducido</small>
                </button>
                <button type="button" class="option" [class.selected]="answers().housing === 'CASA_PATIO'" (click)="select('housing', 'CASA_PATIO')">
                  <span class="option-icon">🏡</span><strong>Casa con patio</strong><small>Espacio amplio o exterior</small>
                </button>
              </div>
            </fieldset>
          }

          @if (step() === 2) {
            <fieldset>
              <legend>¿Cuánto tiempo tienes para dedicarle?</legend>
              <p class="question-help">Considera paseos, juegos, cuidados y compañía.</p>
              <div class="option-grid">
                <button type="button" class="option" [class.selected]="answers().time === 'BAJO'" (click)="select('time', 'BAJO')">
                  <span class="option-icon">🕐</span><strong>Poco tiempo</strong><small>Rutina ocupada</small>
                </button>
                <button type="button" class="option" [class.selected]="answers().time === 'MEDIO'" (click)="select('time', 'MEDIO')">
                  <span class="option-icon">🕑</span><strong>Tiempo moderado</strong><small>Algunos momentos al día</small>
                </button>
                <button type="button" class="option" [class.selected]="answers().time === 'ALTO'" (click)="select('time', 'ALTO')">
                  <span class="option-icon">🕒</span><strong>Mucho tiempo</strong><small>Rutina flexible</small>
                </button>
              </div>
            </fieldset>
          }

          @if (step() === 3) {
            <fieldset>
              <legend>¿Qué nivel de energía prefieres?</legend>
              <p class="question-help">Piensa en el ritmo de vida que te gustaría compartir.</p>
              <div class="option-grid">
                <button type="button" class="option" [class.selected]="answers().energy === 'TRANQUILO'" (click)="select('energy', 'TRANQUILO')">
                  <span class="option-icon">🛋️</span><strong>Tranquilo</strong><small>Le gusta descansar</small>
                </button>
                <button type="button" class="option" [class.selected]="answers().energy === 'MODERADO'" (click)="select('energy', 'MODERADO')">
                  <span class="option-icon">🚶</span><strong>Moderado</strong><small>Activo en algunos momentos</small>
                </button>
                <button type="button" class="option" [class.selected]="answers().energy === 'ACTIVO'" (click)="select('energy', 'ACTIVO')">
                  <span class="option-icon">⚽</span><strong>Activo</strong><small>Disfruta jugar y correr</small>
                </button>
              </div>
            </fieldset>
          }

          @if (step() === 4) {
            <fieldset>
              <legend>¿Qué mascota te gustaría conocer?</legend>
              <p class="question-help">Esto nos ayuda a orientar mejor la sugerencia.</p>
              <div class="option-grid">
                <button type="button" class="option" [class.selected]="answers().species === 'PERRO'" (click)="select('species', 'PERRO')">
                  <span class="option-icon">🐶</span><strong>Perro</strong><small>Compañero de paseos y juegos</small>
                </button>
                <button type="button" class="option" [class.selected]="answers().species === 'GATO'" (click)="select('species', 'GATO')">
                  <span class="option-icon">🐱</span><strong>Gato</strong><small>Compañero independiente</small>
                </button>
              </div>
            </fieldset>
          }

          <div class="wizard-actions">
            @if (step() > 1) {
              <button type="button" class="btn btn-outline" (click)="previousStep()">Atrás</button>
            }
            @if (step() < totalSteps) {
              <button type="button" class="btn btn-primary" [disabled]="!canContinue()" (click)="nextStep()">Siguiente</button>
            } @else {
              <button type="button" class="btn btn-primary" [disabled]="!canContinue()" (click)="showRecommendation()">Ver mi sugerencia</button>
            }
          </div>
        </div>
      } @else {
        <div class="card result-card">
          <div class="result-icon">{{ recommendation()!.icon }}</div>
          <span class="badge badge-success">Resultado orientativo</span>
          <h2>Podría encajar contigo un {{ recommendation()!.label }}</h2>
          <p>{{ recommendation()!.description }}</p>
          <div class="result-details">
            <span>📏 Tamaño sugerido: <strong>{{ recommendation()!.size }}</strong></span>
            <span>⚡ Energía: <strong>{{ recommendation()!.energy }}</strong></span>
          </div>
          <p class="disclaimer">Esta sugerencia es aproximada. Cada mascota tiene su propia personalidad.</p>
          <div class="action-footer">
            <button type="button" class="btn btn-primary" (click)="restart()">Repetir test</button>
            <a routerLink="/catalogo" class="btn btn-outline">Ver catálogo</a>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .section-padding { padding-top: var(--space-2xl); padding-bottom: var(--space-2xl); }
    .module-header { margin-bottom: var(--space-xl); }
    .subtitle { color: var(--text-secondary); font-size: 1.1rem; margin-top: var(--space-xs); }
    .badge-accent { background: rgba(139, 92, 246, 0.15); color: var(--color-accent); border: 1px solid rgba(139, 92, 246, 0.3); }
    .lead-badge { margin-top: var(--space-md); font-size: 0.95rem; color: var(--color-accent); }
    .placeholder-card { text-align: center; padding: var(--space-2xl); max-width: 680px; margin: 0 auto var(--space-xl); }
    .card-icon { font-size: 3rem; margin-bottom: var(--space-md); }
    .placeholder-card h3 { margin-bottom: var(--space-sm); }
    .placeholder-card p { color: var(--text-secondary); font-size: 0.95rem; }
    .wizard-card, .result-card { max-width: 760px; margin: 0 auto; padding: var(--space-xl); }
    .progress-row { display: flex; justify-content: space-between; color: var(--text-secondary); font-size: 0.85rem; }
    .progress-bar { height: 6px; background: var(--bg-input); border-radius: var(--radius-full); margin: var(--space-sm) 0 var(--space-xl); overflow: hidden; }
    .progress-bar span { display: block; height: 100%; background: var(--color-accent); transition: width 0.2s ease; }
    fieldset { border: 0; }
    legend { font: 700 clamp(1.25rem, 3vw, 1.65rem) var(--font-heading); margin-bottom: var(--space-xs); }
    .question-help { color: var(--text-secondary); margin-bottom: var(--space-lg); }
    .option-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(190px, 1fr)); gap: var(--space-md); }
    .option { display: flex; flex-direction: column; align-items: center; gap: var(--space-xs); min-height: 150px; padding: var(--space-lg) var(--space-md); color: var(--text-main); background: var(--bg-input); border: 2px solid var(--border-color); border-radius: var(--radius-md); cursor: pointer; text-align: center; transition: border-color 0.2s ease, background 0.2s ease; }
    .option:hover, .option.selected { background: rgba(139, 92, 246, 0.12); border-color: var(--color-accent); }
    .option-icon { font-size: 2rem; }
    .option small { color: var(--text-secondary); }
    .wizard-actions { display: flex; justify-content: flex-end; gap: var(--space-sm); margin-top: var(--space-xl); }
    .action-footer { margin-top: var(--space-xl); }
    .result-card { text-align: center; }
    .result-icon { font-size: 4rem; margin-bottom: var(--space-sm); }
    .result-card h2 { margin: var(--space-md) 0 var(--space-sm); }
    .result-card > p { color: var(--text-secondary); }
    .result-details { display: flex; justify-content: center; flex-wrap: wrap; gap: var(--space-md); margin: var(--space-lg) 0; color: var(--text-secondary); }
    .result-details strong { color: var(--text-main); }
    .disclaimer { font-size: 0.85rem; }
    code { background: rgba(255,255,255,0.1); padding: 0.2rem 0.4rem; border-radius: var(--radius-sm); color: var(--color-accent); }
    @media (max-width: 520px) { .wizard-card, .result-card { padding: var(--space-lg); } .wizard-actions { justify-content: stretch; } .wizard-actions .btn { flex: 1; } }
  `]
})
export class MatchmakerContainerComponent {
  readonly totalSteps = 4;
  readonly step = signal(1);
  readonly answers = signal<Partial<Answers>>({});
  readonly recommendation = signal<{
    icon: string;
    label: string;
    size: string;
    energy: string;
    description: string;
  } | null>(null);
  readonly progress = computed(() => this.step() / this.totalSteps * 100);

  canContinue(): boolean {
    const answer = this.answers();
    return this.step() === 1 ? !!answer.housing
      : this.step() === 2 ? !!answer.time
      : this.step() === 3 ? !!answer.energy
      : !!answer.species;
  }

  select<Key extends keyof Answers>(key: Key, value: Answers[Key]): void {
    this.answers.update(current => ({ ...current, [key]: value }));
  }

  nextStep(): void {
    if (this.canContinue() && this.step() < this.totalSteps) {
      this.step.update(current => current + 1);
    }
  }

  previousStep(): void {
    if (this.step() > 1) {
      this.step.update(current => current - 1);
    }
  }

  showRecommendation(): void {
    if (!this.canContinue()) return;

    const answer = this.answers() as Answers;
    const size = answer.species === 'GATO'
      ? 'mediano'
      : answer.housing === 'DEPARTAMENTO' && answer.energy !== 'ACTIVO'
        ? 'pequeño'
        : answer.energy === 'ACTIVO' || answer.housing === 'CASA_PATIO'
          ? 'mediano o grande'
          : 'mediano';
    const label = answer.species === 'GATO'
      ? 'gato de energía ' + this.energyLabel(answer.energy)
      : 'perro ' + size + ' de energía ' + this.energyLabel(answer.energy);

    this.recommendation.set({
      icon: answer.species === 'GATO' ? '🐱' : '🐶',
      label,
      size,
      energy: this.energyLabel(answer.energy),
      description: answer.species === 'GATO'
        ? 'Un gato puede adaptarse bien a tu rutina y ofrecer compañía respetando tus espacios y tiempos.'
        : `Un perro ${size} podría adaptarse mejor al espacio de tu hogar y al tiempo que tienes disponible.`,
    });
  }

  restart(): void {
    this.answers.set({});
    this.recommendation.set(null);
    this.step.set(1);
  }

  private energyLabel(energy: Energy): string {
    return energy === 'TRANQUILO' ? 'tranquila' : energy === 'MODERADO' ? 'moderada' : 'activa';
  }
}

