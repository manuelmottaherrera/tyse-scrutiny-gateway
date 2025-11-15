import axios from 'axios';
import sinon from 'sinon';
import { Storage } from 'react-jhipster';

import setupAxiosInterceptors from './axios-interceptor';

describe('Axios Interceptor', () => {
  describe('setupAxiosInterceptors', () => {
    const client = axios;
    const onUnauthenticated = sinon.spy();
    setupAxiosInterceptors(onUnauthenticated);

    it('onRequestSuccess is called on fulfilled request', () => {
      expect((client.interceptors.request as any).handlers[0].fulfilled({ data: 'foo', url: '/test' })).toMatchObject({
        data: 'foo',
      });
    });

    it('onRequestSuccess adds X-Locale header with session locale', () => {
      // Given: Spanish locale in session storage
      Storage.session.set('locale', 'es');

      // When: Request is intercepted
      const config = { headers: {}, url: '/api/test' };
      const result = (client.interceptors.request as any).handlers[0].fulfilled(config);

      // Then: X-Locale header is added with Spanish
      expect(result.headers['X-Locale']).toBe('es');
    });

    it('onRequestSuccess adds X-Locale header with English locale', () => {
      // Given: English locale in session storage
      Storage.session.set('locale', 'en');

      // When: Request is intercepted
      const config = { headers: {}, url: '/api/test' };
      const result = (client.interceptors.request as any).handlers[0].fulfilled(config);

      // Then: X-Locale header is added with English
      expect(result.headers['X-Locale']).toBe('en');
    });

    it('onRequestSuccess defaults X-Locale to es when no locale in storage', () => {
      // Given: No locale in session storage
      Storage.session.remove('locale');

      // When: Request is intercepted
      const config = { headers: {}, url: '/api/test' };
      const result = (client.interceptors.request as any).handlers[0].fulfilled(config);

      // Then: X-Locale header defaults to Spanish
      expect(result.headers['X-Locale']).toBe('es');
    });

    it('onResponseSuccess is called on fulfilled response', () => {
      expect((client.interceptors.response as any).handlers[0].fulfilled({ data: 'foo' })).toEqual({ data: 'foo' });
    });
    it('onResponseError is called on rejected response', () => {
      const rejectError = {
        response: {
          statusText: 'NotFound',
          status: 401,
          data: { message: 'Page not found' },
        },
      };
      expect((client.interceptors.response as any).handlers[0].rejected(rejectError)).rejects.toEqual(rejectError);
      expect(onUnauthenticated.calledOnce).toBe(true);
    });
  });
});
