import { IAuthority } from './authority.model';

export interface IUserAuthority {
  id?: number;
  userId?: number;
  authorityId?: number;
  authority?: IAuthority;
  assignedBy?: string;
  assignedDate?: Date | null;
  expiresAt?: Date | null;
  isActive?: boolean;
  revokedBy?: string;
  revokedDate?: Date | null;
  revokedReason?: string;
}

export const defaultValue: Readonly<IUserAuthority> = {
  isActive: true,
};
