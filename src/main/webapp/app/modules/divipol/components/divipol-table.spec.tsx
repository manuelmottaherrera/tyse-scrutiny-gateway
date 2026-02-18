import React from 'react';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import { TranslatorContext } from 'react-jhipster';
import configureStore from 'redux-mock-store';

import { DivipolTable, generateDivipolCode } from './divipol-table';
import { DivipolState } from '../divipol.reducer';

const mockStore = configureStore([]);

describe('divipol-table', () => {
  beforeAll(() => {
    TranslatorContext.registerTranslations('es', {});
  });

  describe('generateDivipolCode function', () => {
    describe('should generate codes WITHOUT spaces', () => {
      it('should generate full code without spaces for complete divipol', () => {
        const result = generateDivipolCode(1, 1, 2, 1);
        expect(result).toBe('010010201');
        expect(result).not.toContain(' ');
      });

      it('should generate code without spaces for departamento only', () => {
        const result = generateDivipolCode(5);
        expect(result).toBe('050000000');
        expect(result).not.toContain(' ');
      });

      it('should generate code without spaces for departamento + municipio', () => {
        const result = generateDivipolCode(11, 1);
        expect(result).toBe('110010000');
        expect(result).not.toContain(' ');
      });

      it('should generate code without spaces for departamento + municipio + zona', () => {
        const result = generateDivipolCode(5, 1, 2);
        expect(result).toBe('050010200');
        expect(result).not.toContain(' ');
      });
    });

    describe('padding rules', () => {
      it('should pad departamento to 2 digits', () => {
        expect(generateDivipolCode(1)).toBe('010000000');
        expect(generateDivipolCode(5)).toBe('050000000');
        expect(generateDivipolCode(11)).toBe('110000000');
        expect(generateDivipolCode(99)).toBe('990000000');
      });

      it('should pad municipio to 3 digits', () => {
        expect(generateDivipolCode(1, 1)).toBe('010010000');
        expect(generateDivipolCode(1, 12)).toBe('010120000');
        expect(generateDivipolCode(1, 123)).toBe('011230000');
      });

      it('should pad zona to 2 digits', () => {
        expect(generateDivipolCode(1, 1, 1)).toBe('010010100');
        expect(generateDivipolCode(1, 1, 12)).toBe('010011200');
      });

      it('should pad puesto to 2 digits', () => {
        expect(generateDivipolCode(1, 1, 1, 1)).toBe('010010101');
        expect(generateDivipolCode(1, 1, 1, 12)).toBe('010010112');
      });

      it('should handle string values correctly', () => {
        expect(generateDivipolCode('1', '1', '2', '1')).toBe('010010201');
        expect(generateDivipolCode('05', '001', '02', '01')).toBe('050010201');
      });

      it('should handle alphanumeric puesto codes', () => {
        expect(generateDivipolCode(1, 1, 1, 'A1')).toBe('0100101A1');
        expect(generateDivipolCode(1, 1, 1, 'AB')).toBe('0100101AB');
      });
    });

    describe('default values for null/undefined', () => {
      it('should use 00 for undefined departamento', () => {
        expect(generateDivipolCode(undefined, 1, 1, 1)).toBe('000010101');
      });

      it('should use 000 for undefined municipio', () => {
        expect(generateDivipolCode(1, undefined, 1, 1)).toBe('010000101');
      });

      it('should use 00 for undefined zona', () => {
        expect(generateDivipolCode(1, 1, undefined, 1)).toBe('010010001');
      });

      it('should use 00 for undefined puesto', () => {
        expect(generateDivipolCode(1, 1, 1, undefined)).toBe('010010100');
      });

      it('should return all zeros when no arguments provided', () => {
        expect(generateDivipolCode()).toBe('000000000');
      });
    });

    describe('code length', () => {
      it('should always return a 9-character code (2+3+2+2)', () => {
        expect(generateDivipolCode().length).toBe(9);
        expect(generateDivipolCode(1).length).toBe(9);
        expect(generateDivipolCode(1, 1).length).toBe(9);
        expect(generateDivipolCode(1, 1, 1).length).toBe(9);
        expect(generateDivipolCode(1, 1, 1, 1).length).toBe(9);
        expect(generateDivipolCode(99, 999, 99, 99).length).toBe(9);
      });
    });
  });

  describe('DivipolTable component', () => {
    const createInitialState = (overrides: Partial<DivipolState> = {}) => ({
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
        ...overrides,
      },
      authentication: {
        account: {
          authorities: [] as string[],
        },
      },
    });

    it('should show "no data" message when there is no data', () => {
      const store = mockStore(createInitialState());

      const { container } = render(
        <MemoryRouter>
          <Provider store={store}>
            <DivipolTable />
          </Provider>
        </MemoryRouter>,
      );

      const alert = container.querySelector('.alert-info');
      expect(alert).toBeTruthy();
      expect(alert?.textContent).toContain('No hay datos para mostrar');
    });

    it('should render departamentos with correct divipol code format (no spaces)', () => {
      const store = mockStore(
        createInitialState({
          departamentos: [
            { coddepto: 5, nomdepto: 'Antioquia', mujeres: 5000, hombres: 4800, totalPotencial: 9800, mesas: 500 },
            { coddepto: 11, nomdepto: 'Bogotá D.C.', mujeres: 6000, hombres: 5500, totalPotencial: 11500, mesas: 600 },
          ],
        }),
      );

      const { container } = render(
        <MemoryRouter>
          <Provider store={store}>
            <DivipolTable />
          </Provider>
        </MemoryRouter>,
      );

      // Get all table cells
      const cells = container.querySelectorAll('tbody td');
      const cellTexts = Array.from(cells).map(cell => cell.textContent);

      // Verify codes are rendered WITHOUT spaces (9 digits: 2+3+2+2)
      expect(cellTexts).toContain('050000000');
      expect(cellTexts).toContain('110000000');

      // Verify names are rendered
      expect(cellTexts).toContain('Antioquia');
      expect(cellTexts).toContain('Bogotá D.C.');
    });

    it('should render municipios with correct divipol code format (no spaces)', () => {
      const store = mockStore(
        createInitialState({
          filters: { departamento: 5, municipio: null, zona: null, puesto: null },
          municipios: [
            {
              coddepto: 5,
              codmipio: 1,
              nommipio: 'Medellín',
              potencialFemenino: 2000,
              potencialMasculino: 1800,
              potencialTotal: 3800,
              mesas: 200,
            },
            {
              coddepto: 5,
              codmipio: 79,
              nommipio: 'Abejorral',
              potencialFemenino: 100,
              potencialMasculino: 90,
              potencialTotal: 190,
              mesas: 10,
            },
          ],
        }),
      );

      const { container } = render(
        <MemoryRouter>
          <Provider store={store}>
            <DivipolTable />
          </Provider>
        </MemoryRouter>,
      );

      const cells = container.querySelectorAll('tbody td');
      const cellTexts = Array.from(cells).map(cell => cell.textContent);

      // Verify codes are rendered WITHOUT spaces
      expect(cellTexts).toContain('050010000');
      expect(cellTexts).toContain('050790000');

      // Verify names are rendered
      expect(cellTexts).toContain('Medellín');
      expect(cellTexts).toContain('Abejorral');
    });

    it('should render zonas with correct divipol code format (no spaces)', () => {
      const store = mockStore(
        createInitialState({
          filters: { departamento: 5, municipio: 1, zona: null, puesto: null },
          zonas: [
            { coddepto: 5, codmipio: 1, codzona: 1, potencialFemenino: 500, potencialMasculino: 450, potencialTotal: 950, mesas: 50 },
            { coddepto: 5, codmipio: 1, codzona: 2, potencialFemenino: 480, potencialMasculino: 430, potencialTotal: 910, mesas: 48 },
          ],
        }),
      );

      const { container } = render(
        <MemoryRouter>
          <Provider store={store}>
            <DivipolTable />
          </Provider>
        </MemoryRouter>,
      );

      const cells = container.querySelectorAll('tbody td');
      const cellTexts = Array.from(cells).map(cell => cell.textContent);

      // Verify codes are rendered WITHOUT spaces
      expect(cellTexts).toContain('050010100');
      expect(cellTexts).toContain('050010200');
    });

    it('should render puestos with correct divipol code format (no spaces)', () => {
      const store = mockStore(
        createInitialState({
          filters: { departamento: 5, municipio: 1, zona: 2, puesto: null },
          puestos: [
            {
              coddepto: 5,
              codmipio: 1,
              codzona: 2,
              codpuesto: '01',
              nompuesto: 'Puesto Principal',
              potencialFemenino: 100,
              potencialMasculino: 90,
              potencialTotal: 190,
              mesas: 10,
            },
            {
              coddepto: 5,
              codmipio: 1,
              codzona: 2,
              codpuesto: '02',
              nompuesto: 'Puesto Secundario',
              potencialFemenino: 80,
              potencialMasculino: 70,
              potencialTotal: 150,
              mesas: 8,
            },
          ],
        }),
      );

      const { container } = render(
        <MemoryRouter>
          <Provider store={store}>
            <DivipolTable />
          </Provider>
        </MemoryRouter>,
      );

      const cells = container.querySelectorAll('tbody td');
      const cellTexts = Array.from(cells).map(cell => cell.textContent);

      // Verify codes are rendered WITHOUT spaces
      expect(cellTexts).toContain('050010201');
      expect(cellTexts).toContain('050010202');

      // Verify names are rendered
      expect(cellTexts).toContain('Puesto Principal');
      expect(cellTexts).toContain('Puesto Secundario');
    });

    it('should render table headers correctly', () => {
      const store = mockStore(
        createInitialState({
          departamentos: [{ coddepto: 1, nomdepto: 'Test', mujeres: 100, hombres: 100, totalPotencial: 200, mesas: 10 }],
        }),
      );

      const { container } = render(
        <MemoryRouter>
          <Provider store={store}>
            <DivipolTable />
          </Provider>
        </MemoryRouter>,
      );

      const headers = container.querySelectorAll('thead th');
      const headerTexts = Array.from(headers).map(h => h.textContent);

      expect(headerTexts).toContain('Cod. Divipol');
      expect(headerTexts).toContain('Nombre');
      expect(headerTexts).toContain('Mujeres');
      expect(headerTexts).toContain('Hombres');
      expect(headerTexts).toContain('Total');
      expect(headerTexts).toContain('Mesas');
    });

    it('should format numbers with locale es-CO (dots as thousand separators)', () => {
      const store = mockStore(
        createInitialState({
          departamentos: [{ coddepto: 1, nomdepto: 'Test', mujeres: 1234567, hombres: 7654321, totalPotencial: 8888888, mesas: 999 }],
        }),
      );

      const { container } = render(
        <MemoryRouter>
          <Provider store={store}>
            <DivipolTable />
          </Provider>
        </MemoryRouter>,
      );

      const cells = container.querySelectorAll('tbody td');
      const cellTexts = Array.from(cells).map(cell => cell.textContent);

      // Numbers should be formatted with thousand separators (es-CO uses dots)
      expect(cellTexts).toContain('1.234.567');
      expect(cellTexts).toContain('7.654.321');
      expect(cellTexts).toContain('8.888.888');
    });
  });
});
