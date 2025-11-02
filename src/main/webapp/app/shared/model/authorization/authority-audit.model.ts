import { AuditAction } from '../enumerations/audit-action.model';

export interface IAuthorityAudit {
  id?: number;
  authorityId?: number;
  authorityCode?: string;
  action?: AuditAction;
  oldValues?: string;
  newValues?: string;
  changedBy?: string;
  changedDate?: Date | null;
  ipAddress?: string;
  userAgent?: string;
}

export const defaultValue: Readonly<IAuthorityAudit> = {};
