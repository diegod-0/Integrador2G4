import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-tracking-search',
  standalone: true,
  templateUrl: './tracking-search.component.html',
  styleUrl: './tracking-search.component.css',
})
export class TrackingSearchComponent {
  readonly initialCode = input('');
  readonly codeSubmitted = output<string>();

  submit(event: SubmitEvent): void {
    event.preventDefault();
    const form = event.currentTarget as HTMLFormElement;
    const input = form.elements.namedItem('tracking-code') as HTMLInputElement;
    const code = input.value.trim();

    if (code) {
      this.codeSubmitted.emit(code);
    }
  }
}
