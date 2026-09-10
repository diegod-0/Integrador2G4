import { Component, HostListener, output, signal } from '@angular/core';
import { MatchmakerAnswers } from '../../../models/matchmaker.model';

@Component({
  selector: 'app-matchmaker-modal',
  standalone: true,
  templateUrl: './matchmaker-modal.component.html',
  styleUrl: './matchmaker-modal.component.css',
})
export class MatchmakerModalComponent {
  readonly onComplete = output<MatchmakerAnswers>();
  readonly onClose = output<void>();
  readonly currentStep = signal(1);

  private readonly answers = signal<Partial<MatchmakerAnswers>>({});

  @HostListener('keydown.escape')
  handleEscape(): void {
    this.onClose.emit();
  }

  @HostListener('keydown', ['$event'])
  handleKeyboard(event: KeyboardEvent): void {
    if (event.key !== 'Tab') {
      return;
    }

    const dialog = event.currentTarget as HTMLElement;
    const focusable = Array.from(
      dialog.querySelectorAll<HTMLElement>('button, input, select, textarea, [tabindex]:not([tabindex="-1"])'),
    ).filter((element) => !element.hasAttribute('disabled'));

    if (focusable.length === 0) {
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  }

  selectHousing(housingType: MatchmakerAnswers['housingType']): void {
    this.answers.update((answers) => ({ ...answers, housingType }));
    this.currentStep.set(2);
  }

  selectFreeTime(freeTimeDaily: MatchmakerAnswers['freeTimeDaily']): void {
    this.answers.update((answers) => ({ ...answers, freeTimeDaily }));
    this.currentStep.set(3);
  }

  selectEnergy(preferredEnergy: MatchmakerAnswers['preferredEnergy']): void {
    this.answers.update((answers) => ({ ...answers, preferredEnergy }));
  }

  goBack(): void {
    this.currentStep.update((step) => Math.max(1, step - 1));
  }

  complete(): void {
    const answers = this.answers();
    if (answers.housingType && answers.freeTimeDaily && answers.preferredEnergy) {
      this.onComplete.emit(answers as MatchmakerAnswers);
    }
  }
}
