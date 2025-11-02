import axios from 'axios';
import { IAuthorityAudit } from '../model/authorization/authority-audit.model';
import { AuditAction } from '../model/enumerations/audit-action.model';

const apiUrl = 'api/authority-audits';

export interface IAuditSearchParams {
  authorityId?: number;
  changedBy?: string;
  action?: AuditAction;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}

export interface IAuditMetricsSummary {
  totalAudits: number;
  auditsByAction: Record<string, number>;
  topUsers: Array<{ username: string; count: number }>;
  recentActivity: {
    last24Hours: number;
    last7Days: number;
    last30Days: number;
  };
}

export const getAudits = (page = 0, size = 20) => axios.get<IAuthorityAudit[]>(`${apiUrl}?page=${page}&size=${size}`);

export const getAudit = (id: number) => axios.get<IAuthorityAudit>(`${apiUrl}/${id}`);

export const getAuditsByAuthority = (authorityId: number) => axios.get<IAuthorityAudit[]>(`${apiUrl}/authority/${authorityId}`);

export const searchAudits = (params: IAuditSearchParams) => {
  const queryParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      queryParams.append(key, value.toString());
    }
  });
  return axios.get<IAuthorityAudit[]>(`${apiUrl}/search?${queryParams.toString()}`);
};

export const getRecentAudits = (limit = 50) => axios.get<IAuthorityAudit[]>(`${apiUrl}/recent?limit=${limit}`);

export const exportAuditsAsCsv = (params?: IAuditSearchParams) => {
  const queryParams = new URLSearchParams();
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        queryParams.append(key, value.toString());
      }
    });
  }
  return axios.get(`${apiUrl}/export/csv?${queryParams.toString()}`, { responseType: 'blob' });
};

export const exportAuditsAsJson = (params?: IAuditSearchParams) => {
  const queryParams = new URLSearchParams();
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        queryParams.append(key, value.toString());
      }
    });
  }
  return axios.get(`${apiUrl}/export/json?${queryParams.toString()}`, { responseType: 'blob' });
};

export const getMetricsSummary = () => axios.get<IAuditMetricsSummary>(`${apiUrl}/metrics/summary`);

export const getMetricsByAuthority = (authorityId: number) => axios.get(`${apiUrl}/metrics/by-authority/${authorityId}`);
