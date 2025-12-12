import React from 'react';
import { render, fireEvent, waitFor, act } from '@testing-library/react';
import { Provider } from 'react-redux';
import { TranslatorContext } from 'react-jhipster';
import configureStore from 'redux-mock-store';
import { DivipolExport } from './divipol-export';
import divipolService from 'app/shared/services/divipol.service';
import { toast } from 'react-toastify';
import type { DivipolState } from '../divipol.reducer';

// Mock del servicio
jest.mock('app/shared/services/divipol.service', () => ({
  __esModule: true,
  default: {
    exportFilters: jest.fn(),
    exportSearch: jest.fn(),
  },
}));

// Mock de toast
jest.mock('react-toastify', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn(),
  },
}));

// Mock de URL.createObjectURL y URL.revokeObjectURL
const mockCreateObjectURL = jest.fn(() => 'blob:mock-url');
const mockRevokeObjectURL = jest.fn();
global.URL.createObjectURL = mockCreateObjectURL;
global.URL.revokeObjectURL = mockRevokeObjectURL;

// Mock de Reactstrap Modal para evitar problemas con portales
jest.mock('reactstrap', () => {
  const original = jest.requireActual('reactstrap');
  return {
    ...original,
    Modal: ({ isOpen, children }: { isOpen: boolean; children: React.ReactNode }) =>
      isOpen ? <div className="modal">{children}</div> : null,
  };
});

const mockStore = configureStore([]);

