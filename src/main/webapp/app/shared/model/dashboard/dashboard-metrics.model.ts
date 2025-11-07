import { IRecentActivity } from './recent-activity.model';
import { IExpiringRole } from './expiring-role.model';
import { IAuthorityUsage } from './authority-usage.model';
import { IPermissionUsage } from './permission-usage.model';

export interface IDashboardMetrics {
  totalAuthorities?: number;
  totalPermissions?: number;
  activeUsers?: number;
  expiredRoles?: number;
  recentActivity?: IRecentActivity[];
  expiringRoles?: IExpiringRole[];
  topAuthorities?: IAuthorityUsage[];
  permissionUsage?: IPermissionUsage[];
}

export const defaultValue: Readonly<IDashboardMetrics> = {
  totalAuthorities: 0,
  totalPermissions: 0,
  activeUsers: 0,
  expiredRoles: 0,
  recentActivity: [],
  expiringRoles: [],
  topAuthorities: [],
  permissionUsage: [],
};
