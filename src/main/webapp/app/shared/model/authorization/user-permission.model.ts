import { IPermission } from './permission.model';

export interface IUserPermission {
  id?: number;
  userId?: number;
  permissionId?: number;
  permission?: IPermission;
  assignedBy?: string;
  assignedDate?: Date | null;
  expiresAt?: Date | null;
  isActive?: boolean;
  revokedBy?: string;
  revokedDate?: Date | null;
  revokedReason?: string;
}

export const defaultValue: Readonly<IUserPermission> = {
  isActive: true,
};
