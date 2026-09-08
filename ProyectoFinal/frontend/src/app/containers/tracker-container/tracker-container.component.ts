import { Component, computed, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TrackingSearchComponent } from '../../components/tracking/tracking-search/tracking-search.component';
import { RescueTimelineComponent } from '../../components/tracking/rescue-timeline/rescue-timeline.component';
import { TicketEmptyStateComponent } from '../../components/tracking/ticket-empty-state/ticket-empty-state.component';
import { TrackingService } from '../../services/tracking.service';

@Component({
  selector: 'app-tracker-container',
  standalone: true,
  imports: [CommonModule, TrackingSearchComponent, RescueTimelineComponent, TicketEmptyStateComponent],
  templateUrl: './tracker-container.component.html',
  styleUrl: './tracker-container.component.css',
})
export class TrackerContainerComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly trackingService = inject(TrackingService);

  readonly viewState = this.trackingService.state;
  readonly ticketCode = this.route.snapshot.paramMap.get('codigo');
  readonly ticket = computed(() => {
    const state = this.viewState();
    return state.status === 'success' ? state.data : null;
  });
  readonly errorMessage = computed(() => {
    const state = this.viewState();
    return state.status === 'error' ? state.message : '';
  });

  constructor() {
    this.trackingService.load(this.ticketCode ?? undefined);
  }

  search(code: string): void {
    this.trackingService.load(code);
  }

  retry(): void {
    this.trackingService.load(this.ticketCode ?? undefined);
  }
}
