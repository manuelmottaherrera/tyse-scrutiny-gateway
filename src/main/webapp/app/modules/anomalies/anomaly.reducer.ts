import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import anomalyService, {
  Anomaly,
  AnomalyFilters,
  AnomalySeverity,
  AnomalyStats,
  AnomalyStatus,
  AnomalyType,
  UpdateStatusRequest,
} from 'app/shared/services/anomaly.service';

// Estado del módulo
export interface AnomalyState {
  anomalies: Anomaly[];
  selectedAnomaly: Anomaly | null;
  stats: AnomalyStats | null;
  filters: {
    electionProcessId: number;
    status: AnomalyStatus | null;
    type: AnomalyType | null;
    severity: AnomalySeverity | null;
  };
  pagination: {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
  loading: boolean;
  loadingDetail: boolean;
  loadingStats: boolean;
  error: string | null;
}

const initialState: AnomalyState = {
  anomalies: [],
  selectedAnomaly: null,
  stats: null,
  filters: {
    electionProcessId: 1, // Default al proceso electoral 1
    status: null,
    type: null,
    severity: null,
  },
  pagination: {
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  },
  loading: false,
  loadingDetail: false,
  loadingStats: false,
  error: null,
};

// Async Thunks
export const fetchAnomalies = createAsyncThunk('anomaly/fetchAnomalies', async (_, { getState }) => {
  const state = getState() as { anomaly: AnomalyState };
  const { filters, pagination } = state.anomaly;

  const params: AnomalyFilters = {
    electionProcessId: filters.electionProcessId,
    status: filters.status ?? undefined,
    type: filters.type ?? undefined,
    severity: filters.severity ?? undefined,
    page: pagination.page,
    size: pagination.size,
  };

  return await anomalyService.getAnomalies(params);
});

export const fetchNewAnomalies = createAsyncThunk(
  'anomaly/fetchNewAnomalies',
  async (params: { electionProcessId: number; page?: number; size?: number }) => {
    return await anomalyService.getNewAnomalies(params.electionProcessId, params.page, params.size);
  },
);

export const fetchHighSeverityAnomalies = createAsyncThunk(
  'anomaly/fetchHighSeverityAnomalies',
  async (params: { electionProcessId: number; page?: number; size?: number }) => {
    return await anomalyService.getHighSeverityAnomalies(params.electionProcessId, params.page, params.size);
  },
);

export const fetchAnomalyById = createAsyncThunk('anomaly/fetchAnomalyById', async (id: number) => {
  return await anomalyService.getAnomaly(id);
});

export const fetchAnomalyStats = createAsyncThunk('anomaly/fetchAnomalyStats', async (electionProcessId: number) => {
  return await anomalyService.getStats(electionProcessId);
});

export const updateAnomalyStatus = createAsyncThunk(
  'anomaly/updateAnomalyStatus',
  async (params: { id: number; request: UpdateStatusRequest }) => {
    return await anomalyService.updateStatus(params.id, params.request);
  },
);

export const deleteAnomaly = createAsyncThunk('anomaly/deleteAnomaly', async (id: number) => {
  await anomalyService.deleteAnomaly(id);
  return id;
});

// Slice
export const anomalySlice = createSlice({
  name: 'anomaly',
  initialState,
  reducers: {
    setFilterStatus(state, action: PayloadAction<AnomalyStatus | null>) {
      state.filters.status = action.payload;
      state.pagination.page = 0; // Reset page al cambiar filtro
    },
    setFilterType(state, action: PayloadAction<AnomalyType | null>) {
      state.filters.type = action.payload;
      state.pagination.page = 0;
    },
    setFilterSeverity(state, action: PayloadAction<AnomalySeverity | null>) {
      state.filters.severity = action.payload;
      state.pagination.page = 0;
    },
    setElectionProcessId(state, action: PayloadAction<number>) {
      state.filters.electionProcessId = action.payload;
      state.pagination.page = 0;
    },
    setPage(state, action: PayloadAction<number>) {
      state.pagination.page = action.payload;
    },
    setPageSize(state, action: PayloadAction<number>) {
      state.pagination.size = action.payload;
      state.pagination.page = 0;
    },
    clearFilters(state) {
      state.filters.status = null;
      state.filters.type = null;
      state.filters.severity = null;
      state.pagination.page = 0;
    },
    clearSelectedAnomaly(state) {
      state.selectedAnomaly = null;
    },
    clearError(state) {
      state.error = null;
    },
  },
  extraReducers(builder) {
    builder
      // fetchAnomalies
      .addCase(fetchAnomalies.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAnomalies.fulfilled, (state, action) => {
        state.loading = false;
        state.anomalies = action.payload.content;
        state.pagination.totalElements = action.payload.totalElements;
        state.pagination.totalPages = action.payload.totalPages;
      })
      .addCase(fetchAnomalies.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message ?? 'Error al cargar anomalías';
      })

      // fetchNewAnomalies
      .addCase(fetchNewAnomalies.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchNewAnomalies.fulfilled, (state, action) => {
        state.loading = false;
        state.anomalies = action.payload.content;
        state.pagination.totalElements = action.payload.totalElements;
        state.pagination.totalPages = action.payload.totalPages;
      })
      .addCase(fetchNewAnomalies.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message ?? 'Error al cargar anomalías nuevas';
      })

      // fetchHighSeverityAnomalies
      .addCase(fetchHighSeverityAnomalies.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchHighSeverityAnomalies.fulfilled, (state, action) => {
        state.loading = false;
        state.anomalies = action.payload.content;
        state.pagination.totalElements = action.payload.totalElements;
        state.pagination.totalPages = action.payload.totalPages;
      })
      .addCase(fetchHighSeverityAnomalies.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message ?? 'Error al cargar anomalías de alta severidad';
      })

