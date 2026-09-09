import { TestBed } from '@angular/core/testing';
import { ANIMALS_MOCK } from '../mocks/animals.mock';
import { MatchmakerAnswers } from '../models/matchmaker.model';
import { MatchmakerService } from './matchmaker.service';

describe('MatchmakerService', () => {
  let service: MatchmakerService;
  const answers: MatchmakerAnswers = {
    housingType: 'DEPARTAMENTO_SIN_PATIO',
    freeTimeDaily: 'MENOS_DE_2H',
    preferredEnergy: 'TRANQUILO',
  };

  
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [MatchmakerService] });
    service = TestBed.inject(MatchmakerService);
  });

  it('otorga mayor puntaje a un gato frente a un perro grande en departamento y con poco tiempo', () => {
    const scores = service.calculateAffinity(ANIMALS_MOCK, answers);
    const cat = scores.find((score) => score.animalId === 'a2');
    const largeDog = scores.find((score) => score.animalId === 'a3');

    expect(cat?.scorePercentage).toBeGreaterThan(largeDog?.scorePercentage ?? 0);
  });

  it('mantiene todos los puntajes estrictamente dentro del rango [0, 100]', () => {
    const scores = service.calculateAffinity(ANIMALS_MOCK, {
      housingType: 'CASA_CAMPO_GRANDE',
      freeTimeDaily: 'MAS_DE_4H',
      preferredEnergy: 'MUY_ACTIVO',
    });

    expect(scores.every(({ scorePercentage }) => scorePercentage >= 0 && scorePercentage <= 100)).toBe(true);
  });

  it('ordena los resultados de mayor a menor compatibilidad', () => {
    const scores = service.calculateAffinity(ANIMALS_MOCK, answers);
    const values = scores.map(({ scorePercentage }) => scorePercentage);

    expect(values).toEqual([...values].sort((left, right) => right - left));
  });

  it('actualiza y limpia las señales de afinidad', () => {
    const scores = service.calculateAffinity(ANIMALS_MOCK, answers);
    service.applyScores(scores);

    expect(service.isWizardActive()).toBe(true);
    expect(service.topScores()['a2']).toBeDefined();

    service.clearAffinity();
    expect(service.isWizardActive()).toBe(false);
    expect(service.topScores()).toEqual({});
  });
});
