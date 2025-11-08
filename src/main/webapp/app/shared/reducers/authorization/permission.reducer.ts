import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { IPermission } from '../../model/authorization/permission.model';
import * as permissionService from '../../services/permission.service';
import { serializeAxiosError } from '../reducer.utils';

interface PermissionState {
  loading: boolean;
  errorMessage: string | null;
  entities: IPermission[];
  entity: IPermission | null;
  updating: boolean;
  updateSuccess: boolean;
}

const initialState: PermissionState = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: null,
  updating: false,
  updateSuccess: false,
};

// Async thunks
export const getPermissions = createAsyncThunk(
  'permission/fetch_entity_list',
  async () => {
    const response = await permissionService.getPermissions();
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const getPermission = createAsyncThunk(
  'permission/fetch_entity',
  async (id: number) => {
    const response = await permissionService.getPermission(id);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const createPermission = createAsyncThunk(
  'permission/create_entity',
  async (permission: IPermission) => {
    const response = await permissionService.createPermission(permission);
    return response; // Return full response to allow notification middleware to read headers
  },
  { serializeError: serializeAxiosError },
);

export const updatePermission = createAsyncThunk(
  'permission/update_entity',
  async (permission: IPermission) => {
    const response = await permissionService.updatePermission(permission);
    return response; // Return full response to allow notification middleware to read headers
  },
  { serializeError: serializeAxiosError },
);

export const deletePermission = createAsyncThunk(
  'permission/delete_entity',
  async (id: number) => {
    await permissionService.deletePermission(id);
    return id;
  },
  { serializeError: serializeAxiosError },
);

export const searchPermissions = createAsyncThunk(
  'permission/search',
  async (query: string) => {
    const response = await permissionService.searchPermissions(query);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

// Slice
export const PermissionSlice = createSlice({
  name: 'permission',
  initialState,
  reducers: {
    reset() {
      return initialState;
    },
    clearError(state) {
      state.errorMessage = null;
      state.updateSuccess = false;
    },
  },
  extraReducers(builder) {
    builder
      // Get all permissions
      .addCase(getPermissions.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getPermissions.fulfilled, (state, action) => {
        state.loading = false;
        state.entities = action.payload;
      })
      .addCase(getPermissions.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message || 'Error loading permissions';
      })
      // Get single permission
      .addCase(getPermission.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getPermission.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload;
      })
      .addCase(getPermission.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message || 'Error loading permission';
      })
      // Create permission
      .addCase(createPermission.pending, state => {
        state.updating = true;
        state.updateSuccess = false;
        state.errorMessage = null;
      })
      .addCase(createPermission.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
        state.entities.push(action.payload.data);
      })
      .addCase(createPermission.rejected, (state, action) => {
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message || 'Error creating permission';
      })
      // Update permission
      .addCase(updatePermission.pending, state => {
        state.updating = true;
        state.updateSuccess = false;
        state.errorMessage = null;
      })
      .addCase(updatePermission.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
        const index = state.entities.findIndex(e => e.id === action.payload.data.id);
        if (index !== -1) {
          state.entities[index] = action.payload.data;
        }
      })
      .addCase(updatePermission.rejected, (state, action) => {
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message || 'Error updating permission';
      })
      // Delete permission
      .addCase(deletePermission.pending, state => {
        state.updating = true;
        state.updateSuccess = false;
        state.errorMessage = null;
      })
      .addCase(deletePermission.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entities = state.entities.filter(e => e.id !== action.payload);
      })
      .addCase(deletePermission.rejected, (state, action) => {
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message || 'Error deleting permission';
      })
      // Search permissions
      .addCase(searchPermissions.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(searchPermissions.fulfilled, (state, action) => {
        state.loading = false;
        state.entities = action.payload;
      })
      .addCase(searchPermissions.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message || 'Error searching permissions';
      });
  },
});

export const { reset, clearError } = PermissionSlice.actions;

export default PermissionSlice.reducer;
