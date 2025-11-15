import React from 'react';
import { render, waitFor, screen, act } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { Storage } from 'react-jhipster';
import configureMockStore from 'redux-mock-store';
import { thunk } from 'redux-thunk';

import PasswordResetFinishPage from './password-reset-finish';

const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

describe('PasswordResetFinishPage', () => {
  let store;

  beforeEach(() => {
    // Clear session storage before each test
    Storage.session.remove('locale');

    // Initialize mock store
    store = mockStore({
      passwordReset: {
        resetPasswordSuccess: false,
        resetPasswordFailure: false,
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

  it('should set locale to Spanish when ?lang=es is in URL', async () => {
    // Given: URL with ?lang=es parameter
    const resetKey = 'test-reset-key';

    // When: PasswordResetFinishPage is rendered with lang=es
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/account/reset/finish?key=${resetKey}&lang=es`]}>
          <Routes>
            <Route path="/account/reset/finish" element={<PasswordResetFinishPage />} />
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    // Then: Locale should be set to Spanish in session storage
    await waitFor(() => {
      const locale = Storage.session.get('locale');
      expect(locale).toBe('es');
    });

    // And: setLocale action should be dispatched
    const actions = store.getActions();
    expect(actions).toContainEqual(
      expect.objectContaining({
        type: 'locale/setLocale/fulfilled',
        payload: 'es',
      }),
    );
  });

  it('should set locale to English when ?lang=en is in URL', async () => {
    // Given: URL with ?lang=en parameter
    const resetKey = 'test-reset-key';

    // When: PasswordResetFinishPage is rendered with lang=en
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/account/reset/finish?key=${resetKey}&lang=en`]}>
          <Routes>
            <Route path="/account/reset/finish" element={<PasswordResetFinishPage />} />
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    // Then: Locale should be set to English in session storage
    await waitFor(() => {
      const locale = Storage.session.get('locale');
      expect(locale).toBe('en');
    });

    // And: setLocale action should be dispatched
    const actions = store.getActions();
    expect(actions).toContainEqual(
      expect.objectContaining({
        type: 'locale/setLocale/fulfilled',
        payload: 'en',
      }),
    );
  });

  it('should NOT set locale when ?lang parameter is missing', async () => {
    // Given: URL without lang parameter
    const resetKey = 'test-reset-key';

    // When: PasswordResetFinishPage is rendered without lang parameter
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/account/reset/finish?key=${resetKey}`]}>
          <Routes>
            <Route path="/account/reset/finish" element={<PasswordResetFinishPage />} />
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    // Then: Locale should NOT be set in session storage
    await waitFor(() => {
      const locale = Storage.session.get('locale');
      // Should be undefined or null (not set)
      expect(locale).toBeFalsy();
    });
  });

  it('should NOT set locale when ?lang parameter is invalid', async () => {
    // Given: URL with invalid lang parameter (French)
    const resetKey = 'test-reset-key';

    // When: PasswordResetFinishPage is rendered with lang=fr (invalid)
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/account/reset/finish?key=${resetKey}&lang=fr`]}>
          <Routes>
            <Route path="/account/reset/finish" element={<PasswordResetFinishPage />} />
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    // Then: Locale should NOT be set in session storage
    await waitFor(() => {
      const locale = Storage.session.get('locale');
      // Should be undefined or null (not set)
      expect(locale).toBeFalsy();
    });

    // And: setLocale action should NOT be dispatched for invalid locale
    const actions = store.getActions();
    const setLocaleActions = actions.filter(action => action.type.startsWith('locale/setLocale'));
    expect(setLocaleActions).toHaveLength(0);
  });

  it('should render password reset form when key is provided', () => {
    // Given: URL with reset key
    const resetKey = 'test-reset-key-12345';

    // When: PasswordResetFinishPage is rendered with key
    const { container } = render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/account/reset/finish?key=${resetKey}`]}>
          <Routes>
            <Route path="/account/reset/finish" element={<PasswordResetFinishPage />} />
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    // Then: Password reset form should be rendered
    const newPasswordField = container.querySelector('[data-cy="resetPassword"]');
    const confirmPasswordField = container.querySelector('[data-cy="confirmResetPassword"]');
    const submitButton = container.querySelector('[data-cy="submit"]');

    expect(newPasswordField).toBeTruthy();
    expect(confirmPasswordField).toBeTruthy();
    expect(submitButton).toBeTruthy();
  });

  it('should NOT render password reset form when key is missing', () => {
    // Given: URL without reset key
    // When: PasswordResetFinishPage is rendered without key
    const { container } = render(
      <Provider store={store}>
        <MemoryRouter initialEntries={['/account/reset/finish']}>
          <Routes>
            <Route path="/account/reset/finish" element={<PasswordResetFinishPage />} />
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    // Then: Password reset form should NOT be rendered
    const newPasswordField = container.querySelector('[data-cy="resetPassword"]');
    const confirmPasswordField = container.querySelector('[data-cy="confirmResetPassword"]');

    expect(newPasswordField).toBeFalsy();
    expect(confirmPasswordField).toBeFalsy();
  });

  it('should handle both key and lang parameters together', async () => {
    // Given: URL with both key and lang parameters
    const resetKey = 'test-key-123';

    // When: PasswordResetFinishPage is rendered with both parameters
    const { container } = render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/account/reset/finish?key=${resetKey}&lang=en`]}>
          <Routes>
            <Route path="/account/reset/finish" element={<PasswordResetFinishPage />} />
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    // Then: Both password reset form and locale should be processed
    await waitFor(() => {
      // Locale should be set
      const locale = Storage.session.get('locale');
      expect(locale).toBe('en');

      // Password reset form should be rendered
      const newPasswordField = container.querySelector('[data-cy="resetPassword"]');
      expect(newPasswordField).toBeTruthy();
    });
  });

  describe('Navigation after successful password reset', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.runOnlyPendingTimers();
      jest.useRealTimers();
    });

    it('should navigate to home page after successful password reset', async () => {
      // Given: Store with successMessage indicating successful password reset
      const storeWithSuccess = mockStore({
        passwordReset: {
          resetPasswordSuccess: true,
          resetPasswordFailure: false,
          successMessage: 'reset.finish.messages.success',
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

      const resetKey = 'test-reset-key';

      // When: PasswordResetFinishPage is rendered with success state
      render(
        <Provider store={storeWithSuccess}>
          <MemoryRouter initialEntries={[`/account/reset/finish?key=${resetKey}`]}>
            <Routes>
              <Route path="/account/reset/finish" element={<PasswordResetFinishPage />} />
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
      // Given: Store without successMessage (no successful reset yet)
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

      const resetKey = 'test-reset-key';
      const initialLocation = `/account/reset/finish?key=${resetKey}`;

      // When: PasswordResetFinishPage is rendered without success state
      const { container } = render(
        <Provider store={storeWithoutSuccess}>
          <MemoryRouter initialEntries={[initialLocation]}>
            <Routes>
              <Route path="/account/reset/finish" element={<PasswordResetFinishPage />} />
              <Route path="/" element={<div data-testid="home-page">Home</div>} />
            </Routes>
          </MemoryRouter>
        </Provider>,
      );

      // Then: User should remain on password reset page
      // Fast-forward time to ensure no navigation occurs
      jest.advanceTimersByTime(1000);

      await waitFor(() => {
        // Password reset form should still be visible
        const newPasswordField = container.querySelector('[data-cy="resetPassword"]');
        expect(newPasswordField).toBeTruthy();

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
          successMessage: 'reset.finish.messages.success',
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

      const resetKey = 'test-reset-key';

      // When: PasswordResetFinishPage is rendered and then unmounted before timer fires
      const { unmount } = render(
        <Provider store={storeWithSuccess}>
          <MemoryRouter initialEntries={[`/account/reset/finish?key=${resetKey}`]}>
            <Routes>
              <Route path="/account/reset/finish" element={<PasswordResetFinishPage />} />
            </Routes>
          </MemoryRouter>
        </Provider>,
      );

      // Unmount before timer fires (before 500ms)
      unmount();

      // Then: Timer should be cleaned up and no navigation should occur
      // This test passes if no errors are thrown during cleanup
      expect(jest.getTimerCount()).toBe(0); // All timers should be cleared
    });
  });
});
