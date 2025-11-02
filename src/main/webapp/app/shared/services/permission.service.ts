import axios from 'axios';
import { IPermission } from '../model/authorization/permission.model';

const apiUrl = 'api/permissions';

export const getPermissions = () => axios.get<IPermission[]>(apiUrl);

export const getPermission = (id: number) => axios.get<IPermission>(`${apiUrl}/${id}`);

export const createPermission = (permission: IPermission) => axios.post<IPermission>(apiUrl, permission);

export const updatePermission = (permission: IPermission) => axios.put<IPermission>(`${apiUrl}/${permission.id}`, permission);

export const deletePermission = (id: number) => axios.delete(`${apiUrl}/${id}`);

export const searchPermissions = (query: string) => axios.get<IPermission[]>(`${apiUrl}/search?query=${query}`);

export const getPermissionsByResource = (resource: string) => axios.get<IPermission[]>(`${apiUrl}/resource/${resource}`);
