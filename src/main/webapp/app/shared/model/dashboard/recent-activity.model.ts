import { AuditAction } from '../enumerations/audit-action.model';

export interface IRecentActivity {
  id?: number;
  authorityId?: number;
  authorityName?: string;
  action?: AuditAction;
  changedBy?: string;
  changedDate?: Date;
  description?: string;
}

export const defaultValue: Readonly<IRecentActivity> = {};
