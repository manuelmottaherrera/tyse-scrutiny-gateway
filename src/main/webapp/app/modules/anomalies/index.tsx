import React, { lazy, Suspense } from 'react';
import { Route } from 'react-router';
import { Spinner } from 'reactstrap';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import getStore from 'app/config/store';
import anomalyReducer from './anomaly.reducer';
import AnomaliesPage from './anomalies-page';

// Lazy load pages for code splitting
const AnomalyDetailPage = lazy(() => import('./anomaly-detail-page'));

// Inyectar el reducer de anomalías en el store
const store = getStore();
store.injectReducer('anomaly', anomalyReducer);

const Loading = () => (
  <div className="text-center py-5">
    <Spinner color="primary" />
  </div>
);

const AnomaliesRoutes = () => (
  <Suspense fallback={<Loading />}>
    <ErrorBoundaryRoutes>
      <Route index element={<AnomaliesPage />} />
      <Route path=":id" element={<AnomalyDetailPage />} />
    </ErrorBoundaryRoutes>
  </Suspense>
);

export default AnomaliesRoutes;
