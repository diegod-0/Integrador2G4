import { ComponentFixture, TestBed } from '@angular/core/testing';
import { pendingTicketMock } from '../../../mocks/tracking.mocks';
import { RescueTimelineComponent } from './rescue-timeline.component';

describe('RescueTimelineComponent', () => {
  let fixture: ComponentFixture<RescueTimelineComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RescueTimelineComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RescueTimelineComponent);
    fixture.componentRef.setInput('steps', pendingTicketMock.historial);
    fixture.detectChanges();
  });

  it('renderiza todos los pasos del timeline', () => {
    expect(fixture.nativeElement.querySelectorAll('li').length).toBe(
      pendingTicketMock.historial.length,
    );
  });

  it('marca aria-current únicamente en el paso actual', () => {
    const currentSteps = fixture.nativeElement.querySelectorAll('[aria-current="step"]');

    expect(currentSteps.length).toBe(1);
    expect(currentSteps[0].textContent).toContain('Emergencia Reportada');
  });
});
