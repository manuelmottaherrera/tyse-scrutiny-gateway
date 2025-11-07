import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import PermissionList from './permission-list';
import PermissionDetail from './permission-detail';
import PermissionForm from './permission-form';
import PermissionDeleteDialog from './permission-delete-dialog';

const PermissionRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<PermissionList />} />
    <Route path="new" element={<PermissionForm />} />
    <Route path=":id">
      <Route index element={<PermissionDetail />} />
      <Route path="edit" element={<PermissionForm />} />
      <Route path="delete" element={<PermissionDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PermissionRoutes;
