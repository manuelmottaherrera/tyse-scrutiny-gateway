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
  iddivipol?: number; // Solo presente para tipo PUESTO
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

// =====================================================
// Tipos para Mesa de Votación
// =====================================================

export interface MesaVotacion {
  id: number;
  puestoId: number;
  numeroMesa: number;
  activo: boolean;
}

// =====================================================
// Tipos para Organización Política
// =====================================================

export type TipoOrganizacion = 'PARTIDO' | 'MOVIMIENTO' | 'COALICION' | 'GRUPO_SIGNIFICATIVO' | 'COMITE_VOTO_BLANCO';

export interface OrganizacionPolitica {
  id: number;
  nombre: string;
  sigla?: string;
  tipo: TipoOrganizacion;
  activo: boolean;
}

export interface OrganizacionPoliticaCreate {
  nombre: string;
  sigla?: string;
  tipo: TipoOrganizacion;
}

export interface OrganizacionPoliticaPage {
  content: OrganizacionPolitica[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// =====================================================
// Tipos para Comisión Escrutadora
// =====================================================

export type TipoComision = 'AUXILIAR' | 'MUNICIPAL' | 'DISTRITAL' | 'GENERAL';

export interface ComisionEscrutadora {
  id: number;
  tipo: TipoComision;
  nombre: string;
  ubicacion?: string;
  departamentoId?: number;
  municipioId?: number;
  fechaInicio?: string;
  activo: boolean;
}

export interface ComisionEscrutadoraCreate {
  tipo: TipoComision;
  nombre: string;
  ubicacion?: string;
  departamentoId?: number;
  municipioId?: number;
  fechaInicio?: string;
}

export interface ComisionEscrutadoraPage {
  content: ComisionEscrutadora[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// =====================================================
// Tipos para Testigo Mesa y Testigo Comisión
// =====================================================

export type TipoTestigo = 'PRINCIPAL' | 'REMANENTE';

export interface TestigoMesa {
  id: number;
  testigoId: number;
  testigoNombreCompleto: string;
  testigoNumeroDocumento: string;
  mesaId: number;
  mesaNumero: number;
  puestoId: number;
  organizacionId?: number;
  organizacionNombre?: string;
  tipoTestigo: TipoTestigo;
  assignedDate: string;
  assignedBy: string;
  activo: boolean;
}

export interface TestigoMesaAsignacion {
  testigoId: number;
  organizacionId?: number;
  tipoTestigo: TipoTestigo;
}

export interface TestigoComision {
  id: number;
  testigoId: number;
  testigoNombreCompleto: string;
  testigoNumeroDocumento: string;
  comisionId: number;
  comisionNombre: string;
  organizacionId?: number;
  organizacionNombre?: string;
  tipoTestigo: TipoTestigo;
  assignedDate: string;
  assignedBy: string;
  activo: boolean;
}

export interface TestigoComisionAsignacion {
  testigoId: number;
  organizacionId?: number;
  tipoTestigo: TipoTestigo;
}

// =====================================================
// Tipos para Credencial
// =====================================================

export type TipoCredencial = 'E15' | 'E16';
export type EstadoCredencial = 'PENDIENTE' | 'EMITIDA' | 'ENTREGADA' | 'ANULADA';

export interface Credencial {
  id: number;
  testigoId: number;
  testigoNombreCompleto: string;
  testigoNumeroDocumento: string;
  tipo: TipoCredencial;
  estado: EstadoCredencial;
  codigoVerificacion?: string;
  testigoMesaId?: number;
  testigoComisionId?: number;
  asignacionDescripcion?: string;
  fechaEmision?: string;
  fechaEntrega?: string;
  emitidoPor?: string;
  activo: boolean;
}

export interface CredencialCreate {
  testigoId: number;
  tipo: TipoCredencial;
  testigoMesaId?: number;
  testigoComisionId?: number;
}

export interface CredencialEstado {
  estado: EstadoCredencial;
}

export interface CredencialPage {
  content: Credencial[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// =====================================================
// Tipos para Reclamación
// =====================================================

export type TipoReclamacion =
  | 'IRREGULARIDAD_MESA'
  | 'EXCESO_VOTANTES'
  | 'ERROR_ARITMETICO'
  | 'ERROR_NOMBRES'
  | 'FIRMAS_INSUFICIENTES'
  | 'DISCREPANCIA_ACTAS'
  | 'OTRO';

export type EstadoReclamacion = 'PRESENTADA' | 'EN_REVISION' | 'ACEPTADA' | 'RECHAZADA';

export interface Reclamacion {
  id: number;
  testigoId: number;
  testigoNombreCompleto: string;
  testigoNumeroDocumento: string;
  tipoReclamacion: TipoReclamacion;
  descripcion: string;
  estado: EstadoReclamacion;
  mesaId?: number;
  mesaDescripcion?: string;
  comisionId?: number;
  comisionNombre?: string;
  fechaPresentacion: string;
  resolucion?: string;
  fechaResolucion?: string;
  resueltaPor?: string;
  activo: boolean;
}

export interface ReclamacionCreate {
  testigoId: number;
  tipoReclamacion: TipoReclamacion;
  descripcion: string;
  mesaId?: number;
  comisionId?: number;
}

export interface ReclamacionResolucion {
  estado: 'ACEPTADA' | 'RECHAZADA';
  resolucion: string;
}

export interface ReclamacionPage {
  content: Reclamacion[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// =====================================================
// Tipos para Configuración Electoral
// =====================================================

export interface ConfiguracionElectoral {
  id: number;
  clave: string;
  valor: string;
  descripcion?: string;
  activo: boolean;
}

export interface ConfiguracionInscripcion {
  inscripcionAbierta: boolean;
  fechaInicio?: string;
  fechaFin?: string;
  fechaElecciones?: string;
}

// Base URLs del microservicio divipol (a través del gateway)
const API_BASE_URL = '/services/tysescrutinymicrodivipol/api/divipol';
const TESTIGOS_API_URL = '/services/tysescrutinymicrodivipol/api/testigos';
const ORGANIZACIONES_API_URL = '/services/tysescrutinymicrodivipol/api/organizaciones';
const COMISIONES_API_URL = '/services/tysescrutinymicrodivipol/api/comisiones';
const CREDENCIALES_API_URL = '/services/tysescrutinymicrodivipol/api/credenciales';
const RECLAMACIONES_API_URL = '/services/tysescrutinymicrodivipol/api/reclamaciones';
const CONFIGURACION_API_URL = '/services/tysescrutinymicrodivipol/api/configuracion';

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

  // =====================================================
  // Métodos de Mesas de Votación
  // =====================================================

  /**
   * Obtiene las mesas de un puesto
   */
  async getMesasByPuesto(puestoId: number): Promise<MesaVotacion[]> {
    const response = await axios.get<MesaVotacion[]>(`${API_BASE_URL}/puestos/${puestoId}/mesas`);
    return response.data;
  }

  /**
   * Obtiene los testigos asignados a una mesa
   */
  async getTestigosByMesa(puestoId: number, mesaId: number): Promise<TestigoMesa[]> {
    const response = await axios.get<TestigoMesa[]>(`${API_BASE_URL}/puestos/${puestoId}/mesas/${mesaId}/testigos`);
    return response.data;
  }

  /**
   * Asigna un testigo a una mesa
   */
  async asignarTestigoAMesa(puestoId: number, mesaId: number, asignacion: TestigoMesaAsignacion): Promise<TestigoMesa> {
    const response = await axios.post<TestigoMesa>(`${API_BASE_URL}/puestos/${puestoId}/mesas/${mesaId}/testigos`, asignacion);
    return response.data;
  }

  /**
   * Desasigna un testigo de una mesa
   */
  async desasignarTestigoDeMesa(puestoId: number, mesaId: number, testigoId: number): Promise<void> {
    await axios.delete(`${API_BASE_URL}/puestos/${puestoId}/mesas/${mesaId}/testigos/${testigoId}`);
  }

  // =====================================================
  // Métodos de Organizaciones Políticas
  // =====================================================

  /**
   * Obtiene todas las organizaciones paginadas
   */
  async getAllOrganizaciones(page = 0, size = 20, tipo?: TipoOrganizacion): Promise<OrganizacionPoliticaPage> {
    const response = await axios.get<OrganizacionPoliticaPage>(ORGANIZACIONES_API_URL, {
      params: { page, size, tipo },
    });
    return response.data;
  }

  /**
   * Obtiene una organización por ID
   */
  async getOrganizacionById(id: number): Promise<OrganizacionPolitica> {
    const response = await axios.get<OrganizacionPolitica>(`${ORGANIZACIONES_API_URL}/${id}`);
    return response.data;
  }

  /**
   * Crea una nueva organización
   */
  async createOrganizacion(org: OrganizacionPoliticaCreate): Promise<OrganizacionPolitica> {
    const response = await axios.post<OrganizacionPolitica>(ORGANIZACIONES_API_URL, org);
    return response.data;
  }

  /**
   * Actualiza una organización
   */
  async updateOrganizacion(id: number, org: Partial<OrganizacionPoliticaCreate>): Promise<OrganizacionPolitica> {
    const response = await axios.put<OrganizacionPolitica>(`${ORGANIZACIONES_API_URL}/${id}`, org);
    return response.data;
  }

  /**
   * Elimina una organización (soft delete)
   */
  async deleteOrganizacion(id: number): Promise<void> {
    await axios.delete(`${ORGANIZACIONES_API_URL}/${id}`);
  }

  // =====================================================
  // Métodos de Comisiones Escrutadoras
  // =====================================================

  /**
   * Obtiene todas las comisiones paginadas
   */
  async getAllComisiones(page = 0, size = 20, tipo?: TipoComision): Promise<ComisionEscrutadoraPage> {
    const response = await axios.get<ComisionEscrutadoraPage>(COMISIONES_API_URL, {
      params: { page, size, tipo },
    });
    return response.data;
  }

  /**
   * Obtiene una comisión por ID
   */
  async getComisionById(id: number): Promise<ComisionEscrutadora> {
    const response = await axios.get<ComisionEscrutadora>(`${COMISIONES_API_URL}/${id}`);
    return response.data;
  }

  /**
   * Crea una nueva comisión
   */
  async createComision(comision: ComisionEscrutadoraCreate): Promise<ComisionEscrutadora> {
    const response = await axios.post<ComisionEscrutadora>(COMISIONES_API_URL, comision);
    return response.data;
  }

  /**
   * Actualiza una comisión
   */
  async updateComision(id: number, comision: Partial<ComisionEscrutadoraCreate>): Promise<ComisionEscrutadora> {
    const response = await axios.put<ComisionEscrutadora>(`${COMISIONES_API_URL}/${id}`, comision);
    return response.data;
  }

  /**
   * Elimina una comisión (soft delete)
   */
  async deleteComision(id: number): Promise<void> {
    await axios.delete(`${COMISIONES_API_URL}/${id}`);
  }

  /**
   * Obtiene los testigos asignados a una comisión
   */
  async getTestigosByComision(comisionId: number): Promise<TestigoComision[]> {
    const response = await axios.get<TestigoComision[]>(`${COMISIONES_API_URL}/${comisionId}/testigos`);
    return response.data;
  }

  /**
   * Asigna un testigo a una comisión
   */
  async asignarTestigoAComision(comisionId: number, asignacion: TestigoComisionAsignacion): Promise<TestigoComision> {
    const response = await axios.post<TestigoComision>(`${COMISIONES_API_URL}/${comisionId}/testigos`, asignacion);
    return response.data;
  }

  /**
   * Desasigna un testigo de una comisión
   */
  async desasignarTestigoDeComision(comisionId: number, testigoId: number): Promise<void> {
    await axios.delete(`${COMISIONES_API_URL}/${comisionId}/testigos/${testigoId}`);
  }

  // =====================================================
  // Métodos de Credenciales
  // =====================================================

  /**
   * Obtiene todas las credenciales paginadas
   */
  async getAllCredenciales(page = 0, size = 20, estado?: EstadoCredencial, tipo?: TipoCredencial): Promise<CredencialPage> {
    const response = await axios.get<CredencialPage>(CREDENCIALES_API_URL, {
      params: { page, size, estado, tipo },
    });
    return response.data;
  }

  /**
   * Obtiene una credencial por ID
   */
  async getCredencialById(id: number): Promise<Credencial> {
    const response = await axios.get<Credencial>(`${CREDENCIALES_API_URL}/${id}`);
    return response.data;
  }

  /**
   * Crea una nueva credencial
   */
  async createCredencial(credencial: CredencialCreate): Promise<Credencial> {
    const response = await axios.post<Credencial>(CREDENCIALES_API_URL, credencial);
    return response.data;
  }

  /**
   * Actualiza el estado de una credencial
   */
  async updateCredencialEstado(id: number, estado: CredencialEstado): Promise<Credencial> {
    const response = await axios.put<Credencial>(`${CREDENCIALES_API_URL}/${id}/estado`, estado);
    return response.data;
  }

  /**
   * Anula una credencial
   */
  async anularCredencial(id: number): Promise<void> {
    await axios.delete(`${CREDENCIALES_API_URL}/${id}`);
  }

  /**
   * Obtiene las credenciales de un testigo
   */
  async getCredencialesByTestigo(testigoId: number): Promise<Credencial[]> {
    const response = await axios.get<Credencial[]>(`${CREDENCIALES_API_URL}/testigo/${testigoId}`);
    return response.data;
  }

  /**
   * Verifica una credencial por código
   */
  async verificarCredencial(codigo: string): Promise<Credencial> {
    const response = await axios.get<Credencial>(`${CREDENCIALES_API_URL}/verificar/${codigo}`);
    return response.data;
  }

  // =====================================================
  // Métodos de Reclamaciones
  // =====================================================

  /**
   * Obtiene todas las reclamaciones paginadas
   */
  async getAllReclamaciones(page = 0, size = 20, estado?: EstadoReclamacion, tipo?: TipoReclamacion): Promise<ReclamacionPage> {
    const response = await axios.get<ReclamacionPage>(RECLAMACIONES_API_URL, {
      params: { page, size, estado, tipo },
    });
    return response.data;
  }

  /**
   * Obtiene una reclamación por ID
   */
  async getReclamacionById(id: number): Promise<Reclamacion> {
    const response = await axios.get<Reclamacion>(`${RECLAMACIONES_API_URL}/${id}`);
    return response.data;
  }

  /**
   * Crea una nueva reclamación
   */
  async createReclamacion(reclamacion: ReclamacionCreate): Promise<Reclamacion> {
    const response = await axios.post<Reclamacion>(RECLAMACIONES_API_URL, reclamacion);
    return response.data;
  }

  /**
   * Resuelve una reclamación
   */
  async resolverReclamacion(id: number, resolucion: ReclamacionResolucion): Promise<Reclamacion> {
    const response = await axios.put<Reclamacion>(`${RECLAMACIONES_API_URL}/${id}/resolver`, resolucion);
    return response.data;
  }

  /**
   * Elimina una reclamación (soft delete)
   */
  async deleteReclamacion(id: number): Promise<void> {
    await axios.delete(`${RECLAMACIONES_API_URL}/${id}`);
  }

  /**
   * Obtiene las reclamaciones de un testigo
   */
  async getReclamacionesByTestigo(testigoId: number): Promise<Reclamacion[]> {
    const response = await axios.get<Reclamacion[]>(`${RECLAMACIONES_API_URL}/testigo/${testigoId}`);
    return response.data;
  }

  /**
   * Obtiene las reclamaciones de una mesa
   */
  async getReclamacionesByMesa(mesaId: number): Promise<Reclamacion[]> {
    const response = await axios.get<Reclamacion[]>(`${RECLAMACIONES_API_URL}/mesa/${mesaId}`);
    return response.data;
  }

  /**
   * Obtiene las reclamaciones de una comisión
   */
  async getReclamacionesByComision(comisionId: number): Promise<Reclamacion[]> {
    const response = await axios.get<Reclamacion[]>(`${RECLAMACIONES_API_URL}/comision/${comisionId}`);
    return response.data;
  }

  // =====================================================
  // Métodos de Configuración Electoral
  // =====================================================

  /**
   * Obtiene toda la configuración electoral
   */
  async getAllConfiguracion(): Promise<ConfiguracionElectoral[]> {
    const response = await axios.get<ConfiguracionElectoral[]>(CONFIGURACION_API_URL);
    return response.data;
  }

  /**
   * Obtiene el estado de la inscripción de testigos
   */
  async getConfiguracionInscripcion(): Promise<ConfiguracionInscripcion> {
    const response = await axios.get<ConfiguracionInscripcion>(`${CONFIGURACION_API_URL}/inscripcion`);
    return response.data;
  }

  /**
   * Actualiza una configuración
   */
  async updateConfiguracion(clave: string, valor: string): Promise<ConfiguracionElectoral> {
    const response = await axios.put<ConfiguracionElectoral>(`${CONFIGURACION_API_URL}/${clave}`, { valor });
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
