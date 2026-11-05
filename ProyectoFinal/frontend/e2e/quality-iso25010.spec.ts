import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

/**
 * Suite Ejecutable de Verificación y Validación E2E + Accesibilidad WCAG 2.2 AA
 * Unidad 3 (Semanas 10-13) — Calidad orientada a Funcionalidad, Compatibilidad e Interacción.
 */
test.describe('ISO/IEC 25010:2023 · Funcionalidad, Interacción y Accesibilidad WCAG 2.2', () => {

  test('1. [Adecuación Funcional & Asistencia] Catálogo carga mascotas y cuenta con asistente guiado Matchmaker', async ({ page }) => {
    await page.goto('/catalogo');

    await expect(page.locator('h1, h2').first()).toBeVisible();
    const cards = page.locator('article.animal-card');
    await expect(cards.first()).toBeVisible();

    // Asistencia al usuario: flujo guiado de compatibilidad Matchmaker
    await page.goto('/matchmaker');
    await expect(page.locator('main, .container, fieldset, legend').first()).toBeVisible();
  });

  test('2. [Protección contra Errores del Usuario] Bloquea envíos inválidos en Reporte de Emergencia', async ({ page }) => {
    await page.goto('/reportar');

    const submitButton = page.locator('button[type="submit"]');
    await expect(submitButton).toBeVisible();
    await submitButton.click();

    // Protección activa: muestra alerta de campos obligatorios y bloquea la navegación
    await expect(page.locator('.field-error, [role="alert"]').first()).toBeVisible();
    expect(page.url()).toContain('/reportar');
  });

  test('3. [Accesibilidad Técnica WCAG 2.2 AA] Auditoría automatizada con @axe-core/playwright sin violaciones críticas', async ({ page }) => {
    await page.goto('/catalogo');

    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
      .analyze();

    const criticalViolations = accessibilityScanResults.violations.filter(
      v => v.impact === 'critical'
    );
    expect(criticalViolations).toEqual([]);
  });

  test('4. [Accesibilidad Técnica: Teclado y Zoom 200%] Navegación por foco visible y reflujo sin pérdida de contenido', async ({ page }) => {
    // Simulación de Zoom al 200% (Viewport de 640x360 sobre base 1280x720 - WCAG 1.4.4 / 1.4.10 Reflow)
    await page.setViewportSize({ width: 640, height: 360 });
    await page.goto('/tracking');

    await page.keyboard.press('Tab');
    const focusedElement = page.locator(':focus');
    await expect(focusedElement).toBeVisible();

    // Verifica que no exista desbordamiento horizontal severo en zoom 200%
    const hasHorizontalOverflow = await page.evaluate(() => {
      return document.documentElement.scrollWidth > document.documentElement.clientWidth + 16;
    });
    expect(hasHorizontalOverflow).toBe(false);
  });
});

