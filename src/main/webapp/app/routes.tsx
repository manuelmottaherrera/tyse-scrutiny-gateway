import React, { Suspense } from 'react';
import { Route } from 'react-router';

import Login from 'app/modules/login/login';
import Register from 'app/modules/account/register/register';
import Activate from 'app/modules/account/activate/activate';
import PasswordResetInit from 'app/modules/account/password-reset/init/password-reset-init';
import PasswordResetFinish from 'app/modules/account/password-reset/finish/password-reset-finish';
import Logout from 'app/modules/login/logout';
import Home from 'app/modules/home/home';
import EntitiesRoutes from 'app/entities/routes';
import PrivateRoute from 'app/shared/auth/private-route';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import PageNotFound from 'app/shared/error/page-not-found';
import { AUTHORITIES } from 'app/config/constants';

const loading = <div>loading ...</div>;

const Account = React.lazy(() => import(/* webpackChunkName: "account" */ 'app/modules/account'));

const Admin = React.lazy(() => import(/* webpackChunkName: "administration" */ 'app/modules/administration'));

const Divipol = React.lazy(() => import(/* webpackChunkName: "divipol" */ 'app/modules/divipol'));

const E14 = React.lazy(() => import(/* webpackChunkName: "e14" */ 'app/modules/e14'));

const Anomalies = React.lazy(() => import(/* webpackChunkName: "anomalies" */ 'app/modules/anomalies'));
const AppRoutes = () => {
  return (
    <div className="view-routes">
      <ErrorBoundaryRoutes>
        <Route index element={<Home />} />
        <Route path="login" element={<Login />} />
        <Route path="logout" element={<Logout />} />
        <Route path="account">
          <Route
            path="*"
            element={
              <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.USER]}>
                <Suspense fallback={loading}>
                  <Account />
                </Suspense>
              </PrivateRoute>
            }
          />
          <Route path="register" element={<Register />} />
          <Route path="activate" element={<Activate />} />
          <Route path="reset">
            <Route path="request" element={<PasswordResetInit />} />
            <Route path="finish" element={<PasswordResetFinish />} />
          </Route>
        </Route>
        <Route
          path="admin/*"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN]}>
              <Suspense fallback={loading}>
                <Admin />
              </Suspense>
            </PrivateRoute>
          }
        />
        <Route
          path="divipol/*"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.USER]}>
              <Suspense fallback={loading}>
                <Divipol />
              </Suspense>
            </PrivateRoute>
          }
        />
        <Route
          path="e14/*"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.USER]}>
              <Suspense fallback={loading}>
                <E14 />
              </Suspense>
            </PrivateRoute>
          }
        />
        <Route
          path="anomalies/*"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.USER]}>
              <Suspense fallback={loading}>
                <Anomalies />
              </Suspense>
            </PrivateRoute>
          }
        />
        <Route
          path="*"
          element={
            <PrivateRoute hasAnyAuthorities={[AUTHORITIES.USER]}>
              <EntitiesRoutes />
            </PrivateRoute>
          }
        />
        <Route path="*" element={<PageNotFound />} />
      </ErrorBoundaryRoutes>
    </div>
  );
};

export default AppRoutes;
