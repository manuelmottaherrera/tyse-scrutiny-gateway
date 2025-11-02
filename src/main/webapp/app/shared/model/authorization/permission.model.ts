export interface IPermission {
  id?: number;
  name?: string;
  resource?: string;
  action?: string;
  description?: string;
  isActive?: boolean;
  createdDate?: Date | null;
  lastModifiedDate?: Date | null;
}

export const defaultValue: Readonly<IPermission> = {
  isActive: true,
};
