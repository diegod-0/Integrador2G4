import { Injectable, signal } from '@angular/core';
import { AnimalAdopcion } from '../models/animal.model';
import { AffinityScore, MatchmakerAnswers } from '../models/matchmaker.model';

@Injectable({ providedIn: 'root' })
export class MatchmakerService {
  private readonly wizardActive = signal(false);
  private readonly scores = signal<Record<string, number>>({});

  readonly isWizardActive = this.wizardActive.asReadonly();
  readonly topScores = this.scores.asReadonly();

  calculateAffinity(animals: AnimalAdopcion[], answers: MatchmakerAnswers): AffinityScore[] {
    return animals
      .map((animal, index) => ({
        animal,
        index,
        scorePercentage: this.calculateScore(animal, answers),
      }))
      .sort((left, right) => right.scorePercentage - left.scorePercentage || left.index - right.index)
      .map(({ animal, scorePercentage }) => ({
        animalId: animal.id,
        scorePercentage,
        isTopMatch: scorePercentage >= 85,
      }));
  }

  applyScores(scores: AffinityScore[]): void {
    this.scores.set(Object.fromEntries(scores.map((score) => [score.animalId, score.scorePercentage])));
    this.wizardActive.set(true);
  }

  clearAffinity(): void {
    this.scores.set({});
    this.wizardActive.set(false);
  }

  private calculateScore(animal: AnimalAdopcion, answers: MatchmakerAnswers): number {
    let score = 0;

    if (answers.housingType === 'CASA_CAMPO_GRANDE') {
      score += 40;
    } else if (
      answers.housingType === 'DEPARTAMENTO_SIN_PATIO' &&
      (animal.especie === 'GATO' || animal.tamano === 'PEQUENO')
    ) {
      score += 40;
    } else if (answers.housingType === 'DEPARTAMENTO_SIN_PATIO' && animal.tamano === 'GRANDE') {
      score += 10;
    }

    if (answers.freeTimeDaily === 'MAS_DE_4H') {
      score += 30;
    } else if (
      answers.freeTimeDaily === 'MENOS_DE_2H' &&
      (animal.especie === 'GATO' || animal.edadAnios >= 5)
    ) {
      score += 30;
    }

    if (
      answers.preferredEnergy === 'TRANQUILO' &&
      (animal.especie === 'GATO' || animal.edadAnios >= 5)
    ) {
      score += 30;
    } else if (
      answers.preferredEnergy === 'MUY_ACTIVO' &&
      animal.especie === 'PERRO' &&
      animal.edadAnios <= 3
    ) {
      score += 30;
    }

    return Math.min(100, Math.max(0, score));
  }
}
