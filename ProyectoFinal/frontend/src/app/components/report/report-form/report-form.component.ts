import { Component, computed, inject, input, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { EmergencyReportPayload, GeoCoordinates, GeolocationState, UrgencyLevel } from '../../../models/emergency-report.model';
import { ImageOptimizerService } from '../../../services/image-optimizer.service';

@Component({
  selector: 'app-report-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './report-form.component.html',
  styleUrl: './report-form.component.css'
})
export class ReportFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly imageOptimizer = inject(ImageOptimizerService);

  // Inputs con la nueva API nativa de Angular 19
  readonly isSubmitting = input<boolean>(false);
  readonly currentCoordinates = input<GeoCoordinates | null>(null);
  readonly gpsState = input<GeolocationState>({ status: 'idle' });
  readonly availableDistricts = input<string[]>([
    'Miraflores', 'San Isidro', 'Santiago de Surco', 'San Borja',
    'Los Olivos', 'Lima Cercado', 'San Miguel', 'Jesús María', 'La Molina', 'Barranco'
  ]);

  // Outputs reactivos
  readonly onSubmitReport = output<EmergencyReportPayload>();
  readonly onRequestGps = output<void>();
  readonly onSelectDistrict = output<string>();

  // Señales locales para manejo de imagen, compresión y drag-and-drop
  readonly imagePreview = signal<string | null>(null);
  readonly imageError = signal<string | null>(null);
  readonly isCompressing = signal<boolean>(false);
  readonly isDragging = signal<boolean>(false);
  readonly compressionStats = signal<{ originalKb: number; compressedKb: number; ratioPercent: number } | null>(null);
  readonly submittedAttempt = signal<boolean>(false);

  readonly hasGps = computed(() => this.currentCoordinates() !== null);

  // Generación reactiva y sanitizada de la URL de OpenStreetMap
  readonly mapUrl = computed<SafeResourceUrl | null>(() => {
    const coords = this.currentCoordinates();
    if (!coords) return null;
    const lat = coords.latitude;
    const lng = coords.longitude;
    const delta = 0.004;
    const minLng = (lng - delta).toFixed(5);
    const minLat = (lat - delta).toFixed(5);
    const maxLng = (lng + delta).toFixed(5);
    const maxLat = (lat + delta).toFixed(5);
    const url = `https://www.openstreetmap.org/export/embed.html?bbox=${minLng}%2C${minLat}%2C${maxLng}%2C${maxLat}&layer=mapnik&marker=${lat}%2C${lng}`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  });

  readonly form: FormGroup = this.fb.group({
    reporterName: ['', [Validators.required, Validators.minLength(3)]],
    reporterPhone: ['', [Validators.required, Validators.pattern(/^9\d{8}$/)]],
    referenceAddress: ['', [Validators.required, Validators.minLength(5)]],
    conditionDescription: [''],
    urgencyLevel: ['MEDIA' as UrgencyLevel, [Validators.required]]
  });

  get f() {
    return this.form.controls;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    this.processFile(input.files[0]);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(true);
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(false);
  }

  onFileDropped(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(false);

    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.processFile(event.dataTransfer.files[0]);
    }
  }

  processFile(file: File): void {
    this.imageError.set(null);

    // Validación de formato
    if (!file.type.startsWith('image/')) {
      this.imageError.set('El archivo seleccionado debe ser una imagen (JPG, PNG o WEBP).');
      return;
    }

    // Validación de peso máximo: 5 MB (5 * 1024 * 1024 bytes)
    const MAX_SIZE_BYTES = 5 * 1024 * 1024;
    if (file.size > MAX_SIZE_BYTES) {
      this.imageError.set('La fotografía excede el tamaño máximo permitido de 5 MB.');
      return;
    }

    // Compresión y reescalado en cliente con Canvas (previene QuotaExceededError en localStorage)
    this.isCompressing.set(true);
    this.imageOptimizer.compressImage(file)
      .then((result) => {
        this.imagePreview.set(result.dataUrl);
        const origKb = Math.round(result.originalSizeBytes / 1024);
        const compKb = Math.round(result.compressedSizeBytes / 1024);
        const ratio = Math.round((1 - result.compressedSizeBytes / result.originalSizeBytes) * 100);
        this.compressionStats.set({
          originalKb: origKb,
          compressedKb: compKb,
          ratioPercent: ratio > 0 ? ratio : 0
        });
        this.isCompressing.set(false);
      })
      .catch(() => {
        this.imageError.set('No se pudo procesar la imagen seleccionada.');
        this.isCompressing.set(false);
      });
  }

  removeImage(): void {
    this.imagePreview.set(null);
    this.imageError.set(null);
    this.compressionStats.set(null);
  }

  onDistrictChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    if (select.value) {
      this.onSelectDistrict.emit(select.value);
    }
  }

  submit(): void {
    this.submittedAttempt.set(true);

    // Validación de imagen obligatoria
    if (!this.imagePreview()) {
      this.imageError.set('Debe adjuntar al menos una fotografía del animal para el reporte.');
    }

    if (this.form.invalid || !this.imagePreview()) {
      this.form.markAllAsTouched();
      return;
    }

    // Coordenadas fijadas o coordenadas por defecto del centro de Lima si no se concedió GPS
    const coords: GeoCoordinates = this.currentCoordinates() ?? {
      latitude: -12.0464,
      longitude: -77.0428,
      accuracyMeters: 1000
    };

    const payload: EmergencyReportPayload = {
      reporterName: this.form.value.reporterName.trim(),
      reporterPhone: this.form.value.reporterPhone.trim(),
      referenceAddress: this.form.value.referenceAddress.trim(),
      conditionDescription: this.form.value.conditionDescription?.trim() || undefined,
      urgencyLevel: this.form.value.urgencyLevel as UrgencyLevel,
      imageUrl: this.imagePreview()!,
      coordinates: coords
    };

    this.onSubmitReport.emit(payload);
  }
}

