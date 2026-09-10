import { Component, input } from '@angular/core';
import { AnimalAdopcion } from '../../../models/animal.model';
import { CompatibilityBadgeComponent } from '../../matchmaker/compatibility-badge/compatibility-badge.component';

@Component({
  selector: 'app-animal-card',
  standalone: true,
  imports: [CompatibilityBadgeComponent],
  templateUrl: './animal-card.component.html',
  styleUrl: './animal-card.component.css',
})
export class AnimalCardComponent {
  readonly animal = input.required<AnimalAdopcion>();
}
