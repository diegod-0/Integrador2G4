import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CatalogFiltersComponent } from './catalog-filters.component';
import { FiltroCatalogo } from '../../../models/animal.model';

describe('CatalogFiltersComponent', () => {
  let fixture: ComponentFixture<CatalogFiltersComponent>;

  const filtroInicial: FiltroCatalogo = {
    especie: 'TODOS',
    tamano: 'TODOS',
    ordenarPorCercania: false,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [CatalogFiltersComponent] });
    fixture = TestBed.createComponent(CatalogFiltersComponent);
    fixture.componentRef.setInput('filtro', filtroInicial);
    fixture.detectChanges();
  });

  it('debe crearse correctamente', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('debe emitir especieChange al cambiar el select de especie', () => {
    let emitted: string | undefined;
    fixture.componentInstance.especieChange.subscribe((v: string) => (emitted = v));

    const select = fixture.nativeElement.querySelector('#filtro-especie') as HTMLSelectElement;
    select.value = 'PERRO';
    select.dispatchEvent(new Event('change'));

    expect(emitted).toBe('PERRO');
  });

  it('debe emitir tamanoChange al cambiar el select de tamaño', () => {
    let emitted: string | undefined;
    fixture.componentInstance.tamanoChange.subscribe((v: string) => (emitted = v));

    const select = fixture.nativeElement.querySelector('#filtro-tamano') as HTMLSelectElement;
    select.value = 'GRANDE';
    select.dispatchEvent(new Event('change'));

    expect(emitted).toBe('GRANDE');
  });

  it('debe emitir cercaniaToggle al activar el switch "Cerca de mí"', () => {
    let emitted: boolean | undefined;
    fixture.componentInstance.cercaniaToggle.subscribe((v: boolean) => (emitted = v));

    const checkbox = fixture.nativeElement.querySelector(
      'input[type="checkbox"]',
    ) as HTMLInputElement;
    checkbox.checked = true;
    checkbox.dispatchEvent(new Event('change'));

    expect(emitted).toBe(true);
  });
});
