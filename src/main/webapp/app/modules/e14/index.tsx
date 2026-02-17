import React, { lazy, Suspense } from 'react';
import { Route } from 'react-router';
import { Spinner } from 'reactstrap';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

// Lazy load pages for code splitting
const E14UploadPage = lazy(() => import('./e14-upload'));

const Loading = () => (
  <div className="text-center py-5">
    <Spinner color="primary" />
  </div>
);

const E14Routes = () => (
  <Suspense fallback={<Loading />}>
    <ErrorBoundaryRoutes>
      <Route index element={<E14UploadPage />} />
      <Route path="upload" element={<E14UploadPage />} />
    </ErrorBoundaryRoutes>
  </Suspense>
);

export default E14Routes;
