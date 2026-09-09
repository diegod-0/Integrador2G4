import { Component, input } from '@angular/core';
import { AnimalAdopcion } from '../../../models/animal.model';

@Component({
  selector: 'app-animal-card',
  standalone: true,
  imports: [],
  templateUrl: './animal-card.component.html',
  styleUrl: './animal-card.component.css',
})
export class AnimalCardComponent {
  readonly animal = input.required<AnimalAdopcion>();
}
