import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import AuthorityList from './authority-list';
import AuthorityDetail from './authority-detail';
import AuthorityForm from './authority-form';
import AuthorityDeleteDialog from './authority-delete-dialog';

const AuthorityRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<AuthorityList />} />
    <Route path="new" element={<AuthorityForm />} />
    <Route path=":id">
      <Route index element={<AuthorityDetail />} />
      <Route path="edit" element={<AuthorityForm />} />
      <Route path="delete" element={<AuthorityDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AuthorityRoutes;
