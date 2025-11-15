import React from 'react';
import { render, waitFor } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { Storage } from 'react-jhipster';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import axios from 'axios';

import ActivatePage from './activate';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

describe('ActivatePage', () => {
  let store;

  beforeEach(() => {
    // Mock axios response for activation
    mockedAxios.get = jest.fn().mockResolvedValue({ data: {} });

    // Initialize mock store
    store = mockStore({
      activate: {
        activationSuccess: false,
        activationFailure: false,
      },
      locale: {
        currentLocale: 'es',
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should set locale to Spanish when ?lang=es is in URL', async () => {
    // Given: URL with ?lang=es parameter
    const activationKey = 'test-activation-key';

    // When: ActivatePage is rendered with lang=es
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/activate?key=${activationKey}&lang=es`]}>
          <Routes>
            <Route path="/activate" element={<ActivatePage />} />
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
        type: 'locale/setLocale',
        payload: 'es',
      }),
    );
  });

  it('should set locale to English when ?lang=en is in URL', async () => {
    // Given: URL with ?lang=en parameter
    const activationKey = 'test-activation-key';

    // When: ActivatePage is rendered with lang=en
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/activate?key=${activationKey}&lang=en`]}>
          <Routes>
            <Route path="/activate" element={<ActivatePage />} />
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
        type: 'locale/setLocale',
        payload: 'en',
      }),
    );
  });

  it('should NOT set locale when ?lang parameter is missing', async () => {
    // Given: URL without lang parameter
    const activationKey = 'test-activation-key';

    // When: ActivatePage is rendered without lang parameter
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/activate?key=${activationKey}`]}>
          <Routes>
            <Route path="/activate" element={<ActivatePage />} />
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
    const activationKey = 'test-activation-key';

    // When: ActivatePage is rendered with lang=fr (invalid)
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/activate?key=${activationKey}&lang=fr`]}>
          <Routes>
            <Route path="/activate" element={<ActivatePage />} />
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
    const setLocaleActions = actions.filter(action => action.type === 'locale/setLocale');
    expect(setLocaleActions).toHaveLength(0);
  });

  it('should call activation API with key parameter', async () => {
    // Given: URL with activation key
    const activationKey = 'test-activation-key-12345';

    // When: ActivatePage is rendered
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/activate?key=${activationKey}`]}>
          <Routes>
            <Route path="/activate" element={<ActivatePage />} />
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    // Then: Activation action should be dispatched
    await waitFor(() => {
      const actions = store.getActions();
      expect(actions).toContainEqual(
        expect.objectContaining({
          type: expect.stringContaining('activate/activateAction'),
        }),
      );
    });
  });

  it('should handle both key and lang parameters together', async () => {
    // Given: URL with both key and lang parameters
    const activationKey = 'test-key-123';

    // When: ActivatePage is rendered with both parameters
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/activate?key=${activationKey}&lang=en`]}>
          <Routes>
            <Route path="/activate" element={<ActivatePage />} />
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    // Then: Both activation and locale should be processed
    await waitFor(() => {
      // Locale should be set
      const locale = Storage.session.get('locale');
      expect(locale).toBe('en');

      // Activation action should be dispatched
      const actions = store.getActions();
      expect(actions).toContainEqual(
        expect.objectContaining({
          type: expect.stringContaining('activate/activateAction'),
        }),
      );
    });
  });
});
