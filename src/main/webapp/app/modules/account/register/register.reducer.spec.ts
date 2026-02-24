import axios from 'axios';
import sinon from 'sinon';
import { configureStore } from '@reduxjs/toolkit';
import { TranslatorContext } from 'react-jhipster';

import register, { handleRegister, reset } from './register.reducer';

describe('Creating account tests', () => {
  const initialState = {
    loading: false,
    registrationSuccess: false,
    registrationFailure: false,
    errorMessage: null,
    successMessage: null,
  };

  beforeAll(() => {
    TranslatorContext.registerTranslations('es', {});
  });

  it('should return the initial state', () => {
    expect(register(undefined, { type: '' })).toEqual({
      ...initialState,
    });
  });

  it('should detect a request', () => {
    expect(register(undefined, { type: handleRegister.pending.type })).toEqual({
      ...initialState,
      loading: true,
    });
  });

  it('should handle RESET', () => {
    expect(
      register({ loading: true, registrationSuccess: true, registrationFailure: true, errorMessage: '', successMessage: '' }, reset()),
    ).toEqual({
      ...initialState,
    });
  });

  it('should handle CREATE_ACCOUNT success', () => {
    expect(
      register(undefined, {
        type: handleRegister.fulfilled.type,
        payload: 'fake payload',
      }),
    ).toEqual({
      ...initialState,
      registrationSuccess: true,
      successMessage: 'register.messages.success',
    });
  });

  it('should set loading to false on fulfilled', () => {
    const pendingState = { ...initialState, loading: true };
    expect(
      register(pendingState, {
        type: handleRegister.fulfilled.type,
        payload: 'fake payload',
      }),
    ).toMatchObject({
      loading: false,
      registrationSuccess: true,
    });
  });

  it('should set loading to false on rejected', () => {
    const pendingState = { ...initialState, loading: true };
    const error = { message: 'fake error' };
    expect(
      register(pendingState, {
        type: handleRegister.rejected.type,
        error,
      }),
    ).toMatchObject({
      loading: false,
      registrationFailure: true,
    });
  });

  it('should handle CREATE_ACCOUNT failure', () => {
    const error = { message: 'fake error' };
    expect(
      register(undefined, {
        type: handleRegister.rejected.type,
        error,
      }),
    ).toEqual({
      ...initialState,
      registrationFailure: true,
      errorMessage: error.message,
    });
  });

  describe('Actions', () => {
    let store;

    const resolvedObject = { value: 'whatever' };
    const getState = jest.fn();
    const dispatch = jest.fn();
    const extra = {};
    beforeEach(() => {
      store = configureStore({
        reducer: (state = [], action) => [...state, action],
      });
      axios.post = sinon.stub().returns(Promise.resolve(resolvedObject));
    });

    it('dispatches CREATE_ACCOUNT_PENDING and CREATE_ACCOUNT_FULFILLED actions', async () => {
      const arg = { login: '', email: '', password: '' };

      const result = await handleRegister(arg)(dispatch, getState, extra);

      const pendingAction = dispatch.mock.calls[0][0];
      expect(pendingAction.meta.requestStatus).toBe('pending');
      expect(handleRegister.fulfilled.match(result)).toBe(true);
      expect(result.payload).toBe(resolvedObject);
    });
    it('dispatches RESET actions', async () => {
      await store.dispatch(reset());
      expect(store.getState()).toEqual([expect.any(Object), expect.objectContaining(reset())]);
    });
  });
});
