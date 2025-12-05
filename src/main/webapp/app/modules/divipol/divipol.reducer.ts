import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import divipolService, {
  DivipolDepartamento,
  DivipolMunicipio,
  DivipolZona,
  DivipolPuesto,
  DivipolStats,
  DivipolSearchResult,
  SearchMode,
  SearchParams,
} from 'app/shared/services/divipol.service';

// Estado inicial
export interface SearchPagination {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface DivipolState {
  departamentos: DivipolDepartamento[];
  municipios: DivipolMunicipio[];
  zonas: DivipolZona[];
  puestos: DivipolPuesto[];
  stats: DivipolStats | null;
  filters: {
    departamento: number | null;
    municipio: number | null;
    zona: number | null;
    puesto: string | null;
  };
  loading: boolean;
  error: string | null;
  // Estado de búsqueda
  search: {
    mode: SearchMode;
    term: string;
    active: boolean;
    results: DivipolSearchResult[];
    suggestions: DivipolSearchResult[];
    loading: boolean;
    suggestionsLoading: boolean;
    pagination: SearchPagination;
  };
}

const initialState: DivipolState = {
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
  // Estado inicial de búsqueda
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
};

// Acciones asíncronas
export const fetchDepartamentos = createAsyncThunk('divipol/fetchDepartamentos', async () => {
  return await divipolService.getDepartamentos();
});

export const fetchMunicipios = createAsyncThunk('divipol/fetchMunicipios', async (codDepto: number) => {
  return await divipolService.getMunicipios(codDepto);
});

export const fetchZonas = createAsyncThunk('divipol/fetchZonas', async ({ codDepto, codMpio }: { codDepto: number; codMpio: number }) => {
  return await divipolService.getZonas(codDepto, codMpio);
});

export const fetchPuestos = createAsyncThunk(
  'divipol/fetchPuestos',
  async ({ codDepto, codMpio, codZona }: { codDepto: number; codMpio: number; codZona: number }) => {
    return await divipolService.getPuestos(codDepto, codMpio, codZona);
  },
);

export const fetchGeneralStats = createAsyncThunk('divipol/fetchGeneralStats', async () => {
  return await divipolService.getGeneralStats();
});

export const fetchStatsByDepartamento = createAsyncThunk('divipol/fetchStatsByDepartamento', async (codDepto: number) => {
  return await divipolService.getStatsByDepartamento(codDepto);
});

export const fetchStatsByMunicipio = createAsyncThunk(
  'divipol/fetchStatsByMunicipio',
  async ({ codDepto, codMpio }: { codDepto: number; codMpio: number }) => {
    return await divipolService.getStatsByMunicipio(codDepto, codMpio);
  },
);

export const fetchStatsByZona = createAsyncThunk(
  'divipol/fetchStatsByZona',
  async ({ codDepto, codMpio, codZona }: { codDepto: number; codMpio: number; codZona: number }) => {
    return await divipolService.getStatsByZona(codDepto, codMpio, codZona);
  },
);

// Acciones de búsqueda
export const searchDivipol = createAsyncThunk('divipol/searchDivipol', async (params: SearchParams) => {
  return await divipolService.search(params);
});

export const fetchSuggestions = createAsyncThunk('divipol/fetchSuggestions', async ({ q, mode }: { q: string; mode: SearchMode }) => {
  return await divipolService.getSuggestions(q, mode);
});

// Slice
export const DivipolSlice = createSlice({
  name: 'divipol',
  initialState,
  reducers: {
    setFilterDepartamento(state, action) {
      state.filters.departamento = action.payload;
      // Limpiar filtros dependientes
      state.filters.municipio = null;
      state.filters.zona = null;
      state.filters.puesto = null;
      state.municipios = [];
      state.zonas = [];
      state.puestos = [];
    },
    setFilterMunicipio(state, action) {
      state.filters.municipio = action.payload;
      // Limpiar filtros dependientes
      state.filters.zona = null;
      state.filters.puesto = null;
      state.zonas = [];
      state.puestos = [];
    },
    setFilterZona(state, action) {
      state.filters.zona = action.payload;
      // Limpiar filtros dependientes
      state.filters.puesto = null;
      state.puestos = [];
    },
    setFilterPuesto(state, action) {
      state.filters.puesto = action.payload;
    },
    resetFilters(state) {
      state.filters = {
        departamento: null,
        municipio: null,
        zona: null,
        puesto: null,
      };
      state.municipios = [];
      state.zonas = [];
      state.puestos = [];
    },
    clearError(state) {
      state.error = null;
    },
    // Acciones de búsqueda síncronas
    setSearchMode(state, action: PayloadAction<SearchMode>) {
      state.search.mode = action.payload;
      state.search.term = '';
      state.search.suggestions = [];
    },
    setSearchTerm(state, action: PayloadAction<string>) {
      state.search.term = action.payload;
    },
    clearSearch(state) {
      state.search.active = false;
      state.search.term = '';
      state.search.results = [];
      state.search.suggestions = [];
      state.search.pagination = {
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      };
    },
    clearSuggestions(state) {
      state.search.suggestions = [];
    },
  },
  extraReducers(builder) {
    // Fetch Departamentos
    builder
      .addCase(fetchDepartamentos.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchDepartamentos.fulfilled, (state, action) => {
        state.loading = false;
        state.departamentos = action.payload;
      })
      .addCase(fetchDepartamentos.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Error al cargar departamentos';
      });

    // Fetch Municipios
    builder
      .addCase(fetchMunicipios.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchMunicipios.fulfilled, (state, action) => {
        state.loading = false;
        state.municipios = action.payload;
      })
      .addCase(fetchMunicipios.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Error al cargar municipios';
      });

    // Fetch Zonas
    builder
      .addCase(fetchZonas.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchZonas.fulfilled, (state, action) => {
        state.loading = false;
        state.zonas = action.payload;
      })
      .addCase(fetchZonas.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Error al cargar zonas';
      });

