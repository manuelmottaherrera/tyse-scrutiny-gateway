import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { IUserAuthority } from '../../model/authorization/user-authority.model';
import * as userAuthorityService from '../../services/user-authority.service';
import { serializeAxiosError } from '../reducer.utils';

interface UserAuthorityState {
  loading: boolean;
  errorMessage: string | null;
  entities: IUserAuthority[];
  entity: IUserAuthority | null;
  updating: boolean;
  updateSuccess: boolean;
  expiringAuthorities: IUserAuthority[];
}

const initialState: UserAuthorityState = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: null,
  updating: false,
  updateSuccess: false,
  expiringAuthorities: [],
};

// Async thunks
export const getUserAuthorities = createAsyncThunk(
  'userAuthority/fetch_entity_list',
  async () => {
    const response = await userAuthorityService.getUserAuthorities();
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const getUserAuthority = createAsyncThunk(
  'userAuthority/fetch_entity',
  async (id: number) => {
    const response = await userAuthorityService.getUserAuthority(id);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const getUserAuthoritiesByUser = createAsyncThunk(
  'userAuthority/fetch_by_user',
  async (userId: number) => {
    const response = await userAuthorityService.getUserAuthoritiesByUser(userId);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const getActiveAuthoritiesByUser = createAsyncThunk(
  'userAuthority/fetch_active_by_user',
  async (userId: number) => {
    const response = await userAuthorityService.getActiveAuthoritiesByUser(userId);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const assignAuthorityToUser = createAsyncThunk(
  'userAuthority/assign',
  async (userAuthority: IUserAuthority) => {
    const response = await userAuthorityService.assignAuthorityToUser(userAuthority);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const revokeAuthorityFromUser = createAsyncThunk(
  'userAuthority/revoke',
  async ({ id, reason }: { id: number; reason: string }) => {
    await userAuthorityService.revokeAuthorityFromUser(id, reason);
    return id;
  },
  { serializeError: serializeAxiosError },
);

export const getExpiringAuthorities = createAsyncThunk(
  'userAuthority/fetch_expiring',
  async (days: number) => {
    const response = await userAuthorityService.getExpiringAuthorities(days);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

// Slice
export const UserAuthoritySlice = createSlice({
  name: 'userAuthority',
  initialState,
  reducers: {
    reset() {
      return initialState;
    },
  },
  extraReducers(builder) {
    builder
      // Get all user authorities
      .addCase(getUserAuthorities.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getUserAuthorities.fulfilled, (state, action) => {
        state.loading = false;
        state.entities = action.payload;
      })
      .addCase(getUserAuthorities.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message || 'Error loading user authorities';
      })
      // Get single user authority
      .addCase(getUserAuthority.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getUserAuthority.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload;
      })
      .addCase(getUserAuthority.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message || 'Error loading user authority';
      })
      // Get by user
      .addCase(getUserAuthoritiesByUser.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getUserAuthoritiesByUser.fulfilled, (state, action) => {
        state.loading = false;
        state.entities = action.payload;
      })
      .addCase(getUserAuthoritiesByUser.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message || 'Error loading user authorities';
      })
      // Get active by user
      .addCase(getActiveAuthoritiesByUser.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getActiveAuthoritiesByUser.fulfilled, (state, action) => {
        state.loading = false;
        state.entities = action.payload;
      })
      .addCase(getActiveAuthoritiesByUser.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message || 'Error loading active authorities';
      })
      // Assign authority
      .addCase(assignAuthorityToUser.pending, state => {
        state.updating = true;
        state.updateSuccess = false;
        state.errorMessage = null;
      })
      .addCase(assignAuthorityToUser.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = action.payload;
        state.entities.push(action.payload);
      })
      .addCase(assignAuthorityToUser.rejected, (state, action) => {
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message || 'Error assigning authority';
      })
      // Revoke authority
      .addCase(revokeAuthorityFromUser.pending, state => {
        state.updating = true;
        state.updateSuccess = false;
        state.errorMessage = null;
      })
      .addCase(revokeAuthorityFromUser.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entities = state.entities.filter(e => e.id !== action.payload);
      })
      .addCase(revokeAuthorityFromUser.rejected, (state, action) => {
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message || 'Error revoking authority';
      })
      // Get expiring authorities
      .addCase(getExpiringAuthorities.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getExpiringAuthorities.fulfilled, (state, action) => {
        state.loading = false;
        state.expiringAuthorities = action.payload;
      })
      .addCase(getExpiringAuthorities.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message || 'Error loading expiring authorities';
      });
  },
});

export const { reset } = UserAuthoritySlice.actions;

export default UserAuthoritySlice.reducer;
