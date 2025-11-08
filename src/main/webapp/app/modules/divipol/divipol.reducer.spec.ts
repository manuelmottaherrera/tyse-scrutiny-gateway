import divipol, {
  fetchDepartamentos,
  fetchMunicipios,
  fetchZonas,
  fetchGeneralStats,
  setFilterDepartamento,
  setFilterMunicipio,
  setFilterZona,
  setFilterPuesto,
  resetFilters,
  clearError,
  DivipolState,
} from './divipol.reducer';

describe('Divipol reducer tests', () => {
  describe('Common tests', () => {
    it('should return the initial state', () => {
      const expectedInitialState: DivipolState = {
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
      };

      expect(divipol(undefined, { type: '' })).toEqual(expectedInitialState);
    });
  });

  describe('Synchronous actions', () => {
    it('should set departamento filter and clear dependent filters and data', () => {
      const stateWithData: DivipolState = {
        departamentos: [{ coddepto: 1, nomdepto: 'Antioquia', mujeres: 100, hombres: 100, totalPotencial: 200, mesas: 10 }],
        municipios: [
          { coddepto: 1, codmipio: 1, nommipio: 'Medellín', potencialFemenino: 50, potencialMasculino: 50, potencialTotal: 100, mesas: 5 },
        ],
        zonas: [{ coddepto: 1, codmipio: 1, codzona: 1, potencialFemenino: 25, potencialMasculino: 25, potencialTotal: 50, mesas: 2 }],
        puestos: [],
        stats: null,
        filters: {
          departamento: 1,
          municipio: 1,
          zona: 1,
          puesto: '01',
        },
        loading: false,
        error: null,
      };

      const result = divipol(stateWithData, setFilterDepartamento(5));

      expect(result.filters.departamento).toBe(5);
      expect(result.filters.municipio).toBeNull();
      expect(result.filters.zona).toBeNull();
      expect(result.filters.puesto).toBeNull();
      expect(result.municipios).toEqual([]);
      expect(result.zonas).toEqual([]);
      expect(result.puestos).toEqual([]);
    });

    it('should set municipio filter and clear dependent filters and data', () => {
      const stateWithData: DivipolState = {
        departamentos: [],
        municipios: [
          { coddepto: 1, codmipio: 1, nommipio: 'Medellín', potencialFemenino: 50, potencialMasculino: 50, potencialTotal: 100, mesas: 5 },
        ],
        zonas: [{ coddepto: 1, codmipio: 1, codzona: 1, potencialFemenino: 25, potencialMasculino: 25, potencialTotal: 50, mesas: 2 }],
        puestos: [],
        stats: null,
        filters: {
          departamento: 1,
          municipio: 1,
          zona: 1,
          puesto: '01',
        },
        loading: false,
        error: null,
      };

      const result = divipol(stateWithData, setFilterMunicipio(2));

      expect(result.filters.municipio).toBe(2);
      expect(result.filters.zona).toBeNull();
      expect(result.filters.puesto).toBeNull();
      expect(result.zonas).toEqual([]);
      expect(result.puestos).toEqual([]);
    });

    it('should set zona filter and clear puesto filter and data', () => {
      const stateWithData: DivipolState = {
        departamentos: [],
        municipios: [],
        zonas: [],
        puestos: [
          {
            coddepto: 1,
            codmipio: 1,
            codzona: 1,
            codpuesto: '01',
            nompuesto: 'Puesto 1',
            potencialFemenino: 10,
            potencialMasculino: 10,
            potencialTotal: 20,
            mesas: 1,
          },
        ],
        stats: null,
        filters: {
          departamento: 1,
          municipio: 1,
          zona: 1,
          puesto: '01',
        },
        loading: false,
        error: null,
      };

      const result = divipol(stateWithData, setFilterZona(2));

      expect(result.filters.zona).toBe(2);
      expect(result.filters.puesto).toBeNull();
      expect(result.puestos).toEqual([]);
    });

    it('should set puesto filter without clearing anything', () => {
      const stateWithData: DivipolState = {
        departamentos: [],
        municipios: [],
        zonas: [],
        puestos: [],
        stats: null,
        filters: {
          departamento: 1,
          municipio: 1,
          zona: 1,
          puesto: null,
        },
        loading: false,
        error: null,
      };

      const result = divipol(stateWithData, setFilterPuesto('02'));

      expect(result.filters.puesto).toBe('02');
      expect(result.filters.departamento).toBe(1);
      expect(result.filters.municipio).toBe(1);
      expect(result.filters.zona).toBe(1);
    });

    it('should reset all filters and clear dependent data', () => {
      const stateWithData: DivipolState = {
        departamentos: [{ coddepto: 1, nomdepto: 'Antioquia', mujeres: 100, hombres: 100, totalPotencial: 200, mesas: 10 }],
        municipios: [
          { coddepto: 1, codmipio: 1, nommipio: 'Medellín', potencialFemenino: 50, potencialMasculino: 50, potencialTotal: 100, mesas: 5 },
        ],
        zonas: [{ coddepto: 1, codmipio: 1, codzona: 1, potencialFemenino: 25, potencialMasculino: 25, potencialTotal: 50, mesas: 2 }],
        puestos: [
          {
            coddepto: 1,
            codmipio: 1,
            codzona: 1,
            codpuesto: '01',
            nompuesto: 'Puesto 1',
            potencialFemenino: 10,
            potencialMasculino: 10,
            potencialTotal: 20,
            mesas: 1,
          },
        ],
        stats: null,
        filters: {
          departamento: 1,
          municipio: 1,
          zona: 1,
          puesto: '01',
        },
        loading: false,
        error: null,
      };

      const result = divipol(stateWithData, resetFilters());

      expect(result.filters).toEqual({
        departamento: null,
        municipio: null,
        zona: null,
        puesto: null,
      });
      expect(result.municipios).toEqual([]);
      expect(result.zonas).toEqual([]);
      expect(result.puestos).toEqual([]);
      expect(result.departamentos).toHaveLength(1); // No se limpian los departamentos
    });

    it('should clear error', () => {
      const stateWithError: DivipolState = {
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
        error: 'Something went wrong',
      };

      const result = divipol(stateWithError, clearError());

      expect(result.error).toBeNull();
    });
  });

  describe('Async thunks - Departamentos', () => {
    it('should set loading true and clear error on fetchDepartamentos pending', () => {
      const initialStateWithError: DivipolState = {
        ...divipol(undefined, { type: '' }),
        error: 'Previous error',
      };

      const result = divipol(initialStateWithError, { type: fetchDepartamentos.pending.type });

      expect(result.loading).toBe(true);
      expect(result.error).toBeNull();
    });

    it('should store departamentos on fetchDepartamentos fulfilled', () => {
      const mockDepartamentos = [
        { coddepto: 5, nomdepto: 'Antioquia', mujeres: 5000000, hombres: 4800000, totalPotencial: 9800000, mesas: 50000 },
        { coddepto: 11, nomdepto: 'Bogotá D.C.', mujeres: 6000000, hombres: 5500000, totalPotencial: 11500000, mesas: 60000 },
      ];

      const result = divipol(undefined, {
        type: fetchDepartamentos.fulfilled.type,
        payload: mockDepartamentos,
      });

      expect(result.loading).toBe(false);
      expect(result.departamentos).toEqual(mockDepartamentos);
      expect(result.error).toBeNull();
    });

    it('should set error on fetchDepartamentos rejected', () => {
      const error = { message: 'Network error' };

      const result = divipol(undefined, {
        type: fetchDepartamentos.rejected.type,
        error,
      });

      expect(result.loading).toBe(false);
      expect(result.error).toBe('Network error');
    });

    it('should use default error message if none provided on rejected', () => {
      const result = divipol(undefined, {
        type: fetchDepartamentos.rejected.type,
        error: {},
      });

      expect(result.error).toBe('Error al cargar departamentos');
    });
  });

  describe('Async thunks - Municipios', () => {
    it('should set loading true on fetchMunicipios pending', () => {
      const result = divipol(undefined, { type: fetchMunicipios.pending.type });

      expect(result.loading).toBe(true);
      expect(result.error).toBeNull();
    });

    it('should store municipios on fetchMunicipios fulfilled', () => {
      const mockMunicipios = [
        {
          coddepto: 5,
          codmipio: 1,
          nommipio: 'Medellín',
          potencialFemenino: 2000000,
          potencialMasculino: 1800000,
          potencialTotal: 3800000,
          mesas: 20000,
        },
        {
          coddepto: 5,
          codmipio: 2,
          nommipio: 'Abejorral',
          potencialFemenino: 10000,
          potencialMasculino: 9000,
          potencialTotal: 19000,
          mesas: 100,
        },
      ];

      const result = divipol(undefined, {
        type: fetchMunicipios.fulfilled.type,
        payload: mockMunicipios,
      });

      expect(result.loading).toBe(false);
      expect(result.municipios).toEqual(mockMunicipios);
    });

    it('should set error on fetchMunicipios rejected', () => {
      const error = { message: 'Failed to fetch municipalities' };

      const result = divipol(undefined, {
        type: fetchMunicipios.rejected.type,
        error,
      });

      expect(result.loading).toBe(false);
      expect(result.error).toBe('Failed to fetch municipalities');
    });
  });

  describe('Async thunks - Zonas', () => {
    it('should set loading true on fetchZonas pending', () => {
      const result = divipol(undefined, { type: fetchZonas.pending.type });

      expect(result.loading).toBe(true);
    });

    it('should store zonas on fetchZonas fulfilled', () => {
      const mockZonas = [
        {
          coddepto: 5,
          codmipio: 1,
          codzona: 1,
          potencialFemenino: 500000,
          potencialMasculino: 450000,
          potencialTotal: 950000,
          mesas: 5000,
        },
        {
          coddepto: 5,
          codmipio: 1,
          codzona: 2,
          potencialFemenino: 480000,
          potencialMasculino: 430000,
          potencialTotal: 910000,
          mesas: 4800,
        },
      ];

      const result = divipol(undefined, {
        type: fetchZonas.fulfilled.type,
        payload: mockZonas,
      });

      expect(result.loading).toBe(false);
      expect(result.zonas).toEqual(mockZonas);
    });

    it('should set error on fetchZonas rejected', () => {
      const result = divipol(undefined, {
        type: fetchZonas.rejected.type,
        error: {},
      });

      expect(result.error).toBe('Error al cargar zonas');
    });
  });

  describe('Async thunks - General Stats', () => {
    it('should set loading true on fetchGeneralStats pending', () => {
      const result = divipol(undefined, { type: fetchGeneralStats.pending.type });

      expect(result.loading).toBe(true);
    });

    it('should store stats on fetchGeneralStats fulfilled', () => {
      const mockStats = {
        totalDepartamentos: 33,
        totalMunicipios: 1103,
        totalZonas: 5000,
        totalMesas: 120000,
        potencialFemenino: 25000000,
        potencialMasculino: 23000000,
        potencialTotal: 48000000,
      };

      const result = divipol(undefined, {
        type: fetchGeneralStats.fulfilled.type,
        payload: mockStats,
      });

      expect(result.loading).toBe(false);
      expect(result.stats).toEqual(mockStats);
    });

    it('should set error on fetchGeneralStats rejected', () => {
      const error = { message: 'Stats service unavailable' };

      const result = divipol(undefined, {
        type: fetchGeneralStats.rejected.type,
        error,
      });

      expect(result.loading).toBe(false);
      expect(result.error).toBe('Stats service unavailable');
    });
  });
});
