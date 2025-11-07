import { AuditActionType } from '../authorization/audit-action-type.model';

export interface IRecentActivity {
  id?: number;
  authorityId?: number;
  authorityName?: string;
  action?: AuditActionType;
  changedBy?: string;
  changedDate?: Date;
  description?: string;
}

export const defaultValue: Readonly<IRecentActivity> = {};