      // fetchAnomalyById
      .addCase(fetchAnomalyById.pending, state => {
        state.loadingDetail = true;
        state.error = null;
      })
      .addCase(fetchAnomalyById.fulfilled, (state, action) => {
        state.loadingDetail = false;
        state.selectedAnomaly = action.payload;
      })
      .addCase(fetchAnomalyById.rejected, (state, action) => {
        state.loadingDetail = false;
        state.error = action.error.message ?? 'Error al cargar anomalía';
      })

      // fetchAnomalyStats
      .addCase(fetchAnomalyStats.pending, state => {
        state.loadingStats = true;
      })
      .addCase(fetchAnomalyStats.fulfilled, (state, action) => {
        state.loadingStats = false;
        state.stats = action.payload;
      })
      .addCase(fetchAnomalyStats.rejected, (state, action) => {
        state.loadingStats = false;
        state.error = action.error.message ?? 'Error al cargar estadísticas';
      })

      // updateAnomalyStatus
      .addCase(updateAnomalyStatus.fulfilled, (state, action) => {
        const updated = action.payload;
        // Actualizar en lista
        const index = state.anomalies.findIndex(a => a.id === updated.id);
        if (index !== -1) {
          state.anomalies[index] = updated;
        }
        // Actualizar seleccionado si corresponde
        if (state.selectedAnomaly?.id === updated.id) {
          state.selectedAnomaly = updated;
        }
      })

      // deleteAnomaly
      .addCase(deleteAnomaly.fulfilled, (state, action) => {
        const deletedId = action.payload;
        state.anomalies = state.anomalies.filter(a => a.id !== deletedId);
        if (state.selectedAnomaly?.id === deletedId) {
          state.selectedAnomaly = null;
        }
        if (state.pagination.totalElements > 0) {
          state.pagination.totalElements -= 1;
        }
      });
  },
});

export const {
  setFilterStatus,
  setFilterType,
  setFilterSeverity,
  setElectionProcessId,
  setPage,
  setPageSize,
  clearFilters,
  clearSelectedAnomaly,
  clearError,
} = anomalySlice.actions;

export default anomalySlice.reducer;
