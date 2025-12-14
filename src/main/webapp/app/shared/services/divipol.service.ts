import axios from 'axios';

// Tipos basados en los DTOs del backend
export interface DivipolDepartamento {
  coddepto: number;
  nomdepto: string;
  mujeres: number;
  hombres: number;
  totalPotencial: number;
  mesas: number;
}

export interface DivipolMunicipio {
  coddepto: number;
  codmipio: number;
  nomdepto: string;
  nommipio: string;
  potencialFemenino: number;
  potencialMasculino: number;
  potencialTotal: number;
  mesas: number;
}

export interface DivipolZona {
  coddepto: number;
  codmipio: number;
  codzona: number;
  nomdepto: string;
  nommipio: string;
  potencialFemenino: number;
  potencialMasculino: number;
  potencialTotal: number;
  mesas: number;
}

export interface DivipolPuesto {
  iddivipol: number;
  coddepto: number;
  codmipio: number;
  codzona: number;
  codpuesto: string;
  nomdepto: string;
  nommipio: string;
  nompuesto: string;
  potencialFemenino: number;
  potencialMasculino: number;
  potencialTotal: number;
  mesas: number;
}

export interface DivipolStats {
  totalDepartamentos: number;
  totalMunicipios: number;
  totalZonas: number;
  totalPuestos: number;
  totalMesas: number;
  potencialFemenino: number;
  potencialMasculino: number;
  potencialTotal: number;
}

// Tipos para búsqueda
export type SearchMode = 'name' | 'code';

export type DivipolTipo = 'PAIS' | 'DEPTO' | 'MPIO' | 'ZONA' | 'PUESTO';

export interface DivipolSearchResult {
  codigoDivipol: string;
  tipo: DivipolTipo;
  coddepto: number;
  codmipio: number;
  codzona: number;
  codpuesto: string;
  nomdepto: string;
  nommipio: string;
  nompuesto: string;
  potencialTotal: number;
  mesas: number;
}

