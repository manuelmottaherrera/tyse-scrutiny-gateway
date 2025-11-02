import { AuthorityCategory } from '../enumerations/authority-category.model';
import { IPermission } from './permission.model';

export interface IAuthority {
  id?: number;
  name?: string;
  code?: string;
  description?: string;
  category?: AuthorityCategory;
  isSystem?: boolean;
  isActive?: boolean;
  hierarchyLevel?: number;
  permissions?: IPermission[];
  createdBy?: string;
  createdDate?: Date | null;
  lastModifiedBy?: string;
  lastModifiedDate?: Date | null;
}

export const defaultValue: Readonly<IAuthority> = {
  isSystem: false,
  isActive: true,
  hierarchyLevel: 1,
  category: AuthorityCategory.CUSTOM,
  permissions: [],
};
