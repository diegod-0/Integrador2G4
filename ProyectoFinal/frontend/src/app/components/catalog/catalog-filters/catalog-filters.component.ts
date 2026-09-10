import { Component, input, output } from '@angular/core';
import { FiltroCatalogo } from '../../../models/animal.model';

@Component({
  selector: 'app-catalog-filters',
  standalone: true,
  imports: [],
  templateUrl: './catalog-filters.component.html',
  styleUrl: './catalog-filters.component.css',
})
export class CatalogFiltersComponent {
  readonly filtro = input.required<FiltroCatalogo>();

  readonly especieChange = output<FiltroCatalogo['especie']>();
  readonly tamanoChange = output<FiltroCatalogo['tamano']>();
  readonly cercaniaToggle = output<boolean>();

  onEspecieChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as FiltroCatalogo['especie'];
    this.especieChange.emit(value);
  }

  onTamanoChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as FiltroCatalogo['tamano'];
    this.tamanoChange.emit(value);
  }

  onCercaniaToggle(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.cercaniaToggle.emit(checked);
  }
}
