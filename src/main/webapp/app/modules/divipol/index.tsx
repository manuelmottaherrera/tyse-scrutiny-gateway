import React, { lazy, Suspense } from 'react';
import { Route } from 'react-router';
import { Spinner } from 'reactstrap';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import getStore from 'app/config/store';
import divipolReducer from './divipol.reducer';
import DivipolPage from './divipol';

// Lazy load pages for code splitting
const PuestoDetailPage = lazy(() => import('./pages/puesto-detail-page'));
const OrganizacionesPage = lazy(() => import('./pages/organizaciones-page'));
const ComisionesPage = lazy(() => import('./pages/comisiones-page'));
const ReclamacionesPage = lazy(() => import('./pages/reclamaciones-page'));
const ConfiguracionElectoralPage = lazy(() => import('./pages/configuracion-electoral-page'));

// Inyectar el reducer de divipol en el store
const store = getStore();
store.injectReducer('divipol', divipolReducer);

const Loading = () => (
  <div className="text-center py-5">
    <Spinner color="primary" />
  </div>
);

const DivipolRoutes = () => (
  <Suspense fallback={<Loading />}>
    <ErrorBoundaryRoutes>
      <Route index element={<DivipolPage />} />
      <Route path="puestos/:puestoId" element={<PuestoDetailPage />} />
      <Route path="organizaciones" element={<OrganizacionesPage />} />
      <Route path="comisiones" element={<ComisionesPage />} />
      <Route path="reclamaciones" element={<ReclamacionesPage />} />
      <Route path="configuracion" element={<ConfiguracionElectoralPage />} />
    </ErrorBoundaryRoutes>
  </Suspense>
);

export default DivipolRoutes;
