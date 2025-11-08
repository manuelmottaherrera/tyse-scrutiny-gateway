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

// Base URL del microservicio divipol (a través del gateway)
const API_BASE_URL = '/services/tysescrutinymicrodivipol/api/divipol';

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
}

export default new DivipolService();
