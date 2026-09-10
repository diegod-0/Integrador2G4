import { Component, input } from '@angular/core';
import { TimelineStep } from '../../../models/emergency-report.model';

@Component({
  selector: 'app-rescue-timeline',
  standalone: true,
  templateUrl: './rescue-timeline.component.html',
  styleUrl: './rescue-timeline.component.css',
})
export class RescueTimelineComponent {
  readonly steps = input<TimelineStep[]>([]);
}
