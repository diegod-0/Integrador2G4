import { calculateHaversineDistance } from './haversine';

describe('Haversine Formula', () => {
  it('debe retornar 0 km para coordenadas idénticas', () => {
    const point = { latitude: -12.046374, longitude: -77.042793 };
    expect(calculateHaversineDistance(point, point)).toBe(0);
  });

  it('debe calcular correctamente la distancia aproximada entre Plaza de Armas y Parque Kennedy', () => {
    const plazaArmas = { latitude: -12.046374, longitude: -77.042793 };
    const parqueKennedy = { latitude: -12.121925, longitude: -77.029853 };

    const distance = calculateHaversineDistance(plazaArmas, parqueKennedy);
    expect(distance).toBeGreaterThan(8.0);
    expect(distance).toBeLessThan(9.0);
  });
});
