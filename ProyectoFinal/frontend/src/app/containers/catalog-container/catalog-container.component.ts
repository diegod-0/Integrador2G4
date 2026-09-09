import { Component, effect, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AnimalCatalogService } from '../../services/animal-catalog.service';
import { GeolocationService } from '../../services/geolocation.service';
import { FiltroCatalogo } from '../../models/animal.model';
import { CatalogFiltersComponent } from '../../components/catalog/catalog-filters/catalog-filters.component';
import { AnimalCardComponent } from '../../components/catalog/animal-card/animal-card.component';

@Component({
  selector: 'app-catalog-container',
  standalone: true,
  imports: [RouterLink, CatalogFiltersComponent, AnimalCardComponent],
  templateUrl: './catalog-container.component.html',
  styleUrl: './catalog-container.component.css',
})
export class CatalogContainerComponent {
  protected readonly catalogService = inject(AnimalCatalogService);
  protected readonly geolocationService = inject(GeolocationService);

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
}
