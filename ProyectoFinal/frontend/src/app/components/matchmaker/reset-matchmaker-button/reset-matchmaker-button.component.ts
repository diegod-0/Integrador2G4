import { Component, output } from '@angular/core';

@Component({
  selector: 'app-reset-matchmaker-button',
  standalone: true,
  templateUrl: './reset-matchmaker-button.component.html',
  styleUrl: './reset-matchmaker-button.component.css',
})
export class ResetMatchmakerButtonComponent {
  readonly reset = output<void>();
}
