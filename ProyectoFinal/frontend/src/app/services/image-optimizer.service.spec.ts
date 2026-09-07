import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { ImageOptimizerService } from './image-optimizer.service';

describe('ImageOptimizerService', () => {
  let service: ImageOptimizerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ImageOptimizerService);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it('debe crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debe procesar y comprimir una imagen simulando HTML5 Image y Canvas', async () => {
    const fakeFile = new File(['fake-bytes'], 'mascota.jpg', { type: 'image/jpeg' });

    // Mock Image object
    class MockImage {
      width = 2000;
      height = 1000;
      onload: (() => void) | null = null;
      onerror: (() => void) | null = null;
      set src(_val: string) {
        setTimeout(() => {
          if (this.onload) this.onload();
        }, 10);
      }
    }
    vi.stubGlobal('Image', MockImage);

    // Mock Canvas & Context 2D
    const mockCtx = {
      imageSmoothingEnabled: false,
      imageSmoothingQuality: '',
      drawImage: vi.fn(),
    };
    const mockCanvas = {
      width: 0,
      height: 0,
      getContext: vi.fn().mockReturnValue(mockCtx),
      toDataURL: vi.fn().mockReturnValue('data:image/jpeg;base64,mockCompressedDataUrl')
    };

    const origCreateElement = document.createElement.bind(document);
    vi.spyOn(document, 'createElement').mockImplementation((tagName: string) => {
      if (tagName === 'canvas') return mockCanvas as unknown as HTMLCanvasElement;
      return origCreateElement(tagName);
    });

    const result = await service.compressImage(fakeFile);

    expect(result).toBeTruthy();
    expect(result.dataUrl).toBe('data:image/jpeg;base64,mockCompressedDataUrl');
    expect(result.width).toBe(1000);
    expect(result.height).toBe(500);
    expect(mockCtx.drawImage).toHaveBeenCalled();
  });

  it('debe rechazar si la imagen dispara onerror', async () => {
    const fakeFile = new File(['corrupted'], 'corrupted.jpg', { type: 'image/jpeg' });

    class ErrorMockImage {
      onload: (() => void) | null = null;
      onerror: (() => void) | null = null;
      set src(_val: string) {
        setTimeout(() => {
          if (this.onerror) this.onerror();
        }, 10);
      }
    }
    vi.stubGlobal('Image', ErrorMockImage);

    await expect(service.compressImage(fakeFile)).rejects.toThrow('No se pudo procesar el formato de la imagen.');
  });
});

