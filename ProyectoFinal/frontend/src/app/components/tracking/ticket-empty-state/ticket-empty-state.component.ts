import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-ticket-empty-state',
  standalone: true,
  templateUrl: './ticket-empty-state.component.html',
  styleUrl: './ticket-empty-state.component.css',
})
export class TicketEmptyStateComponent {
  readonly code = input<string | null>(null);
  readonly retryRequested = output<void>();
}
