import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReportFormComponent } from './report-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { ImageOptimizerService } from '../../../services/image-optimizer.service';
import { vi } from 'vitest';

describe('ReportFormComponent', () => {
  let component: ReportFormComponent;
  let fixture: ComponentFixture<ReportFormComponent>;
  let mockImageOptimizer: { compressImage: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockImageOptimizer = {
      compressImage: vi.fn().mockResolvedValue({
        dataUrl: 'data:image/jpeg;base64,compressedOptimizedData',
        originalSizeBytes: 2048000,
        compressedSizeBytes: 153600,
        width: 1000,
        height: 750
      })
    };

    await TestBed.configureTestingModule({
      imports: [ReportFormComponent, ReactiveFormsModule],
      providers: [
        { provide: ImageOptimizerService, useValue: mockImageOptimizer }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReportFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('debe iniciar con el formulario inválido por campos obligatorios vacíos', () => {
    expect(component.form.valid).toBe(false);
  });

  it('debe marcar como inválido un teléfono que no tenga 9 dígitos o no empiece con 9', () => {
    const phoneControl = component.form.controls['reporterPhone'];

    // Menos de 9 dígitos
    phoneControl.setValue('98765');
    expect(phoneControl.valid).toBe(false);

    // 9 dígitos pero no empieza con 9
    phoneControl.setValue('887654321');
    expect(phoneControl.valid).toBe(false);

    // Letras o caracteres especiales
    phoneControl.setValue('98765abcd');
    expect(phoneControl.valid).toBe(false);

    // Válido: 9 dígitos iniciando con 9
    phoneControl.setValue('987654321');
    expect(phoneControl.valid).toBe(true);
  });

  it('debe gestionar eventos de drag & drop actualizando la señal isDragging', () => {
    const fakeDragEvent = {
      preventDefault: vi.fn(),
      stopPropagation: vi.fn()
    } as unknown as DragEvent;

    component.onDragOver(fakeDragEvent);
    expect(component.isDragging()).toBe(true);

    component.onDragLeave(fakeDragEvent);
    expect(component.isDragging()).toBe(false);
  });

  it('debe optimizar y comprimir la imagen al invocar processFile con un archivo válido', async () => {
    const fakeFile = new File(['fake-image-bytes'], 'mascota.png', { type: 'image/png' });

    component.processFile(fakeFile);

    expect(component.isCompressing()).toBe(true);
    await vi.waitFor(() => expect(component.isCompressing()).toBe(false));

    expect(mockImageOptimizer.compressImage).toHaveBeenCalledWith(fakeFile);
    expect(component.imagePreview()).toBe('data:image/jpeg;base64,compressedOptimizedData');
    expect(component.compressionStats()).toEqual({
      originalKb: 2000,
      compressedKb: 150,
      ratioPercent: 93
    });
  });

  it('debe bloquear el envío si no se adjuntó una fotografía y setear mensaje de error', () => {
    component.form.patchValue({
      reporterName: 'Carlos Ruiz',
      reporterPhone: '912345678',
      referenceAddress: 'Av. Arequipa 1234, Lince',
      urgencyLevel: 'MEDIA'
    });

    expect(component.form.valid).toBe(true);

    // Intentar enviar sin foto
    component.submit();

    expect(component.imageError()).toBe('Debe adjuntar al menos una fotografía del animal para el reporte.');
  });

  it('debe emitir onSubmitReport con el payload completo cuando los campos y la foto son válidos', () => {
    let emittedPayload: any = null;
    component.onSubmitReport.subscribe(payload => {
      emittedPayload = payload;
    });

    component.form.patchValue({
      reporterName: 'Lucía Mendoza',
      reporterPhone: '998877665',
      referenceAddress: 'Parque de la Reserva, Puerta 3',
      conditionDescription: 'Gatito atrapado en un árbol',
      urgencyLevel: 'CRITICA'
    });

    // Simular imagen cargada
    component.imagePreview.set('data:image/jpeg;base64,mockImageData123');

    component.submit();

    expect(emittedPayload).not.toBeNull();
    expect(emittedPayload.reporterName).toBe('Lucía Mendoza');
    expect(emittedPayload.reporterPhone).toBe('998877665');
    expect(emittedPayload.referenceAddress).toBe('Parque de la Reserva, Puerta 3');
    expect(emittedPayload.urgencyLevel).toBe('CRITICA');
    expect(emittedPayload.imageUrl).toBe('data:image/jpeg;base64,mockImageData123');
  });
});

