import { Injectable } from '@angular/core';

export interface OptimizationResult {
  dataUrl: string;
  originalSizeBytes: number;
  compressedSizeBytes: number;
  width: number;
  height: number;
}

@Injectable({
  providedIn: 'root'
})
export class ImageOptimizerService {
  private readonly MAX_WIDTH = 1000;
  private readonly MAX_HEIGHT = 1000;
  private readonly JPEG_QUALITY = 0.75;

  /**
   * Comprime y redimensiona una imagen en el cliente usando HTML5 Canvas.
   * Reduce archivos de 4 MB a menos de 150 KB para prevenir QuotaExceededError en localStorage.
   */
  compressImage(file: File): Promise<OptimizationResult> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onerror = () => reject(new Error('Error al leer el archivo de imagen.'));

      reader.onload = () => {
        const img = new Image();
        img.onerror = () => reject(new Error('No se pudo procesar el formato de la imagen.'));

        img.onload = () => {
          let { width, height } = img;

          // Calcular dimensiones manteniendo la relación de aspecto
          if (width > this.MAX_WIDTH || height > this.MAX_HEIGHT) {
            if (width > height) {
              height = Math.round((height * this.MAX_WIDTH) / width);
              width = this.MAX_WIDTH;
            } else {
              width = Math.round((width * this.MAX_HEIGHT) / height);
              height = this.MAX_HEIGHT;
            }
          }

          // Crear canvas en memoria para el reescalado
          const canvas = document.createElement('canvas');
          canvas.width = width;
          canvas.height = height;

          const ctx = canvas.getContext('2d');
          if (!ctx) {
            // Fallback si no hay contexto 2D (ej. entornos sin soporte)
            resolve({
              dataUrl: reader.result as string,
              originalSizeBytes: file.size,
              compressedSizeBytes: file.size,
              width: img.width,
              height: img.height
            });
            return;
          }

          // Dibujar con suavizado
          ctx.imageSmoothingEnabled = true;
          ctx.imageSmoothingQuality = 'high';
          ctx.drawImage(img, 0, 0, width, height);

          // Exportar a JPEG comprimido
          const compressedDataUrl = canvas.toDataURL('image/jpeg', this.JPEG_QUALITY);
          
          // Estimar peso en bytes del base64 (longitud * 3/4 aprox)
          const approximateSizeBytes = Math.round((compressedDataUrl.length - 22) * 0.75);

          resolve({
            dataUrl: compressedDataUrl,
            originalSizeBytes: file.size,
            compressedSizeBytes: approximateSizeBytes,
            width,
            height
          });
        };

        img.src = reader.result as string;
      };

      reader.readAsDataURL(file);
    });
  }
}
