import axios from 'axios';
import sinon from 'sinon';
import divipolService from './divipol.service';

describe('Divipol service tests', () => {
  let axiosGetStub: sinon.SinonStub;
  let axiosPostStub: sinon.SinonStub;
  let axiosPutStub: sinon.SinonStub;
  let axiosDeleteStub: sinon.SinonStub;

  beforeEach(() => {
    axiosGetStub = sinon.stub(axios, 'get');
    axiosPostStub = sinon.stub(axios, 'post');
    axiosPutStub = sinon.stub(axios, 'put');
    axiosDeleteStub = sinon.stub(axios, 'delete');
  });

  afterEach(() => {
    axiosGetStub.restore();
    axiosPostStub.restore();
    axiosPutStub.restore();
    axiosDeleteStub.restore();
  });

  describe('getDepartamentos', () => {
    it('should call correct API endpoint', async () => {
      const mockData = [
        { coddepto: 5, nomdepto: 'Antioquia', mujeres: 5000000, hombres: 4800000, totalPotencial: 9800000, mesas: 50000 },
        { coddepto: 11, nomdepto: 'Bogotá D.C.', mujeres: 6000000, hombres: 5500000, totalPotencial: 11500000, mesas: 60000 },
      ];
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.getDepartamentos();

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/departamentos')).toBe(true);
      expect(result).toEqual(mockData);
    });

    it('should handle errors', async () => {
      axiosGetStub.rejects(new Error('Network error'));

      await expect(divipolService.getDepartamentos()).rejects.toThrow('Network error');
    });
  });

  describe('getMunicipios', () => {
    it('should call correct API endpoint with codDepto parameter', async () => {
      const codDepto = 5;
      const mockData = [
        {
          coddepto: 5,
          codmipio: 1,
          nomdepto: 'Antioquia',
          nommipio: 'Medellín',
          potencialFemenino: 2000000,
          potencialMasculino: 1800000,
          potencialTotal: 3800000,
          mesas: 20000,
        },
      ];
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.getMunicipios(codDepto);

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/municipios', {
          params: { codDepto: 5 },
        }),
      ).toBe(true);
      expect(result).toEqual(mockData);
    });
  });

  describe('getZonas', () => {
    it('should call correct API endpoint with codDepto and codMpio parameters', async () => {
      const mockData = [
        {
          coddepto: 5,
          codmipio: 1,
          codzona: 1,
          nomdepto: 'Antioquia',
          nommipio: 'Medellín',
          potencialFemenino: 500000,
          potencialMasculino: 450000,
          potencialTotal: 950000,
          mesas: 5000,
        },
      ];
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.getZonas(5, 1);

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/zonas', {
          params: { codDepto: 5, codMpio: 1 },
        }),
      ).toBe(true);
      expect(result).toEqual(mockData);
    });
  });

  describe('getPuestos', () => {
    it('should call correct API endpoint with all parameters', async () => {
      const mockData = [
        {
          coddepto: 5,
          codmipio: 1,
          codzona: 1,
          codpuesto: '01',
          nomdepto: 'Antioquia',
          nommipio: 'Medellín',
          nompuesto: 'Puesto 1',
          potencialFemenino: 50000,
          potencialMasculino: 45000,
          potencialTotal: 95000,
          mesas: 500,
        },
      ];
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.getPuestos(5, 1, 1);

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/puestos', {
          params: { codDepto: 5, codMpio: 1, codZona: 1 },
        }),
      ).toBe(true);
      expect(result).toEqual(mockData);
    });

    it('should handle errors on getPuestos', async () => {
      axiosGetStub.rejects(new Error('Service unavailable'));

      await expect(divipolService.getPuestos(5, 1, 1)).rejects.toThrow('Service unavailable');
    });
  });

  describe('getGeneralStats', () => {
    it('should call correct API endpoint for general stats', async () => {
      const mockStats = {
        totalDepartamentos: 33,
        totalMunicipios: 1103,
        totalZonas: 5000,
        totalPuestos: 100000,
        totalMesas: 120000,
        potencialFemenino: 25000000,
        potencialMasculino: 23000000,
        potencialTotal: 48000000,
      };
      axiosGetStub.resolves({ data: mockStats });

      const result = await divipolService.getGeneralStats();

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/stats')).toBe(true);
      expect(result).toEqual(mockStats);
    });
  });

  describe('getStatsByDepartamento', () => {
    it('should call correct API endpoint with departamento path param', async () => {
      const mockStats = {
        totalDepartamentos: 1,
        totalMunicipios: 125,
        totalZonas: 600,
        totalPuestos: 8000,
        totalMesas: 50000,
        potencialFemenino: 5000000,
        potencialMasculino: 4800000,
        potencialTotal: 9800000,
      };
      axiosGetStub.resolves({ data: mockStats });

      const result = await divipolService.getStatsByDepartamento(5);

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/stats/departamento/5')).toBe(true);
      expect(result).toEqual(mockStats);
    });
  });

  describe('getStatsByMunicipio', () => {
    it('should call correct API endpoint with municipio path params', async () => {
      const mockStats = {
        totalDepartamentos: 1,
        totalMunicipios: 1,
        totalZonas: 25,
        totalPuestos: 2000,
        totalMesas: 20000,
        potencialFemenino: 2000000,
        potencialMasculino: 1800000,
        potencialTotal: 3800000,
      };
      axiosGetStub.resolves({ data: mockStats });

      const result = await divipolService.getStatsByMunicipio(5, 1);

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/stats/municipio/5/1')).toBe(true);
      expect(result).toEqual(mockStats);
    });

    it('should handle errors on getStatsByMunicipio', async () => {
      axiosGetStub.rejects(new Error('Timeout'));

      await expect(divipolService.getStatsByMunicipio(5, 1)).rejects.toThrow('Timeout');
    });
  });

  describe('getStatsByZona', () => {
    it('should call correct API endpoint with zona path params', async () => {
      const mockStats = {
        totalDepartamentos: 1,
        totalMunicipios: 1,
        totalZonas: 1,
        totalPuestos: 50,
        totalMesas: 5000,
        potencialFemenino: 500000,
        potencialMasculino: 450000,
        potencialTotal: 950000,
      };
      axiosGetStub.resolves({ data: mockStats });

      const result = await divipolService.getStatsByZona(5, 1, 1);

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/stats/zona/5/1/1')).toBe(true);
      expect(result).toEqual(mockStats);
    });
  });

  describe('exportFilters', () => {
    it('should call correct endpoint for CSV export', async () => {
      const mockBlob = new Blob(['test'], { type: 'text/csv' });
      axiosGetStub.resolves({ data: mockBlob });

      const result = await divipolService.exportFilters('csv', { codDepto: 5 });

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/export/filters/csv', {
          params: { codDepto: 5, codMpio: undefined, codZona: undefined },
          responseType: 'blob',
        }),
      ).toBe(true);
      expect(result).toEqual(mockBlob);
    });

    it('should call correct endpoint for PDF export', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/pdf' });
      axiosGetStub.resolves({ data: mockBlob });

      const result = await divipolService.exportFilters('pdf', {});

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/export/filters/pdf', {
          params: { codDepto: undefined, codMpio: undefined, codZona: undefined },
          responseType: 'blob',
        }),
      ).toBe(true);
      expect(result).toEqual(mockBlob);
    });

    it('should pass all filter parameters correctly', async () => {
      const mockBlob = new Blob(['test'], { type: 'text/csv' });
      axiosGetStub.resolves({ data: mockBlob });

      await divipolService.exportFilters('csv', { codDepto: 5, codMpio: 1, codZona: 2 });

      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/export/filters/csv', {
          params: { codDepto: 5, codMpio: 1, codZona: 2 },
          responseType: 'blob',
        }),
      ).toBe(true);
    });

    it('should handle export errors', async () => {
      axiosGetStub.rejects(new Error('Export failed'));

      await expect(divipolService.exportFilters('csv', {})).rejects.toThrow('Export failed');
    });
  });

  describe('exportSearch', () => {
    it('should call correct endpoint for CSV export with search params', async () => {
      const mockBlob = new Blob(['test'], { type: 'text/csv' });
      axiosGetStub.resolves({ data: mockBlob });

      const result = await divipolService.exportSearch('csv', {
        q: 'BOLIVAR',
        mode: 'name',
        page: 0,
        size: 20,
        exportAll: false,
      });

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/export/search/csv', {
          params: { q: 'BOLIVAR', mode: 'name', page: 0, size: 20, exportAll: false },
          responseType: 'blob',
        }),
      ).toBe(true);
      expect(result).toEqual(mockBlob);
    });

    it('should call correct endpoint for PDF export', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/pdf' });
      axiosGetStub.resolves({ data: mockBlob });

      await divipolService.exportSearch('pdf', {
        q: 'MEDELLIN',
        mode: 'name',
        page: 1,
        size: 50,
        exportAll: false,
      });

      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/export/search/pdf', {
          params: { q: 'MEDELLIN', mode: 'name', page: 1, size: 50, exportAll: false },
          responseType: 'blob',
        }),
      ).toBe(true);
    });

    it('should set exportAll=true when exporting all results', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/pdf' });
      axiosGetStub.resolves({ data: mockBlob });

      await divipolService.exportSearch('pdf', {
        q: '050',
        mode: 'code',
        exportAll: true,
      });

      const callArgs = axiosGetStub.getCall(0).args[1];
      expect(callArgs.params.exportAll).toBe(true);
      expect(callArgs.params.mode).toBe('code');
    });

    it('should handle search export errors', async () => {
      axiosGetStub.rejects(new Error('Search export failed'));

      await expect(
        divipolService.exportSearch('csv', {
          q: 'test',
          mode: 'name',
          exportAll: false,
        }),
      ).rejects.toThrow('Search export failed');
    });
  });

  // =====================================================
  // Tests para Detalle de Puesto
  // =====================================================

  describe('getPuestoDetalle', () => {
    it('should call correct API endpoint with puestoId', async () => {
      const mockData = {
        iddivipol: 100,
        coddepto: 5,
        codmipio: 1,
        codzona: 1,
        codpuesto: '01',
        nomdepto: 'Antioquia',
        nommipio: 'Medellín',
        nompuesto: 'IE La Paz',
        direccion: 'Calle 10 #20-30',
        latitud: 6.2442,
        longitud: -75.5812,
        nummesas: 10,
        potfemenino: 5000,
        potmasculino: 4500,
        pottotal: 9500,
        totalJurados: 30,
        totalTestigos: 2,
      };
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.getPuestoDetalle(100);

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/puestos/100/detalle')).toBe(true);
      expect(result).toEqual(mockData);
    });

    it('should handle errors', async () => {
      axiosGetStub.rejects(new Error('Puesto not found'));

      await expect(divipolService.getPuestoDetalle(999)).rejects.toThrow('Puesto not found');
    });
  });

  describe('getJuradosByPuesto', () => {
    it('should call correct API endpoint with puestoId', async () => {
      const mockData = [
        { id: 1, tipoDocumento: 'CC', numeroDocumento: '12345678', nombres: 'Juan', apellidos: 'Pérez' },
        { id: 2, tipoDocumento: 'CC', numeroDocumento: '87654321', nombres: 'María', apellidos: 'López' },
      ];
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.getJuradosByPuesto(100);

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/puestos/100/jurados')).toBe(true);
      expect(result).toEqual(mockData);
    });
  });

  describe('getTestigosByPuesto', () => {
    it('should call correct API endpoint with puestoId', async () => {
      const mockData = [
        {
          testigoId: 1,
          tipoDocumento: 'CC',
          numeroDocumento: '11111111',
          nombreCompleto: 'Carlos García',
          telefono: '3001234567',
          assignedDate: '2025-01-15T10:00:00Z',
          assignedBy: 'admin',
        },
      ];
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.getTestigosByPuesto(100);

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/puestos/100/testigos')).toBe(true);
      expect(result).toEqual(mockData);
    });
  });

  // =====================================================
  // Tests para Asignación/Desasignación de Testigos
  // =====================================================

  describe('asignarTestigoAPuesto', () => {
    it('should call correct API endpoint with puestoId and testigoId', async () => {
      const mockData = {
        testigoId: 5,
        tipoDocumento: 'CC',
        numeroDocumento: '11111111',
        nombreCompleto: 'Carlos García',
        telefono: '3001234567',
        assignedDate: '2025-01-15T10:00:00Z',
        assignedBy: 'admin',
      };
      axiosPostStub.resolves({ data: mockData });

      const result = await divipolService.asignarTestigoAPuesto(100, 5);

      expect(axiosPostStub.calledOnce).toBe(true);
      expect(axiosPostStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/puestos/100/testigos/5')).toBe(true);
      expect(result).toEqual(mockData);
    });

    it('should handle conflict errors', async () => {
      axiosPostStub.rejects(new Error('Testigo already assigned'));

      await expect(divipolService.asignarTestigoAPuesto(100, 5)).rejects.toThrow('Testigo already assigned');
    });
  });

  describe('desasignarTestigoDePuesto', () => {
    it('should call correct API endpoint with puestoId and testigoId', async () => {
      axiosDeleteStub.resolves({ data: undefined });

      await divipolService.desasignarTestigoDePuesto(100, 5);

      expect(axiosDeleteStub.calledOnce).toBe(true);
      expect(axiosDeleteStub.calledWith('/services/tysescrutinymicrodivipol/api/divipol/puestos/100/testigos/5')).toBe(true);
    });

    it('should handle errors', async () => {
      axiosDeleteStub.rejects(new Error('Assignment not found'));

      await expect(divipolService.desasignarTestigoDePuesto(100, 5)).rejects.toThrow('Assignment not found');
    });
  });

  // =====================================================
  // Tests para CRUD de Testigos Electorales
  // =====================================================

  describe('getAllTestigos', () => {
    it('should call correct API endpoint with default pagination', async () => {
      const mockData = {
        content: [
          {
            id: 1,
            tipoDocumento: 'CC',
            numeroDocumento: '12345678',
            nombres: 'Juan',
            apellidos: 'Pérez',
            activo: true,
            puestosAsignados: 2,
          },
        ],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      };
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.getAllTestigos();

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/testigos', {
          params: { page: 0, size: 20 },
        }),
      ).toBe(true);
      expect(result).toEqual(mockData);
    });

    it('should call with custom pagination parameters', async () => {
      const mockData = { content: [], page: 2, size: 10, totalElements: 0, totalPages: 0 };
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.getAllTestigos(2, 10);

      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/testigos', {
          params: { page: 2, size: 10 },
        }),
      ).toBe(true);
      expect(result).toEqual(mockData);
    });
  });

  describe('getTestigoById', () => {
    it('should call correct API endpoint with testigo ID', async () => {
      const mockData = {
        id: 1,
        tipoDocumento: 'CC',
        numeroDocumento: '12345678',
        nombres: 'Juan',
        apellidos: 'Pérez',
        telefono: '3001234567',
        email: 'juan@test.com',
        activo: true,
        puestosAsignados: 2,
      };
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.getTestigoById(1);

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/testigos/1')).toBe(true);
      expect(result).toEqual(mockData);
    });

    it('should handle not found errors', async () => {
      axiosGetStub.rejects(new Error('Testigo not found'));

      await expect(divipolService.getTestigoById(999)).rejects.toThrow('Testigo not found');
    });
  });

  describe('createTestigo', () => {
    it('should call correct API endpoint with testigo data', async () => {
      const inputData = {
        tipoDocumento: 'CC',
        numeroDocumento: '12345678',
        nombres: 'Juan',
        apellidos: 'Pérez',
        telefono: '3001234567',
        email: 'juan@test.com',
      };
      const mockResponse = {
        id: 1,
        ...inputData,
        activo: true,
        puestosAsignados: 0,
      };
      axiosPostStub.resolves({ data: mockResponse });

      const result = await divipolService.createTestigo(inputData);

      expect(axiosPostStub.calledOnce).toBe(true);
      expect(axiosPostStub.calledWith('/services/tysescrutinymicrodivipol/api/testigos', inputData)).toBe(true);
      expect(result).toEqual(mockResponse);
    });

    it('should handle conflict errors for duplicate document', async () => {
      axiosPostStub.rejects(new Error('Duplicate document'));

      await expect(
        divipolService.createTestigo({
          tipoDocumento: 'CC',
          numeroDocumento: '12345678',
          nombres: 'Juan',
          apellidos: 'Pérez',
        }),
      ).rejects.toThrow('Duplicate document');
    });
  });

  describe('updateTestigo', () => {
    it('should call correct API endpoint with testigo ID and update data', async () => {
      const updateData = { nombres: 'Juan Carlos', apellidos: 'Pérez López' };
      const mockResponse = {
        id: 1,
        tipoDocumento: 'CC',
        numeroDocumento: '12345678',
        nombres: 'Juan Carlos',
        apellidos: 'Pérez López',
        activo: true,
        puestosAsignados: 2,
      };
      axiosPutStub.resolves({ data: mockResponse });

      const result = await divipolService.updateTestigo(1, updateData);

      expect(axiosPutStub.calledOnce).toBe(true);
      expect(axiosPutStub.calledWith('/services/tysescrutinymicrodivipol/api/testigos/1', updateData)).toBe(true);
      expect(result).toEqual(mockResponse);
    });

    it('should handle not found errors', async () => {
      axiosPutStub.rejects(new Error('Testigo not found'));

      await expect(divipolService.updateTestigo(999, { nombres: 'Test' })).rejects.toThrow('Testigo not found');
    });
  });

  describe('deleteTestigo', () => {
    it('should call correct API endpoint with testigo ID', async () => {
      axiosDeleteStub.resolves({ data: undefined });

      await divipolService.deleteTestigo(1);

      expect(axiosDeleteStub.calledOnce).toBe(true);
      expect(axiosDeleteStub.calledWith('/services/tysescrutinymicrodivipol/api/testigos/1')).toBe(true);
    });

    it('should handle not found errors', async () => {
      axiosDeleteStub.rejects(new Error('Testigo not found'));

      await expect(divipolService.deleteTestigo(999)).rejects.toThrow('Testigo not found');
    });
  });

  describe('searchTestigos', () => {
    it('should call correct API endpoint with search params', async () => {
      const mockData = {
        content: [
          {
            id: 1,
            tipoDocumento: 'CC',
            numeroDocumento: '12345678',
            nombres: 'Juan',
            apellidos: 'Pérez',
            activo: true,
            puestosAsignados: 0,
          },
        ],
        page: 0,
        size: 10,
        totalElements: 1,
        totalPages: 1,
      };
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.searchTestigos('Juan');

      expect(axiosGetStub.calledOnce).toBe(true);
      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/testigos/search', {
          params: { q: 'Juan', page: 0, size: 10 },
        }),
      ).toBe(true);
      expect(result).toEqual(mockData);
    });

    it('should call with custom pagination', async () => {
      const mockData = { content: [], page: 1, size: 5, totalElements: 0, totalPages: 0 };
      axiosGetStub.resolves({ data: mockData });

      const result = await divipolService.searchTestigos('test', 1, 5);

      expect(
        axiosGetStub.calledWith('/services/tysescrutinymicrodivipol/api/testigos/search', {
          params: { q: 'test', page: 1, size: 5 },
        }),
      ).toBe(true);
      expect(result).toEqual(mockData);
    });

    it('should handle search errors', async () => {
      axiosGetStub.rejects(new Error('Search failed'));

      await expect(divipolService.searchTestigos('test')).rejects.toThrow('Search failed');
    });
  });
});
