export interface IExpiringRole {
  id?: number;
  userId?: number;
  userName?: string;
  userLogin?: string;
  authorityId?: number;
  authorityName?: string;
  expiresAt?: Date;
  daysUntilExpiration?: number;
}

export const defaultValue: Readonly<IExpiringRole> = {};
