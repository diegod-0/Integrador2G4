import { Component, input } from '@angular/core';

@Component({
  selector: 'app-compatibility-badge',
  standalone: true,
  templateUrl: './compatibility-badge.component.html',
  styleUrl: './compatibility-badge.component.css',
})
export class CompatibilityBadgeComponent {
  readonly score = input.required<number>();
}
