export interface IPermissionUsage {
  permissionId?: number;
  permissionName?: string;
  resource?: string;
  action?: string;
  usageCount?: number;
  authorityCount?: number;
  directUserCount?: number;
}

export const defaultValue: Readonly<IPermissionUsage> = {
  usageCount: 0,
  authorityCount: 0,
  directUserCount: 0,
};
