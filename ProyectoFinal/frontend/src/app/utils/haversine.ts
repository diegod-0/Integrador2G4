import { GeoCoordinates } from '../models/emergency-report.model';

export function calculateHaversineDistance(pointA: GeoCoordinates, pointB: GeoCoordinates): number {
  if (pointA.latitude === pointB.latitude && pointA.longitude === pointB.longitude) {
    return 0;
  }

  const R = 6371; // Radio de la Tierra en km
  const dLat = degreesToRadians(pointB.latitude - pointA.latitude);
  const dLon = degreesToRadians(pointB.longitude - pointA.longitude);

  const lat1 = degreesToRadians(pointA.latitude);
  const lat2 = degreesToRadians(pointB.latitude);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  const distance = R * c;
  return Number(distance.toFixed(1));
}

function degreesToRadians(degrees: number): number {
  return degrees * (Math.PI / 180);
}
