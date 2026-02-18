import axios from 'axios';

// Tipos para Anomalías
export type AnomalyType = 'PRECOUNT_DIFFERENCE' | 'VOTES_EXCEED_VOTERS' | 'SUM_MISMATCH';
export type AnomalySeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type AnomalyStatus = 'NEW' | 'REVIEWED' | 'CLAIMED' | 'DISMISSED';

export interface Anomaly {
  id: number;
  electionProcessId: number;
  type: AnomalyType;
  severity: AnomalySeverity;
  status: AnomalyStatus;
  divipolKey: string;
  depCode?: string;
  munCode?: string;
  votingTable?: string;
  partyNumber?: string;
  candidateId?: string;
  candidateFirstName?: string;
  candidateLastName?: string;
  precountVotes?: number;
  scrutinyVotes?: number;
  difference?: number;
  scrutinyDayId?: number;
  description?: string;
  reviewedBy?: string;
  reviewDate?: string;
  notes?: string;
  detectedAt: string;
}

export interface AnomalyPage {
  content: Anomaly[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface AnomalyFilters {
  electionProcessId: number;
  status?: AnomalyStatus;
  type?: AnomalyType;
  severity?: AnomalySeverity;
  page?: number;
  size?: number;
}

export interface UpdateStatusRequest {
  status: AnomalyStatus;
  reviewedBy?: string;
  notes?: string;
}

export interface AnomalyStats {
  total: number;
  new: number;
  highSeverity: number;
  claimed: number;
}

// URL base para micro-scrutiny via gateway proxy
const API_BASE_URL = '/services/tysescrutinymicroscrutiny/api/anomalies';

/**
 * Servicio para operaciones relacionadas con anomalías electorales
 */
class AnomalyService {
  /**
   * Obtener lista de anomalías con filtros y paginación
   */
  async getAnomalies(filters: AnomalyFilters): Promise<AnomalyPage> {
    const response = await axios.get<Anomaly[]>(API_BASE_URL, {
      params: {
        electionProcessId: filters.electionProcessId,
        status: filters.status,
        type: filters.type,
        severity: filters.severity,
        page: filters.page ?? 0,
        size: filters.size ?? 20,
      },
    });

    // Extraer total del header X-Total-Count
    const totalElements = parseInt(response.headers['x-total-count'] || '0', 10);

    return {
      content: response.data,
      totalElements,
      totalPages: Math.ceil(totalElements / (filters.size ?? 20)),
      size: filters.size ?? 20,
      number: filters.page ?? 0,
    };
  }

  /**
   * Obtener anomalías nuevas (sin revisar)
   */
  async getNewAnomalies(electionProcessId: number, page = 0, size = 20): Promise<AnomalyPage> {
    const response = await axios.get<Anomaly[]>(`${API_BASE_URL}/new`, {
      params: { electionProcessId, page, size },
    });

    const totalElements = parseInt(response.headers['x-total-count'] || '0', 10);

    return {
      content: response.data,
      totalElements,
      totalPages: Math.ceil(totalElements / size),
      size,
      number: page,
    };
  }

  /**
   * Obtener anomalías de alta severidad (HIGH y CRITICAL)
   */
  async getHighSeverityAnomalies(electionProcessId: number, page = 0, size = 20): Promise<AnomalyPage> {
    const response = await axios.get<Anomaly[]>(`${API_BASE_URL}/high-severity`, {
      params: { electionProcessId, page, size },
    });

    const totalElements = parseInt(response.headers['x-total-count'] || '0', 10);

    return {
      content: response.data,
      totalElements,
      totalPages: Math.ceil(totalElements / size),
      size,
      number: page,
    };
  }

  /**
   * Obtener una anomalía por ID
   */
  async getAnomaly(id: number): Promise<Anomaly> {
    const response = await axios.get<Anomaly>(`${API_BASE_URL}/${id}`);
    return response.data;
  }

  /**
   * Actualizar estado de una anomalía
   */
  async updateStatus(id: number, request: UpdateStatusRequest): Promise<Anomaly> {
    const response = await axios.patch<Anomaly>(`${API_BASE_URL}/${id}/status`, request);
    return response.data;
  }

  /**
   * Eliminar una anomalía
   */
  async deleteAnomaly(id: number): Promise<void> {
    await axios.delete(`${API_BASE_URL}/${id}`);
  }

  /**
   * Obtener estadísticas de anomalías
   * Hace múltiples llamadas para construir las estadísticas
   */
  async getStats(electionProcessId: number): Promise<AnomalyStats> {
    const [allResponse, newResponse, highResponse, claimedResponse] = await Promise.all([
      axios.get<Anomaly[]>(API_BASE_URL, { params: { electionProcessId, size: 1 } }),
      axios.get<Anomaly[]>(`${API_BASE_URL}/new`, { params: { electionProcessId, size: 1 } }),
      axios.get<Anomaly[]>(`${API_BASE_URL}/high-severity`, { params: { electionProcessId, size: 1 } }),
      axios.get<Anomaly[]>(API_BASE_URL, { params: { electionProcessId, status: 'CLAIMED', size: 1 } }),
    ]);

    return {
      total: parseInt(allResponse.headers['x-total-count'] || '0', 10),
      new: parseInt(newResponse.headers['x-total-count'] || '0', 10),
      highSeverity: parseInt(highResponse.headers['x-total-count'] || '0', 10),
      claimed: parseInt(claimedResponse.headers['x-total-count'] || '0', 10),
    };
  }
}

export default new AnomalyService();
