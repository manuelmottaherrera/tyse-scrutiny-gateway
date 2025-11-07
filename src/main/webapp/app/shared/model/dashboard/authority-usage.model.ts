export interface IAuthorityUsage {
  authorityId?: number;
  authorityName?: string;
  authorityCode?: string;
  userCount?: number;
  activeCount?: number;
  expiredCount?: number;
}

export const defaultValue: Readonly<IAuthorityUsage> = {
  userCount: 0,
  activeCount: 0,
  expiredCount: 0,
};