describe('DivipolExport', () => {
  const createInitialState = (overrides: Partial<DivipolState> = {}): { divipol: DivipolState } => ({
    divipol: {
      departamentos: [],
      municipios: [],
      zonas: [],
      puestos: [],
      stats: null,
      filters: {
        departamento: null,
        municipio: null,
        zona: null,
        puesto: null,
      },
      loading: false,
      error: null,
      search: {
        mode: 'name',
        term: '',
        active: false,
        results: [],
        suggestions: [],
        loading: false,
        suggestionsLoading: false,
        pagination: {
          page: 0,
          size: 20,
          totalElements: 0,
          totalPages: 0,
        },
      },
      ...overrides,
    },
  });

  beforeAll(() => {
    TranslatorContext.registerTranslations('es', {});
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Renderizado', () => {
    it('no debe renderizar cuando no hay datos', () => {
      const store = mockStore(createInitialState());
      const { container } = render(
        <Provider store={store}>
          <DivipolExport />
        </Provider>,
      );

      expect(container.firstChild).toBeNull();
    });

    it('debe renderizar dropdown cuando hay departamentos', () => {
      const store = mockStore(
        createInitialState({
          departamentos: [{ coddepto: 5, nomdepto: 'Antioquia', mujeres: 5000, hombres: 4800, totalPotencial: 9800, mesas: 500 }],
        }),
      );

      const { container } = render(
        <Provider store={store}>
          <DivipolExport />
        </Provider>,
      );

      const dropdown = container.querySelector('.dropdown');
      expect(dropdown).toBeTruthy();
    });

    it('debe renderizar dropdown cuando hay resultados de busqueda', () => {
      const store = mockStore(
        createInitialState({
          search: {
            mode: 'name',
            term: 'ANTIOQUIA',
            active: true,
            results: [
              {
                codigoDivipol: '050000000',
                tipo: 'DEPTO',
                coddepto: 5,
                codmipio: 0,
                codzona: 0,
                codpuesto: '',
                nomdepto: 'ANTIOQUIA',
                nommipio: '',
                nompuesto: '',
                potencialTotal: 9800000,
                mesas: 50000,
              },
            ],
            suggestions: [],
            loading: false,
            suggestionsLoading: false,
            pagination: {
              page: 0,
              size: 20,
              totalElements: 1,
              totalPages: 1,
            },
          },
        }),
      );

      const { container } = render(
        <Provider store={store}>
          <DivipolExport />
        </Provider>,
      );

      const dropdown = container.querySelector('.dropdown');
      expect(dropdown).toBeTruthy();
    });
  });

  describe('Exportacion en modo filtros', () => {
    it('debe exportar directamente sin modal en modo filtros', async () => {
      const mockBlob = new Blob(['test'], { type: 'text/csv' });
      (divipolService.exportFilters as jest.Mock).mockResolvedValue(mockBlob);

      const store = mockStore(
        createInitialState({
          departamentos: [{ coddepto: 5, nomdepto: 'Antioquia', mujeres: 5000, hombres: 4800, totalPotencial: 9800, mesas: 500 }],
        }),
      );

      const { container } = render(
        <Provider store={store}>
          <DivipolExport />
        </Provider>,
      );

      // Abrir dropdown
      const dropdownToggle = container.querySelector('.dropdown-toggle');
      act(() => {
        if (dropdownToggle) {
          fireEvent.click(dropdownToggle);
        }
      });

      // Click en CSV
      const csvOption = container.querySelector('.dropdown-item');
      act(() => {
        if (csvOption) {
          fireEvent.click(csvOption);
        }
      });

      await waitFor(() => {
        expect(divipolService.exportFilters).toHaveBeenCalledWith('csv', {
          codDepto: undefined,
          codMpio: undefined,
          codZona: undefined,
        });
      });
    });

    it('debe pasar filtros correctamente al servicio', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/pdf' });
      (divipolService.exportFilters as jest.Mock).mockResolvedValue(mockBlob);

      const store = mockStore(
        createInitialState({
          departamentos: [{ coddepto: 5, nomdepto: 'Antioquia', mujeres: 5000, hombres: 4800, totalPotencial: 9800, mesas: 500 }],
          filters: {
            departamento: 5,
            municipio: 1,
            zona: null,
            puesto: null,
          },
        }),
      );

      const { container } = render(
        <Provider store={store}>
          <DivipolExport />
        </Provider>,
      );

      // Abrir dropdown y seleccionar PDF
      const dropdownToggle = container.querySelector('.dropdown-toggle');
      act(() => {
        if (dropdownToggle) {
          fireEvent.click(dropdownToggle);
        }
      });

      const options = container.querySelectorAll('.dropdown-item');
      const pdfOption = options[1]; // PDF es la segunda opcion
      act(() => {
        if (pdfOption) {
          fireEvent.click(pdfOption);
        }
      });

      await waitFor(() => {
        expect(divipolService.exportFilters).toHaveBeenCalledWith('pdf', {
          codDepto: 5,
          codMpio: 1,
          codZona: undefined,
        });
      });
    });

    it('debe mostrar toast de exito al exportar correctamente', async () => {
      const mockBlob = new Blob(['test'], { type: 'text/csv' });
      (divipolService.exportFilters as jest.Mock).mockResolvedValue(mockBlob);

      const store = mockStore(
        createInitialState({
          departamentos: [{ coddepto: 5, nomdepto: 'Antioquia', mujeres: 5000, hombres: 4800, totalPotencial: 9800, mesas: 500 }],
        }),
      );

      const { container } = render(
        <Provider store={store}>
          <DivipolExport />
        </Provider>,
      );

      // Exportar
      const dropdownToggle = container.querySelector('.dropdown-toggle');
      act(() => {
        if (dropdownToggle) {
          fireEvent.click(dropdownToggle);
        }
      });

      const csvOption = container.querySelector('.dropdown-item');
      act(() => {
        if (csvOption) {
          fireEvent.click(csvOption);
        }
      });

      await waitFor(() => {
        expect(toast.success).toHaveBeenCalled();
      });
    });

    it('debe mostrar toast de error cuando falla la exportacion', async () => {
      (divipolService.exportFilters as jest.Mock).mockRejectedValue(new Error('Export failed'));

      const store = mockStore(
        createInitialState({
          departamentos: [{ coddepto: 5, nomdepto: 'Antioquia', mujeres: 5000, hombres: 4800, totalPotencial: 9800, mesas: 500 }],
        }),
      );

      const { container } = render(
        <Provider store={store}>
          <DivipolExport />
        </Provider>,
      );

      // Exportar
      const dropdownToggle = container.querySelector('.dropdown-toggle');
      act(() => {
        if (dropdownToggle) {
          fireEvent.click(dropdownToggle);
        }
      });

      const csvOption = container.querySelector('.dropdown-item');
      act(() => {
        if (csvOption) {
          fireEvent.click(csvOption);
        }
      });

      await waitFor(() => {
        expect(toast.error).toHaveBeenCalled();
      });
    });
  });

  describe('Exportacion en modo busqueda', () => {
    it('debe abrir modal cuando hay multiples paginas de resultados', () => {
      const store = mockStore(
        createInitialState({
          search: {
            mode: 'name',
            term: 'BOLIVAR',
            active: true,
            results: [
              {
                codigoDivipol: '130000000',
                tipo: 'DEPTO',
                coddepto: 13,
                codmipio: 0,
                codzona: 0,
                codpuesto: '',
                nomdepto: 'BOLIVAR',
                nommipio: '',
                nompuesto: '',
                potencialTotal: 2000000,
                mesas: 10000,
              },
            ],
            suggestions: [],
            loading: false,
            suggestionsLoading: false,
            pagination: {
              page: 0,
              size: 20,
              totalElements: 100,
              totalPages: 5,
            },
          },
        }),
      );

      const { container } = render(
        <Provider store={store}>
          <DivipolExport />
        </Provider>,
      );

      // Abrir dropdown y seleccionar CSV
      const dropdownToggle = container.querySelector('.dropdown-toggle');
      act(() => {
        if (dropdownToggle) {
          fireEvent.click(dropdownToggle);
        }
      });

      const csvOption = container.querySelector('.dropdown-item');
      act(() => {
        if (csvOption) {
          fireEvent.click(csvOption);
        }
      });

      // Debe abrir el modal
      const modal = container.querySelector('.modal');
      expect(modal).toBeTruthy();
    });

    it('debe exportar directamente cuando solo hay una pagina', async () => {
      const mockBlob = new Blob(['test'], { type: 'text/csv' });
      (divipolService.exportSearch as jest.Mock).mockResolvedValue(mockBlob);

      const store = mockStore(
        createInitialState({
          search: {
            mode: 'name',
            term: 'MEDELLIN',
            active: true,
            results: [
              {
                codigoDivipol: '050010000',
                tipo: 'MPIO',
                coddepto: 5,
                codmipio: 1,
                codzona: 0,
                codpuesto: '',
                nomdepto: 'ANTIOQUIA',
                nommipio: 'MEDELLIN',
                nompuesto: '',
                potencialTotal: 2500000,
                mesas: 10000,
              },
            ],
            suggestions: [],
            loading: false,
            suggestionsLoading: false,
            pagination: {
              page: 0,
              size: 20,
              totalElements: 1,
              totalPages: 1,
            },
          },
        }),
      );

      const { container } = render(
        <Provider store={store}>
          <DivipolExport />
        </Provider>,
      );

      // Exportar
      const dropdownToggle = container.querySelector('.dropdown-toggle');
      act(() => {
        if (dropdownToggle) {
          fireEvent.click(dropdownToggle);
        }
      });

      const csvOption = container.querySelector('.dropdown-item');
      act(() => {
        if (csvOption) {
          fireEvent.click(csvOption);
        }
      });

      await waitFor(() => {
        expect(divipolService.exportSearch).toHaveBeenCalledWith('csv', {
          q: 'MEDELLIN',
          mode: 'name',
          page: 0,
          size: 20,
          exportAll: false,
        });
      });
    });

    it('debe pasar exportAll=true cuando se selecciona "todos" en el modal', async () => {
      const mockBlob = new Blob(['test'], { type: 'text/csv' });
      (divipolService.exportSearch as jest.Mock).mockResolvedValue(mockBlob);

      const store = mockStore(
        createInitialState({
          search: {
            mode: 'code',
            term: '05',
            active: true,
            results: [
              {
                codigoDivipol: '050000000',
                tipo: 'DEPTO',
                coddepto: 5,
                codmipio: 0,
                codzona: 0,
                codpuesto: '',
                nomdepto: 'ANTIOQUIA',
                nommipio: '',
                nompuesto: '',
                potencialTotal: 9800000,
                mesas: 50000,
              },
            ],
            suggestions: [],
            loading: false,
            suggestionsLoading: false,
            pagination: {
              page: 0,
              size: 20,
              totalElements: 500,
              totalPages: 25,
            },
          },
        }),
      );

      const { container } = render(
        <Provider store={store}>
          <DivipolExport />
        </Provider>,
      );

      // Abrir dropdown y seleccionar CSV para abrir modal
      const dropdownToggle = container.querySelector('.dropdown-toggle');
      act(() => {
        if (dropdownToggle) {
          fireEvent.click(dropdownToggle);
        }
      });

      const csvOption = container.querySelector('.dropdown-item');
      act(() => {
        if (csvOption) {
          fireEvent.click(csvOption);
        }
      });

      // Seleccionar "todos" en el modal
      const allRadio = container.querySelector<HTMLInputElement>('input[value="all"]');
      act(() => {
        if (allRadio) {
          fireEvent.click(allRadio);
        }
      });

      // Click en descargar
      const downloadButton = container.querySelector('.modal .btn-primary');
      act(() => {
        if (downloadButton) {
          fireEvent.click(downloadButton);
        }
      });

      await waitFor(() => {
        expect(divipolService.exportSearch).toHaveBeenCalledWith('csv', {
          q: '05',
          mode: 'code',
          page: 0,
          size: 20,
          exportAll: true,
        });
      });
    });
  });
});
