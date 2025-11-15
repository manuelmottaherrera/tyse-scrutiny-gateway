import { localeMenuSelector } from '../../support/commands';
import type { Account } from '../../support/account';

describe('Locale/i18n Functionality', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';

  describe('Language change in Header', () => {
    let originalUserAccount: Account;

    before(() => {
      cy.login(username, password);
      cy.getAccount().then(account => {
        originalUserAccount = account;
      });
    });

    after(() => {
      // Restore original language
      cy.login(username, password);
      cy.saveAccount(originalUserAccount).its('status').should('eq', 200);
    });

    it('authenticated user should be able to change language to English', () => {
      cy.login(username, password);
      cy.visit('/');

      // Intercept PATCH request to /api/account/locale
      cy.intercept('PATCH', '/api/account/locale').as('localeChange');

      // Click on language menu
      cy.get(localeMenuSelector).click();

      // Select English
      cy.get(localeMenuSelector).find('[value="en"]').click();

      // Wait for PATCH request and verify it succeeds
      cy.wait('@localeChange').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });

      // Verify language changed in account
      cy.getAccount().then(account => {
        expect(account.langKey).to.equal('en');
      });
    });

    it('authenticated user should be able to change language to Spanish', () => {
      cy.login(username, password);
      cy.visit('/');

      // Intercept PATCH request to /api/account/locale
      cy.intercept('PATCH', '/api/account/locale').as('localeChange');

      // Click on language menu
      cy.get(localeMenuSelector).click();

      // Select Spanish
      cy.get(localeMenuSelector).find('[value="es"]').click();

      // Wait for PATCH request and verify it succeeds
      cy.wait('@localeChange').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });

      // Verify language changed in account
      cy.getAccount().then(account => {
        expect(account.langKey).to.equal('es');
      });
    });

    it('should reject invalid language change request', () => {
      cy.login(username, password);

      // Try to change locale to invalid language (French)
      cy.authenticatedRequest({
        method: 'PATCH',
        url: '/api/account/locale',
        body: 'fr',
        headers: { 'Content-Type': 'text/plain' },
        failOnStatusCode: false,
      }).then(response => {
        expect(response.status).to.equal(400);
        expect(response.body.title).to.equal('Invalid language key');
        expect(response.body.detail).to.include('es');
        expect(response.body.detail).to.include('en');
      });
    });

    it('language change should persist after logout and login', () => {
      cy.login(username, password);
      cy.visit('/');

      // Change to English
      cy.intercept('PATCH', '/api/account/locale').as('localeChange');
      cy.get(localeMenuSelector).click();
      cy.get(localeMenuSelector).find('[value="en"]').click();
      cy.wait('@localeChange');

      // Logout
      cy.clickOnLogoutItem();
      cy.url().should('match', /\/$/);

      // Login again
      cy.login(username, password);
      cy.visit('/');

      // Verify language is still English
      cy.getAccount().then(account => {
        expect(account.langKey).to.equal('en');
      });
    });
  });

  describe('URL parameter ?lang= in account activation', () => {
    it('should set locale to Spanish when ?lang=es in activation URL', () => {
      // Visit activation page with lang=es (without valid key, just to test locale)
      cy.visit('/account/activate?key=invalid-key&lang=es');

      // Wait for useEffect to execute
      cy.wait(500);

      // Verify session storage has Spanish locale
      cy.window().then(win => {
        const locale = win.sessionStorage.getItem('locale');
        expect(locale).to.equal('"es"'); // Storage.session.set uses JSON.stringify
      });
    });

    it('should set locale to English when ?lang=en in activation URL', () => {
      // Visit activation page with lang=en
      cy.visit('/account/activate?key=invalid-key&lang=en');

      // Wait for useEffect to execute
      cy.wait(500);

      // Verify session storage has English locale
      cy.window().then(win => {
        const locale = win.sessionStorage.getItem('locale');
        expect(locale).to.equal('"en"'); // Storage.session.set uses JSON.stringify
      });
    });

    it('should NOT set locale when ?lang parameter is invalid', () => {
      // Clear session storage first
      cy.window().then(win => {
        win.sessionStorage.removeItem('locale');
      });

      // Visit activation page with invalid lang
      cy.visit('/account/activate?key=invalid-key&lang=fr');

      // Verify session storage does NOT have French locale
      cy.window().then(win => {
        const locale = win.sessionStorage.getItem('locale');
        expect(locale).to.not.equal('"fr"');
      });
    });
  });

  describe('URL parameter ?lang= in password reset', () => {
    it('should set locale to Spanish when ?lang=es in password reset URL', () => {
      // Visit password reset page with lang=es
      cy.visit('/account/reset/finish?key=invalid-key&lang=es');

      // Wait for useEffect to execute
      cy.wait(500);

      // Verify session storage has Spanish locale
      cy.window().then(win => {
        const locale = win.sessionStorage.getItem('locale');
        expect(locale).to.equal('"es"'); // Storage.session.set uses JSON.stringify
      });
    });

    it('should set locale to English when ?lang=en in password reset URL', () => {
      // Visit password reset page with lang=en
      cy.visit('/account/reset/finish?key=invalid-key&lang=en');

      // Wait for useEffect to execute
      cy.wait(500);

      // Verify session storage has English locale
      cy.window().then(win => {
        const locale = win.sessionStorage.getItem('locale');
        expect(locale).to.equal('"en"'); // Storage.session.set uses JSON.stringify
      });
    });

    it('should NOT set locale when ?lang parameter is invalid', () => {
      // Clear session storage first
      cy.window().then(win => {
        win.sessionStorage.removeItem('locale');
      });

      // Visit password reset page with invalid lang
      cy.visit('/account/reset/finish?key=invalid-key&lang=de');

      // Verify session storage does NOT have German locale
      cy.window().then(win => {
        const locale = win.sessionStorage.getItem('locale');
        expect(locale).to.not.equal('"de"');
      });
    });
  });

  describe('Backend error messages in user locale', () => {
    it('should receive error message in Spanish when X-Locale header is es', () => {
      cy.login(username, password);

      // Set session locale to Spanish
      cy.window().then(win => {
        win.sessionStorage.setItem('locale', '"es"');
      });

      // Try to change to invalid locale
      cy.authenticatedRequest({
        method: 'PATCH',
        url: '/api/account/locale',
        body: 'invalid',
        headers: {
          'Content-Type': 'text/plain',
          'X-Locale': 'es',
        },
        failOnStatusCode: false,
      }).then(response => {
        expect(response.status).to.equal(400);
        expect(response.body.title).to.equal('Clave de idioma inválida');
        expect(response.body.detail).to.include('solo');
      });
    });

    it('should receive error message in English when X-Locale header is en', () => {
      cy.login(username, password);

      // Set session locale to English
      cy.window().then(win => {
        win.sessionStorage.setItem('locale', '"en"');
      });

      // Try to change to invalid locale
      cy.authenticatedRequest({
        method: 'PATCH',
        url: '/api/account/locale',
        body: 'invalid',
        headers: {
          'Content-Type': 'text/plain',
          'X-Locale': 'en',
        },
        failOnStatusCode: false,
      }).then(response => {
        expect(response.status).to.equal(400);
        expect(response.body.title).to.equal('Invalid language key');
        expect(response.body.detail).to.include('Only');
      });
    });
  });

  describe('Unauthenticated user language change', () => {
    it('should allow unauthenticated user to change language in session only', () => {
      // Visit home page without authentication
      cy.visit('/');

      // Change language to English
      cy.get(localeMenuSelector).click();
      cy.get(localeMenuSelector).find('[value="en"]').click();

      // Verify session storage has English
      cy.window().then(win => {
        const locale = win.sessionStorage.getItem('locale');
        expect(locale).to.equal('"en"');
      });

      // Verify UI updated (check that some text changed to English)
      // For example, the login menu item should say "Sign in" instead of "Iniciar sesión"
      cy.get('[data-cy="accountMenu"]').click();
      cy.get('[data-cy="login"]').should('contain', 'Sign in');
    });
  });
});
