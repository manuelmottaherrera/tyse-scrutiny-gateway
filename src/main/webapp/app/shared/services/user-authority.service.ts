import axios from 'axios';
import { IUserAuthority } from '../model/authorization/user-authority.model';

const apiUrl = 'api/user-authorities';

export const getUserAuthorities = () => axios.get<IUserAuthority[]>(apiUrl);

export const getUserAuthority = (id: number) => axios.get<IUserAuthority>(`${apiUrl}/${id}`);

export const getUserAuthoritiesByUser = (userId: number) => axios.get<IUserAuthority[]>(`${apiUrl}/user/${userId}`);

export const getUserAuthoritiesByAuthority = (authorityId: number) => axios.get<IUserAuthority[]>(`${apiUrl}/authority/${authorityId}`);

export const assignAuthorityToUser = (userAuthority: IUserAuthority) => axios.post<IUserAuthority>(apiUrl, userAuthority);

export const revokeAuthorityFromUser = (id: number, reason: string) => axios.put(`${apiUrl}/${id}/revoke`, { reason });

export const getActiveAuthoritiesByUser = (userId: number) => axios.get<IUserAuthority[]>(`${apiUrl}/user/${userId}/active`);

export const getExpiringAuthorities = (days: number) => axios.get<IUserAuthority[]>(`${apiUrl}/expiring?days=${days}`);
