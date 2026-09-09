export interface MatchmakerAnswers {
  housingType: 'DEPARTAMENTO_SIN_PATIO' | 'CASA_CON_PATIO_PEQUEÑO' | 'CASA_CAMPO_GRANDE';
  freeTimeDaily: 'MENOS_DE_2H' | 'ENTRE_2H_Y_4H' | 'MAS_DE_4H';
  preferredEnergy: 'TRANQUILO' | 'MODERADO' | 'MUY_ACTIVO';
}

export interface AffinityScore {
  animalId: string;
  scorePercentage: number;
  isTopMatch: boolean;
}
