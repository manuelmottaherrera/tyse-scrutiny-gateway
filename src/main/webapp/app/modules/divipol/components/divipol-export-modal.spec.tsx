import React from 'react';
import { render, fireEvent } from '@testing-library/react';
import { TranslatorContext } from 'react-jhipster';
import { DivipolExportModal } from './divipol-export-modal';

// Mock de Reactstrap Modal para evitar problemas con portales
jest.mock('reactstrap', () => {
  const original = jest.requireActual('reactstrap');
  return {
    ...original,
    Modal: ({ isOpen, children }: { isOpen: boolean; children: React.ReactNode }) =>
      isOpen ? <div className="modal">{children}</div> : null,
  };
});

describe('DivipolExportModal', () => {
  const defaultProps = {
    isOpen: true,
    onClose: jest.fn(),
    onExport: jest.fn(),
    showScopeOption: false,
    totalPages: 1,
    currentPage: 0,
    totalElements: 100,
    exporting: false,
  };

  beforeAll(() => {
    TranslatorContext.registerTranslations('es', {});
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Seleccion de formato', () => {
    it('debe renderizar opciones CSV y PDF', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} />);

      const csvRadio = container.querySelector('input[value="csv"]');
      const pdfRadio = container.querySelector('input[value="pdf"]');

      expect(csvRadio).toBeTruthy();
      expect(pdfRadio).toBeTruthy();
    });

    it('debe tener CSV seleccionado por defecto', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} />);

      const csvRadio = container.querySelector<HTMLInputElement>('input[value="csv"]');
      expect(csvRadio?.checked).toBe(true);
    });

    it('debe permitir cambiar de CSV a PDF', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} />);

      const pdfRadio = container.querySelector<HTMLInputElement>('input[value="pdf"]');
      if (pdfRadio) {
        fireEvent.click(pdfRadio);
        expect(pdfRadio.checked).toBe(true);
      }
    });
  });

  describe('Seleccion de alcance', () => {
    it('debe mostrar opciones de alcance cuando showScopeOption=true', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} showScopeOption={true} totalPages={5} />);

      const currentPageRadio = container.querySelector('input[value="currentPage"]');
      const allRadio = container.querySelector('input[value="all"]');

      expect(currentPageRadio).toBeTruthy();
      expect(allRadio).toBeTruthy();
    });

    it('no debe mostrar opciones de alcance cuando showScopeOption=false', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} showScopeOption={false} />);

      const currentPageRadio = container.querySelector('input[value="currentPage"]');
      const allRadio = container.querySelector('input[value="all"]');

      expect(currentPageRadio).toBeNull();
      expect(allRadio).toBeNull();
    });

    it('debe tener pagina actual seleccionada por defecto', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} showScopeOption={true} />);

      const currentPageRadio = container.querySelector<HTMLInputElement>('input[value="currentPage"]');
      expect(currentPageRadio?.checked).toBe(true);
    });

    it('debe permitir seleccionar todos los resultados', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} showScopeOption={true} />);

      const allRadio = container.querySelector<HTMLInputElement>('input[value="all"]');
      if (allRadio) {
        fireEvent.click(allRadio);
        expect(allRadio.checked).toBe(true);
      }
    });
  });

  describe('Accion de exportar', () => {
    it('debe llamar onExport con formato seleccionado', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} />);

      const downloadButton = container.querySelector('button.btn-primary');
      if (downloadButton) {
        fireEvent.click(downloadButton);
      }

      expect(defaultProps.onExport).toHaveBeenCalledWith('csv', undefined);
    });

    it('debe llamar onExport con formato PDF cuando se selecciona', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} />);

      const pdfRadio = container.querySelector<HTMLInputElement>('input[value="pdf"]');
      if (pdfRadio) {
        fireEvent.click(pdfRadio);
      }

      const downloadButton = container.querySelector('button.btn-primary');
      if (downloadButton) {
        fireEvent.click(downloadButton);
      }

      expect(defaultProps.onExport).toHaveBeenCalledWith('pdf', undefined);
    });

    it('debe llamar onExport con formato y alcance cuando se selecciona scope', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} showScopeOption={true} />);

      const pdfRadio = container.querySelector<HTMLInputElement>('input[value="pdf"]');
      if (pdfRadio) {
        fireEvent.click(pdfRadio);
      }

      const allRadio = container.querySelector<HTMLInputElement>('input[value="all"]');
      if (allRadio) {
        fireEvent.click(allRadio);
      }

      const downloadButton = container.querySelector('button.btn-primary');
      if (downloadButton) {
        fireEvent.click(downloadButton);
      }

      expect(defaultProps.onExport).toHaveBeenCalledWith('pdf', 'all');
    });

    it('debe llamar onExport con currentPage cuando esa opcion esta seleccionada', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} showScopeOption={true} />);

      const downloadButton = container.querySelector('button.btn-primary');
      if (downloadButton) {
        fireEvent.click(downloadButton);
      }

      expect(defaultProps.onExport).toHaveBeenCalledWith('csv', 'currentPage');
    });
  });

  describe('Estado de carga', () => {
    it('debe deshabilitar botones cuando exporting=true', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} exporting={true} />);

      const downloadButton = container.querySelector<HTMLButtonElement>('button.btn-primary');
      const cancelButton = container.querySelector<HTMLButtonElement>('button.btn-secondary');

      expect(downloadButton?.disabled).toBe(true);
      expect(cancelButton?.disabled).toBe(true);
    });

    it('debe deshabilitar radios cuando exporting=true', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} exporting={true} showScopeOption={true} />);

      const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]');
      radios.forEach(radio => {
        expect(radio.disabled).toBe(true);
      });
    });

    it('debe mostrar spinner cuando exporting=true', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} exporting={true} />);

      const spinner = container.querySelector('.spinner-border');
      expect(spinner).toBeTruthy();
    });
  });

  describe('Boton cancelar', () => {
    it('debe llamar onClose al hacer click en cancelar', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} />);

      const cancelButton = container.querySelector('button.btn-secondary');
      if (cancelButton) {
        fireEvent.click(cancelButton);
      }

      expect(defaultProps.onClose).toHaveBeenCalled();
    });

    it('no debe llamar onClose cuando exporting=true', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} exporting={true} />);

      const cancelButton = container.querySelector<HTMLButtonElement>('button.btn-secondary');

      if (cancelButton) {
        fireEvent.click(cancelButton);
      }

      // No se debe llamar porque el boton esta deshabilitado
      expect(defaultProps.onClose).not.toHaveBeenCalled();
    });
  });

  describe('Modal cerrado', () => {
    it('no debe renderizar contenido cuando isOpen=false', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} isOpen={false} />);

      const modal = container.querySelector('.modal');
      expect(modal).toBeNull();
    });
  });

  describe('Informacion de registros', () => {
    it('debe mostrar advertencia cuando totalElements > 10000', () => {
      const { container } = render(<DivipolExportModal {...defaultProps} showScopeOption={true} totalElements={15000} />);

      // Debe haber un small con la advertencia de maximo
      const smallText = container.querySelector('small.text-muted');
      expect(smallText).toBeTruthy();
    });
  });
});
