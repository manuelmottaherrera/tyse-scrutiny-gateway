import axios from 'axios';
import { IAuthority } from '../model/authorization/authority.model';
import { IPermission } from '../model/authorization/permission.model';

const apiUrl = 'api/authorities';

export const getAuthorities = () => axios.get<IAuthority[]>(apiUrl);

export const getAuthority = (id: number) => axios.get<IAuthority>(`${apiUrl}/${id}`);

export const createAuthority = (authority: IAuthority) => axios.post<IAuthority>(apiUrl, authority);

export const updateAuthority = (authority: IAuthority) => axios.put<IAuthority>(`${apiUrl}/${authority.id}`, authority);

export const deleteAuthority = (id: number) => axios.delete(`${apiUrl}/${id}`);

export const getAuthorityPermissions = (id: number) => axios.get<IPermission[]>(`${apiUrl}/${id}/permissions`);

export const assignPermissionToAuthority = (authorityId: number, permissionId: number) =>
  axios.post(`api/authority-permissions`, { authorityId, permissionId });

export const revokePermissionFromAuthority = (authorityId: number, permissionId: number) =>
  axios.delete(`api/authority-permissions/authority/${authorityId}/permission/${permissionId}`);

export const activateAuthority = (id: number) => axios.put(`${apiUrl}/${id}/activate`);

export const deactivateAuthority = (id: number) => axios.put(`${apiUrl}/${id}/deactivate`);
