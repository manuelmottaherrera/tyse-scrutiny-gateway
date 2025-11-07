import axios from 'axios';
import { IDashboardMetrics } from '../model/dashboard/dashboard-metrics.model';

const apiUrl = 'api/authorization/dashboard';

export const getDashboardMetrics = () => axios.get<IDashboardMetrics>(`${apiUrl}/metrics`);