export interface DivipolSearchPage {
  content: DivipolSearchResult[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// =====================================================
// Tipos para Detalle de Puesto
// =====================================================

export interface PuestoDetalle {
  iddivipol: number;
  coddepto: number;
  codmipio: number;
  codzona: number;
  codpuesto: string;
  nomdepto: string;
  nommipio: string;
  nompuesto: string;
  direccion?: string;
  latitud?: number;
  longitud?: number;
  jal?: number;
  nomjal?: string;
  indicador?: number;
  expandida?: number;
  nummesas: number;
  potfemenino: number;
  potmasculino: number;
  pottotal: number;
  totalJurados: number;
  totalTestigos: number;
}

// =====================================================
// Tipos para Jurados
// =====================================================

export interface Jurado {
  id: number;
  tipoDocumento: string;
  numeroDocumento: string;
  nombres: string;
  apellidos: string;
}

// =====================================================
// Tipos para Testigos Electorales
// =====================================================

export interface Testigo {
  id: number;
  tipoDocumento: string;
  numeroDocumento: string;
  nombres: string;
  apellidos: string;
  telefono?: string;
  email?: string;
  activo: boolean;
  puestosAsignados: number;
}

export interface TestigoCreate {
  tipoDocumento: string;
  numeroDocumento: string;
  nombres: string;
  apellidos: string;
  telefono?: string;
  email?: string;
}

export interface TestigoUpdate {
  nombres?: string;
  apellidos?: string;
  telefono?: string;
  email?: string;
}

export interface TestigoAsignado {
  testigoId: number;
  tipoDocumento: string;
  numeroDocumento: string;
  nombreCompleto: string;
  telefono?: string;
  assignedDate: string;
  assignedBy: string;
}

export interface TestigoPage {
  content: Testigo[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface SearchParams {
  q: string;
  mode?: SearchMode;
  page?: number;
  size?: number;
}

// Base URL del microservicio divipol (a través del gateway)
const API_BASE_URL = '/services/tysescrutinymicrodivipol/api/divipol';
const TESTIGOS_API_URL = '/services/tysescrutinymicrodivipol/api/testigos';

/**
 * Servicio para interactuar con el microservicio DIVIPOL
 */
class DivipolService {
  /**
   * Obtiene todos los departamentos
   */
  async getDepartamentos(): Promise<DivipolDepartamento[]> {
    const response = await axios.get<DivipolDepartamento[]>(`${API_BASE_URL}/departamentos`);
    return response.data;
  }

  /**
   * Obtiene municipios por departamento
   */
  async getMunicipios(codDepto: number): Promise<DivipolMunicipio[]> {
    const response = await axios.get<DivipolMunicipio[]>(`${API_BASE_URL}/municipios`, {
      params: { codDepto },
    });
    return response.data;
  }

  /**
   * Obtiene zonas por municipio
   */
  async getZonas(codDepto: number, codMpio: number): Promise<DivipolZona[]> {
    const response = await axios.get<DivipolZona[]>(`${API_BASE_URL}/zonas`, {
      params: { codDepto, codMpio },
    });
    return response.data;
  }

  /**
   * Obtiene puestos por zona
   */
  async getPuestos(codDepto: number, codMpio: number, codZona: number): Promise<DivipolPuesto[]> {
    const response = await axios.get<DivipolPuesto[]>(`${API_BASE_URL}/puestos`, {
      params: { codDepto, codMpio, codZona },
    });
    return response.data;
  }

  /**
   * Obtiene estadísticas generales
   */
  async getGeneralStats(): Promise<DivipolStats> {
    const response = await axios.get<DivipolStats>(`${API_BASE_URL}/stats`);
    return response.data;
  }

  /**
   * Obtiene estadísticas de un departamento
   */
  async getStatsByDepartamento(codDepto: number): Promise<DivipolStats> {
    const response = await axios.get<DivipolStats>(`${API_BASE_URL}/stats/departamento/${codDepto}`);
    return response.data;
  }

  /**
   * Obtiene estadísticas de un municipio
   */
  async getStatsByMunicipio(codDepto: number, codMpio: number): Promise<DivipolStats> {
    const response = await axios.get<DivipolStats>(`${API_BASE_URL}/stats/municipio/${codDepto}/${codMpio}`);
    return response.data;
  }

  /**
   * Obtiene estadísticas de una zona
   */
  async getStatsByZona(codDepto: number, codMpio: number, codZona: number): Promise<DivipolStats> {
    const response = await axios.get<DivipolStats>(`${API_BASE_URL}/stats/zona/${codDepto}/${codMpio}/${codZona}`);
    return response.data;
  }

  // =====================================================
  // Métodos de Búsqueda
  // =====================================================

  /**
   * Busca registros de divipol por nombre o código
   */
  async search(params: SearchParams): Promise<DivipolSearchPage> {
    const response = await axios.get<DivipolSearchPage>(`${API_BASE_URL}/search`, {
      params: {
        q: params.q,
        mode: params.mode || 'name',
        page: params.page || 0,
        size: params.size || 20,
      },
    });
    return response.data;
  }

  /**
   * Obtiene sugerencias de autocompletado
   */
  async getSuggestions(q: string, mode: SearchMode = 'name'): Promise<DivipolSearchResult[]> {
    const response = await axios.get<DivipolSearchResult[]>(`${API_BASE_URL}/search/suggestions`, {
      params: { q, mode },
    });
    return response.data;
  }

  // =====================================================
  // Métodos de Exportación
  // =====================================================

  /**
   * Exporta datos del modo filtros a CSV o PDF
   */
  async exportFilters(format: ExportFormat, params: FilterExportParams): Promise<Blob> {
    const endpoint = `${API_BASE_URL}/export/filters/${format}`;
    const response = await axios.get(endpoint, {
      params: {
        codDepto: params.codDepto,
        codMpio: params.codMpio,
        codZona: params.codZona,
      },
      responseType: 'blob',
    });
    return response.data;
  }

  /**
   * Exporta resultados del modo búsqueda a CSV o PDF
   */
  async exportSearch(format: ExportFormat, params: SearchExportParams): Promise<Blob> {
    const endpoint = `${API_BASE_URL}/export/search/${format}`;
    const response = await axios.get(endpoint, {
      params: {
        q: params.q,
        mode: params.mode,
        page: params.page,
        size: params.size,
        exportAll: params.exportAll,
      },
      responseType: 'blob',
    });
    return response.data;
  }

  // =====================================================
  // Métodos de Detalle de Puesto
  // =====================================================

  /**
   * Obtiene el detalle completo de un puesto
   */
  async getPuestoDetalle(puestoId: number): Promise<PuestoDetalle> {
    const response = await axios.get<PuestoDetalle>(`${API_BASE_URL}/puestos/${puestoId}/detalle`);
    return response.data;
  }

  /**
   * Obtiene los jurados asignados a un puesto (solo lectura)
   */
  async getJuradosByPuesto(puestoId: number): Promise<Jurado[]> {
    const response = await axios.get<Jurado[]>(`${API_BASE_URL}/puestos/${puestoId}/jurados`);
    return response.data;
  }

  /**
   * Obtiene los testigos asignados a un puesto
   */
  async getTestigosByPuesto(puestoId: number): Promise<TestigoAsignado[]> {
    const response = await axios.get<TestigoAsignado[]>(`${API_BASE_URL}/puestos/${puestoId}/testigos`);
    return response.data;
  }

  /**
   * Asigna un testigo a un puesto
   */
  async asignarTestigoAPuesto(puestoId: number, testigoId: number): Promise<TestigoAsignado> {
    const response = await axios.post<TestigoAsignado>(`${API_BASE_URL}/puestos/${puestoId}/testigos/${testigoId}`);
    return response.data;
  }

  /**
   * Desasigna un testigo de un puesto
   */
  async desasignarTestigoDePuesto(puestoId: number, testigoId: number): Promise<void> {
    await axios.delete(`${API_BASE_URL}/puestos/${puestoId}/testigos/${testigoId}`);
  }

  // =====================================================
  // Métodos de Testigos Electorales
  // =====================================================

  /**
   * Obtiene todos los testigos paginados
   */
  async getAllTestigos(page = 0, size = 20): Promise<TestigoPage> {
    const response = await axios.get<TestigoPage>(`${TESTIGOS_API_URL}`, {
      params: { page, size },
    });
    return response.data;
  }

  /**
   * Obtiene un testigo por ID
   */
  async getTestigoById(id: number): Promise<Testigo> {
    const response = await axios.get<Testigo>(`${TESTIGOS_API_URL}/${id}`);
    return response.data;
  }

  /**
   * Crea un nuevo testigo
   */
  async createTestigo(testigo: TestigoCreate): Promise<Testigo> {
    const response = await axios.post<Testigo>(TESTIGOS_API_URL, testigo);
    return response.data;
  }

  /**
   * Actualiza un testigo existente
   */
  async updateTestigo(id: number, testigo: TestigoUpdate): Promise<Testigo> {
    const response = await axios.put<Testigo>(`${TESTIGOS_API_URL}/${id}`, testigo);
    return response.data;
  }

  /**
   * Elimina un testigo (soft delete)
   */
  async deleteTestigo(id: number): Promise<void> {
    await axios.delete(`${TESTIGOS_API_URL}/${id}`);
  }

  /**
   * Busca testigos por nombre o documento
   */
  async searchTestigos(query: string, page = 0, size = 10): Promise<TestigoPage> {
    const response = await axios.get<TestigoPage>(`${TESTIGOS_API_URL}/search`, {
      params: { q: query, page, size },
    });
    return response.data;
  }
}

// =====================================================
// Tipos de Exportación
// =====================================================

export type ExportFormat = 'csv' | 'pdf';

export interface FilterExportParams {
  codDepto?: number;
  codMpio?: number;
  codZona?: number;
}

export interface SearchExportParams {
  q: string;
  mode: SearchMode;
  page?: number;
  size?: number;
  exportAll: boolean;
}

export default new DivipolService();
