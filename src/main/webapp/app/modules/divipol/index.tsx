import React, { useEffect } from 'react';
import { Route } from 'react-router';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import getStore from 'app/config/store';
import divipolReducer from './divipol.reducer';
import DivipolPage from './divipol';

// Inyectar el reducer de divipol en el store
const store = getStore();
store.injectReducer('divipol', divipolReducer);

const DivipolRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<DivipolPage />} />
  </ErrorBoundaryRoutes>
);

export default DivipolRoutes;