    // Fetch Puestos
    builder
      .addCase(fetchPuestos.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchPuestos.fulfilled, (state, action) => {
        state.loading = false;
        state.puestos = action.payload;
      })
      .addCase(fetchPuestos.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Error al cargar puestos';
      });

    // Fetch General Stats
    builder
      .addCase(fetchGeneralStats.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchGeneralStats.fulfilled, (state, action) => {
        state.loading = false;
        state.stats = action.payload;
      })
      .addCase(fetchGeneralStats.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Error al cargar estadísticas';
      });

    // Fetch Stats By Departamento
    builder
      .addCase(fetchStatsByDepartamento.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchStatsByDepartamento.fulfilled, (state, action) => {
        state.loading = false;
        state.stats = action.payload;
      })
      .addCase(fetchStatsByDepartamento.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Error al cargar estadísticas';
      });

    // Fetch Stats By Municipio
    builder
      .addCase(fetchStatsByMunicipio.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchStatsByMunicipio.fulfilled, (state, action) => {
        state.loading = false;
        state.stats = action.payload;
      })
      .addCase(fetchStatsByMunicipio.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Error al cargar estadísticas';
      });

    // Fetch Stats By Zona
    builder
      .addCase(fetchStatsByZona.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchStatsByZona.fulfilled, (state, action) => {
        state.loading = false;
        state.stats = action.payload;
      })
      .addCase(fetchStatsByZona.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Error al cargar estadísticas';
      });

    // Search Divipol
    builder
      .addCase(searchDivipol.pending, state => {
        state.search.loading = true;
        state.error = null;
      })
      .addCase(searchDivipol.fulfilled, (state, action) => {
        state.search.loading = false;
        state.search.active = true;
        state.search.results = action.payload.content;
        state.search.pagination = {
          page: action.payload.page,
          size: action.payload.size,
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages,
        };
        // Limpiar filtros al buscar
        state.filters = {
          departamento: null,
          municipio: null,
          zona: null,
          puesto: null,
        };
        state.municipios = [];
        state.zonas = [];
        state.puestos = [];
      })
      .addCase(searchDivipol.rejected, (state, action) => {
        state.search.loading = false;
        state.error = action.error.message || 'Error al buscar';
      });

    // Fetch Suggestions
    builder
      .addCase(fetchSuggestions.pending, state => {
        state.search.suggestionsLoading = true;
      })
      .addCase(fetchSuggestions.fulfilled, (state, action) => {
        state.search.suggestionsLoading = false;
        state.search.suggestions = action.payload;
      })
      .addCase(fetchSuggestions.rejected, state => {
        state.search.suggestionsLoading = false;
        state.search.suggestions = [];
      });
  },
});

export const {
  setFilterDepartamento,
  setFilterMunicipio,
  setFilterZona,
  setFilterPuesto,
  resetFilters,
  clearError,
  setSearchMode,
  setSearchTerm,
  clearSearch,
  clearSuggestions,
} = DivipolSlice.actions;

export default DivipolSlice.reducer;
