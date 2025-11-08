import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { IAuthority } from '../../model/authorization/authority.model';
import * as authorityService from '../../services/authority.service';
import { serializeAxiosError } from '../reducer.utils';

interface AuthorityState {
  loading: boolean;
  errorMessage: string | null;
  entities: IAuthority[];
  entity: IAuthority | null;
  updating: boolean;
  updateSuccess: boolean;
  permissions: any[];
  permissionsLoading: boolean;
}

const initialState: AuthorityState = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: null,
  updating: false,
  updateSuccess: false,
  permissions: [],
  permissionsLoading: false,
};

// Async thunks
export const getAuthorities = createAsyncThunk(
  'authority/fetch_entity_list',
  async () => {
    const response = await authorityService.getAuthorities();
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const getAuthority = createAsyncThunk(
  'authority/fetch_entity',
  async (id: number) => {
    const response = await authorityService.getAuthority(id);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const createAuthority = createAsyncThunk(
  'authority/create_entity',
  async (authority: IAuthority) => {
    const response = await authorityService.createAuthority(authority);
    return response; // Return full response to allow notification middleware to read headers
  },
  { serializeError: serializeAxiosError },
);

export const updateAuthority = createAsyncThunk(
  'authority/update_entity',
  async (authority: IAuthority) => {
    const response = await authorityService.updateAuthority(authority);
    return response; // Return full response to allow notification middleware to read headers
  },
  { serializeError: serializeAxiosError },
);

export const deleteAuthority = createAsyncThunk(
  'authority/delete_entity',
  async (id: number) => {
    await authorityService.deleteAuthority(id);
    return id;
  },
  { serializeError: serializeAxiosError },
);

export const getAuthorityPermissions = createAsyncThunk(
  'authority/fetch_permissions',
  async (id: number) => {
    const response = await authorityService.getAuthorityPermissions(id);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const assignPermissionToAuthority = createAsyncThunk(
  'authority/assign_permission',
  async ({ authorityId, permissionId }: { authorityId: number; permissionId: number }) => {
    const response = await authorityService.assignPermissionToAuthority(authorityId, permissionId);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const revokePermissionFromAuthority = createAsyncThunk(
  'authority/revoke_permission',
  async ({ authorityId, permissionId }: { authorityId: number; permissionId: number }) => {
    await authorityService.revokePermissionFromAuthority(authorityId, permissionId);
    return { authorityId, permissionId };
  },
  { serializeError: serializeAxiosError },
);

// Slice
export const AuthoritySlice = createSlice({
  name: 'authority',
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
      // Get all authorities
      .addCase(getAuthorities.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getAuthorities.fulfilled, (state, action) => {
        state.loading = false;
        state.entities = action.payload;
      })
      .addCase(getAuthorities.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message || 'Error loading authorities';
      })
      // Get single authority
      .addCase(getAuthority.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getAuthority.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload;
      })
      .addCase(getAuthority.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message || 'Error loading authority';
      })
      // Create authority
      .addCase(createAuthority.pending, state => {
        state.updating = true;
        state.updateSuccess = false;
        state.errorMessage = null;
      })
      .addCase(createAuthority.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
        state.entities.push(action.payload.data);
      })
      .addCase(createAuthority.rejected, (state, action: any) => {
        state.updating = false;
        state.updateSuccess = false;
        // Extract error message from Axios error response
        const axiosError = action.payload || action.error;
        if (axiosError?.response?.data?.message) {
          state.errorMessage = axiosError.response.data.message;
        } else if (axiosError?.response?.data?.title) {
          // Map technical HTTP errors to user-friendly messages
          state.errorMessage = 'error.authority.createFailed';
        } else if (axiosError?.message) {
          state.errorMessage = axiosError.message;
        } else {
          state.errorMessage = 'error.authority.createFailed';
        }
      })
      // Update authority
      .addCase(updateAuthority.pending, state => {
        state.updating = true;
        state.updateSuccess = false;
        state.errorMessage = null;
      })
      .addCase(updateAuthority.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
        const index = state.entities.findIndex(e => e.id === action.payload.data.id);
        if (index !== -1) {
          state.entities[index] = action.payload.data;
        }
      })
      .addCase(updateAuthority.rejected, (state, action: any) => {
        state.updating = false;
        state.updateSuccess = false;
        // Extract error message from Axios error response
        const axiosError = action.payload || action.error;
        if (axiosError?.response?.data?.message) {
          state.errorMessage = axiosError.response.data.message;
        } else if (axiosError?.response?.data?.title) {
          // Map technical HTTP errors to user-friendly messages
          state.errorMessage = 'error.authority.updateFailed';
        } else if (axiosError?.message) {
          state.errorMessage = axiosError.message;
        } else {
          state.errorMessage = 'error.authority.updateFailed';
        }
      })
      // Delete authority
      .addCase(deleteAuthority.pending, state => {
        state.updating = true;
        state.updateSuccess = false;
        state.errorMessage = null;
      })
      .addCase(deleteAuthority.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entities = state.entities.filter(e => e.id !== action.payload);
      })
      .addCase(deleteAuthority.rejected, (state, action) => {
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message || 'Error deleting authority';
      })
      // Assign permission
      .addCase(assignPermissionToAuthority.pending, state => {
        state.updating = true;
        state.errorMessage = null;
      })
      .addCase(assignPermissionToAuthority.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
      })
      .addCase(assignPermissionToAuthority.rejected, (state, action) => {
        state.updating = false;
        state.errorMessage = action.error.message || 'Error assigning permission';
      })
      // Revoke permission
      .addCase(revokePermissionFromAuthority.pending, state => {
        state.updating = true;
        state.errorMessage = null;
      })
      .addCase(revokePermissionFromAuthority.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
      })
      .addCase(revokePermissionFromAuthority.rejected, (state, action) => {
        state.updating = false;
        state.errorMessage = action.error.message || 'Error revoking permission';
      })
      // Get authority permissions
      .addCase(getAuthorityPermissions.pending, state => {
        state.permissionsLoading = true;
        // Don't set errorMessage here - let silent failures happen
      })
      .addCase(getAuthorityPermissions.fulfilled, (state, action) => {
        state.permissionsLoading = false;
        state.permissions = action.payload;
      })
      .addCase(getAuthorityPermissions.rejected, state => {
        state.permissionsLoading = false;
        // Silently handle error - just set empty permissions
        state.permissions = [];
        // Don't set errorMessage - this prevents toast notification
      });
  },
});

export const { reset, clearError } = AuthoritySlice.actions;

export default AuthoritySlice.reducer;
