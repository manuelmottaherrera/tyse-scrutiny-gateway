import React from 'react';
import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import { bindActionCreators } from 'redux';

import getStore from 'app/config/store';
import { registerLocale } from 'app/config/translation';
import setupAxiosInterceptors from 'app/config/axios-interceptor';
import { clearAuthentication } from 'app/shared/reducers/authentication';
import ErrorBoundary from 'app/shared/error/error-boundary';
import AppComponent from 'app/app';
import { loadIcons } from 'app/config/icon-loader';
import { Fade } from 'reactstrap';

// Reactstrap 9.x inherits Transition.propTypes (which marks `timeout` as required)
// but applies the default internally via addDefaultProps() instead of static
// defaultProps. React 18.3+ validates PropTypes before that runs, causing a
// spurious warning. Removing the inherited `timeout` propType silences it
// without affecting runtime behavior.
if ((Fade as any).propTypes) {
  delete (Fade as any).propTypes.timeout;
}

const store = getStore();
registerLocale(store);

const actions = bindActionCreators({ clearAuthentication }, store.dispatch);
setupAxiosInterceptors(() => actions.clearAuthentication('login.error.unauthorized'));

loadIcons();

const rootEl = document.getElementById('root');
const root = createRoot(rootEl);

const render = Component =>
  root.render(
    <ErrorBoundary>
      <Provider store={store}>
        <div>
          <Component />
        </div>
      </Provider>
    </ErrorBoundary>,
  );

render(AppComponent);
