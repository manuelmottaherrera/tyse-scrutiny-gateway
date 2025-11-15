import 'react-toastify/dist/ReactToastify.css';
import './app.scss';
import 'app/config/dayjs';

import React, { useEffect, useState } from 'react';
import { Card } from 'reactstrap';
import { BrowserRouter } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import { GoogleReCaptchaProvider } from 'react-google-recaptcha-v3';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';
import { getProfile } from 'app/shared/reducers/application-profile';
import Header from 'app/shared/layout/header/header';
import Footer from 'app/shared/layout/footer/footer';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import ErrorBoundary from 'app/shared/error/error-boundary';
import { AUTHORITIES } from 'app/config/constants';
import AppRoutes from 'app/routes';
import { ThemeProvider } from 'app/shared/context/theme-contex/theme-context';

const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

export const App = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getSession());
    dispatch(getProfile());
  }, []);

  const currentLocale = useAppSelector(state => state.locale.currentLocale);
  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
  const isAdmin = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [AUTHORITIES.ADMIN]));
  const ribbonEnv = useAppSelector(state => state.applicationProfile.ribbonEnv);
  const isInProduction = useAppSelector(state => state.applicationProfile.inProduction);
  const isOpenAPIEnabled = useAppSelector(state => state.applicationProfile.isOpenAPIEnabled);
  const siteKey = process.env.REACT_APP_RECAPTCHA_SITE_KEY || '';
  const [theme, setTheme] = useState<'light' | 'dark'>('dark');
  const paddingTop = '60px';
  return (
    <ThemeProvider>
      <GoogleReCaptchaProvider
        reCaptchaKey={siteKey}
        scriptProps={{
          async: true,
          defer: true,
          appendTo: 'head',
        }}
        language="en"
        container={{ element: 'recaptcha', parameters: { badge: 'inline', theme } }}
      >
        <BrowserRouter basename={baseHref}>
          <div className="app-container" style={{ paddingTop }}>
            <ToastContainer position="top-left" className="toastify-container" toastClassName="toastify-toast" autoClose={30000} />
            <ErrorBoundary>
              <Header
                isAuthenticated={isAuthenticated}
                isAdmin={isAdmin}
                currentLocale={currentLocale}
                ribbonEnv={ribbonEnv}
                isInProduction={isInProduction}
                isOpenAPIEnabled={isOpenAPIEnabled}
              />
            </ErrorBoundary>
            <div className="container-fluid view-container" id="app-view-container">
              <Card className="jh-card">
                <ErrorBoundary>
                  <AppRoutes />
                </ErrorBoundary>
              </Card>
              <Footer />
            </div>
          </div>
        </BrowserRouter>
      </GoogleReCaptchaProvider>
    </ThemeProvider>
  );
};

export default App;
