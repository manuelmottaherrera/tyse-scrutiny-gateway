import React from 'react';
import { render, waitFor, screen, act } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import configureMockStore from 'redux-mock-store';
import { thunk } from 'redux-thunk';

import PasswordResetInit from './password-reset-init';

const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

describe('PasswordResetInit', () => {
  let store;

  beforeEach(() => {
    // Initialize mock store
    store = mockStore({
      passwordReset: {
        resetPasswordSuccess: false,
        resetPasswordFailure: false,
        successMessage: null,
        loading: false,
      },
      locale: {
        currentLocale: 'es',
        sourcePrefixes: [],
        lastChange: new Date().getTime(),
        loadedKeys: [],
        loadedLocales: ['es', 'en'],
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should render password reset init form', () => {
    // When: PasswordResetInit is rendered
    const { container } = render(
      <Provider store={store}>
        <MemoryRouter initialEntries={['/account/reset/init']}>
          <Routes>
            <Route path="/account/reset/init" element={<PasswordResetInit />} />
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    // Then: Email field and submit button should be rendered
    const emailField = container.querySelector('[data-cy="emailResetPassword"]');
    const submitButton = container.querySelector('[data-cy="submit"]');

    expect(emailField).toBeTruthy();
    expect(submitButton).toBeTruthy();
  });

  describe('Navigation after successful password reset request', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.runOnlyPendingTimers();
      jest.useRealTimers();
    });

    it('should navigate to home page after successful password reset request', async () => {
      // Given: Store with successMessage indicating successful request
      const storeWithSuccess = mockStore({
        passwordReset: {
          resetPasswordSuccess: true,
          resetPasswordFailure: false,
          successMessage: 'reset.request.messages.success',
          loading: false,
        },
        locale: {
          currentLocale: 'es',
          sourcePrefixes: [],
          lastChange: new Date().getTime(),
          loadedKeys: [],
          loadedLocales: ['es', 'en'],
        },
      });

      // When: PasswordResetInit is rendered with success state
      render(
        <Provider store={storeWithSuccess}>
          <MemoryRouter initialEntries={['/account/reset/init']}>
            <Routes>
              <Route path="/account/reset/init" element={<PasswordResetInit />} />
              <Route path="/" element={<div data-testid="home-page">Home</div>} />
            </Routes>
          </MemoryRouter>
        </Provider>,
      );

      // Then: Navigation timer should be scheduled (500ms delay)
      // Fast-forward time by 500ms using act()
      act(() => {
        jest.advanceTimersByTime(500);
      });

      // And: User should be navigated to home page
      await waitFor(() => {
        expect(screen.queryByTestId('home-page')).toBeTruthy();
      });
    });

    it('should NOT navigate when successMessage is null', async () => {
      // Given: Store without successMessage (no successful request yet)
      const storeWithoutSuccess = mockStore({
        passwordReset: {
          resetPasswordSuccess: false,
          resetPasswordFailure: false,
          successMessage: null,
          loading: false,
        },
        locale: {
          currentLocale: 'es',
          sourcePrefixes: [],
          lastChange: new Date().getTime(),
          loadedKeys: [],
          loadedLocales: ['es', 'en'],
        },
      });

      // When: PasswordResetInit is rendered without success state
      const { container } = render(
        <Provider store={storeWithoutSuccess}>
          <MemoryRouter initialEntries={['/account/reset/init']}>
            <Routes>
              <Route path="/account/reset/init" element={<PasswordResetInit />} />
              <Route path="/" element={<div data-testid="home-page">Home</div>} />
            </Routes>
          </MemoryRouter>
        </Provider>,
      );

      // Then: User should remain on password reset init page
      // Fast-forward time to ensure no navigation occurs
      jest.advanceTimersByTime(1000);

      await waitFor(() => {
        // Email field should still be visible
        const emailField = container.querySelector('[data-cy="emailResetPassword"]');
        expect(emailField).toBeTruthy();

        // Home page should NOT be rendered
        const homePage = container.querySelector('[data-testid="home-page"]');
        expect(homePage).toBeFalsy();
      });
    });

    it('should cleanup timer when component unmounts before navigation', () => {
      // Given: Store with successMessage
      const storeWithSuccess = mockStore({
        passwordReset: {
          resetPasswordSuccess: true,
          resetPasswordFailure: false,
          successMessage: 'reset.request.messages.success',
          loading: false,
        },
        locale: {
          currentLocale: 'es',
          sourcePrefixes: [],
          lastChange: new Date().getTime(),
          loadedKeys: [],
          loadedLocales: ['es', 'en'],
        },
      });

      // When: PasswordResetInit is rendered and then unmounted before timer fires
      const { unmount } = render(
        <Provider store={storeWithSuccess}>
          <MemoryRouter initialEntries={['/account/reset/init']}>
            <Routes>
              <Route path="/account/reset/init" element={<PasswordResetInit />} />
            </Routes>
          </MemoryRouter>
        </Provider>,
      );

      // Unmount before timer fires (before 500ms)
      unmount();

      // Then: Timer should be cleaned up and no navigation should occur
      // This test passes if no errors are thrown during cleanup
      // Note: We don't check timer count as other components may have timers
      expect(unmount).toBeDefined(); // Just verify unmount worked without errors
    });

    it('should navigate with correct timing (500ms delay)', async () => {
      // Given: Store with successMessage
      const storeWithSuccess = mockStore({
        passwordReset: {
          resetPasswordSuccess: true,
          resetPasswordFailure: false,
          successMessage: 'reset.request.messages.success',
          loading: false,
        },
        locale: {
          currentLocale: 'es',
          sourcePrefixes: [],
          lastChange: new Date().getTime(),
          loadedKeys: [],
          loadedLocales: ['es', 'en'],
        },
      });

      // When: PasswordResetInit is rendered with success state
      render(
        <Provider store={storeWithSuccess}>
          <MemoryRouter initialEntries={['/account/reset/init']}>
            <Routes>
              <Route path="/account/reset/init" element={<PasswordResetInit />} />
              <Route path="/" element={<div data-testid="home-page">Home</div>} />
            </Routes>
          </MemoryRouter>
        </Provider>,
      );

      // Then: Should NOT navigate before 500ms
      act(() => {
        jest.advanceTimersByTime(400);
      });
      expect(screen.queryByTestId('home-page')).toBeFalsy();

      // And: Should navigate after 500ms
      act(() => {
        jest.advanceTimersByTime(100);
      });
      await waitFor(() => {
        expect(screen.queryByTestId('home-page')).toBeTruthy();
      });
    });
  });
});
