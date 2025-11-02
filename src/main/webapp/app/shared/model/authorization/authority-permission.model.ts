import { IAuthority } from './authority.model';
import { IPermission } from './permission.model';

export interface IAuthorityPermission {
  id?: number;
  authorityId?: number;
  permissionId?: number;
  authority?: IAuthority;
  permission?: IPermission;
  assignedDate?: Date | null;
  isActive?: boolean;
}

export const defaultValue: Readonly<IAuthorityPermission> = {
  isActive: true,
};
