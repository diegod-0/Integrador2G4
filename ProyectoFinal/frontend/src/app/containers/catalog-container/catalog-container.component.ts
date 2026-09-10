import { Component, effect, inject, signal } from '@angular/core';
import { AnimalCatalogService } from '../../services/animal-catalog.service';
import { GeolocationService } from '../../services/geolocation.service';
import { FiltroCatalogo } from '../../models/animal.model';
import { CatalogFiltersComponent } from '../../components/catalog/catalog-filters/catalog-filters.component';
import { AnimalCardComponent } from '../../components/catalog/animal-card/animal-card.component';
import { MatchmakerModalComponent } from '../../components/matchmaker/matchmaker-modal/matchmaker-modal.component';
import { ResetMatchmakerButtonComponent } from '../../components/matchmaker/reset-matchmaker-button/reset-matchmaker-button.component';
import { MatchmakerAnswers } from '../../models/matchmaker.model';
import { MatchmakerService } from '../../services/matchmaker.service';
import { ANIMALS_MOCK } from '../../mocks/animals.mock';

@Component({
  selector: 'app-catalog-container',
  standalone: true,
  imports: [CatalogFiltersComponent, AnimalCardComponent, MatchmakerModalComponent, ResetMatchmakerButtonComponent],
  templateUrl: './catalog-container.component.html',
  styleUrl: './catalog-container.component.css',
})
export class CatalogContainerComponent {
  protected readonly catalogService = inject(AnimalCatalogService);
  protected readonly geolocationService = inject(GeolocationService);
  protected readonly matchmakerService = inject(MatchmakerService);
  protected readonly isWizardOpen = signal(false);

  constructor() {
    // Sincroniza el estado de GeolocationService (compartido con HU01) hacia el catálogo
    effect(() => {
      const state = this.geolocationService.state();

      if (state.status === 'acquired') {
        this.catalogService.setUserLocation(state.coords);
      }

      if (state.status === 'denied' || state.status === 'error') {
        // Escenario 3 de HU03 (BDD): sin ubicación, vuelve al orden por defecto
        this.catalogService.setUserLocation(null);
        this.catalogService.toggleCercania(false);
      }
    });
  }

  onCercaniaToggle(activo: boolean): void {
    this.catalogService.toggleCercania(activo);

    if (activo) {
      this.geolocationService.requestCurrentPosition();
    } else {
      this.geolocationService.reset();
      this.catalogService.setUserLocation(null);
    }
  }

  onEspecieChange(especie: FiltroCatalogo['especie']): void {
    this.catalogService.updateEspecie(especie);
  }

  onTamanoChange(tamano: FiltroCatalogo['tamano']): void {
    this.catalogService.updateTamano(tamano);
  }

  openWizard(): void {
    this.isWizardOpen.set(true);
  }

  closeWizard(): void {
    this.isWizardOpen.set(false);
  }

  handleAffinityResults(answers: MatchmakerAnswers): void {
    const scores = this.matchmakerService.calculateAffinity(ANIMALS_MOCK, answers);
    this.matchmakerService.applyScores(scores);
    this.closeWizard();
  }

  clearAffinity(): void {
    this.catalogService.clearMatchmaker();
  }
}
