import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AnimalCardComponent } from './animal-card.component';
import { AnimalAdopcion } from '../../../models/animal.model';

describe('AnimalCardComponent', () => {
  let fixture: ComponentFixture<AnimalCardComponent>;

  const mockAnimal: AnimalAdopcion = {
    id: 'a1',
    nombre: 'Firulais',
    especie: 'PERRO',
    raza: 'Mestizo',
    edadAnios: 2,
    tamano: 'MEDIANO',
    sexo: 'MACHO',
    fotoUrl: 'https://placedog.net/500?id=1',
    descripcion: 'Juguetón y sociable.',
    albergueNombre: 'Albergue San Francisco',
    coordenadas: { latitude: -12.1211, longitude: -77.0295 },
    distrito: 'Miraflores',
    distanciaKm: 1.8,
    energia: 'ACTIVO',
    espacioRequerido: 'CASA_PATIO',
    tiempoRequerido: 'ALTO',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [AnimalCardComponent] });
    fixture = TestBed.createComponent(AnimalCardComponent);
    fixture.componentRef.setInput('animal', mockAnimal);
    fixture.detectChanges();
  });

  it('debe crearse correctamente', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('debe renderizar el nombre y la distancia del animal', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Firulais');
    expect(compiled.textContent).toContain('1.8 km');
  });

  it('la imagen debe tener atributos de optimización CLS (width, height, lazy)', () => {
    const img = fixture.nativeElement.querySelector('img') as HTMLImageElement;
    expect(img.getAttribute('width')).toBe('320');
    expect(img.getAttribute('height')).toBe('240');
    expect(img.getAttribute('loading')).toBe('lazy');
  });

  it('no debe mostrar el badge de distancia si distanciaKm es undefined', () => {
    const animalSinDistancia: AnimalAdopcion = { ...mockAnimal, distanciaKm: undefined };
    fixture.componentRef.setInput('animal', animalSinDistancia);
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.animal-card__distance');
    expect(badge).toBeNull();
  });
});
