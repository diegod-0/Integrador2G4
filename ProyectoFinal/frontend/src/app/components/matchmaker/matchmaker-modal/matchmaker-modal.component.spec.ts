import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatchmakerModalComponent } from './matchmaker-modal.component';

describe('MatchmakerModalComponent', () => {
  let component: MatchmakerModalComponent;
  let fixture: ComponentFixture<MatchmakerModalComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [MatchmakerModalComponent] });
    fixture = TestBed.createComponent(MatchmakerModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('emite onClose al pulsar Escape', () => {
    let closed = false;
    component.onClose.subscribe(() => (closed = true));

    fixture.nativeElement.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));

    expect(closed).toBe(true);
  });

  it('avanza del paso 1 al paso 2 al seleccionar la vivienda', () => {
    const firstOption = fixture.nativeElement.querySelector('fieldset button') as HTMLButtonElement;
    firstOption.click();

    expect(component.currentStep()).toBe(2);
  });

  it('emite respuestas válidas al finalizar el paso 3', () => {
    let result: unknown;
    component.onComplete.subscribe((answers) => (result = answers));

    (fixture.nativeElement.querySelectorAll('fieldset button')[0] as HTMLButtonElement).click();
    fixture.detectChanges();
    (fixture.nativeElement.querySelectorAll('fieldset button')[0] as HTMLButtonElement).click();
    fixture.detectChanges();
    (fixture.nativeElement.querySelectorAll('fieldset button')[0] as HTMLButtonElement).click();
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('.btn-primary') as HTMLButtonElement).click();

    expect(result).toEqual({
      housingType: 'DEPARTAMENTO_SIN_PATIO',
      freeTimeDaily: 'MENOS_DE_2H',
      preferredEnergy: 'TRANQUILO',
    });
  });
});
