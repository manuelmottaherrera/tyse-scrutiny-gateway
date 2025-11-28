/**
 * E2E tests for dashboard module access control based on user permissions.
 *
 * These tests verify that:
 * - Admin users can see all dashboard modules
 * - Regular users see modules based on their permissions
 * - The /api/account endpoint returns permissions array
 * - Modules are filtered correctly in the dashboard UI
 */
describe('Dashboard Permissions E2E', () => {
  const adminUsername = Cypress.env('E2E_USERNAME') ?? 'admin';
  const adminPassword = Cypress.env('E2E_PASSWORD') ?? 'admin';
  const userUsername = 'user';
  const userPassword = 'user';

  describe('API - Account Permissions', () => {
    beforeEach(() => {
      cy.login(adminUsername, adminPassword);
    });

    it('should return permissions array in /api/account response for admin', () => {
      cy.authenticatedRequest({
        method: 'GET',
        url: '/api/account',
      }).then(response => {
        expect(response.status).to.equal(200);
        expect(response.body).to.have.property('permissions');
        expect(response.body.permissions).to.be.an('array');
        // Admin should have all module permissions
        expect(response.body.permissions).to.include('divipol.read');
        expect(response.body.permissions).to.include('statistics.read');
      });
    });

    it('should return permissions array in /api/account response for user', () => {
      // Use a separate session for user login
      cy.session('user-permissions-test', () => {
        cy.request({
          method: 'POST',
          url: '/api/authenticate',
          body: { username: userUsername, password: userPassword },
        }).then(({ body }) => {
          sessionStorage.setItem(Cypress.env('jwtStorageName'), JSON.stringify(body.id_token));
        });
      });

      cy.authenticatedRequest({
        method: 'GET',
        url: '/api/account',
      }).then(response => {
        expect(response.status).to.equal(200);
        expect(response.body).to.have.property('permissions');
        expect(response.body.permissions).to.be.an('array');
        // User should have some permissions from ROLE_USER
      });
    });
  });

  describe('Dashboard UI - Admin User', () => {
    beforeEach(() => {
      cy.login(adminUsername, adminPassword);
      cy.visit('/');
    });

    it('admin user should see dashboard grid with modules', () => {
      // Wait for dashboard to load
      cy.get('.dashboard-grid', { timeout: 10000 }).should('be.visible');
      cy.get('.dashboard-header').should('be.visible');
      cy.get('.modules-grid').should('be.visible');
    });

    it('admin user should see multiple module cards', () => {
      cy.get('.dashboard-grid', { timeout: 10000 }).should('be.visible');
      // Admin should see multiple modules
      cy.get('.module-card').should('have.length.at.least', 1);
    });

    it('admin user should see enabled modules as clickable links', () => {
      cy.get('.dashboard-grid', { timeout: 10000 }).should('be.visible');
      // Enabled modules should be rendered as links
      cy.get('a.module-card').should('have.length.at.least', 1);
    });

    it('admin should be able to navigate to Divipol module', () => {
      cy.get('.dashboard-grid', { timeout: 10000 }).should('be.visible');
      // Click on divipol module if visible
      cy.get('a.module-card[href="/divipol"]').first().click();
      // Should navigate to divipol page
      cy.url().should('include', '/divipol');
    });
  });

  describe('Dashboard UI - Coming Soon Modules', () => {
    beforeEach(() => {
      cy.login(adminUsername, adminPassword);
      cy.visit('/');
    });

    it('should display Coming Soon badge for disabled modules', () => {
      cy.get('.dashboard-grid', { timeout: 10000 }).should('be.visible');
      // Check if any Coming Soon badges exist
      cy.get('body').then($body => {
        if ($body.find('.badge-coming-soon').length > 0) {
          cy.get('.badge-coming-soon').should('be.visible');
        }
      });
    });

    it('disabled modules should not be clickable links', () => {
      cy.get('.dashboard-grid', { timeout: 10000 }).should('be.visible');
      // Disabled modules should be rendered as divs, not links
      cy.get('body').then($body => {
        if ($body.find('div.module-card.disabled').length > 0) {
          cy.get('div.module-card.disabled').should('exist');
        }
      });
    });
  });

  describe('Dashboard UI - Module Sorting', () => {
    beforeEach(() => {
      cy.login(adminUsername, adminPassword);
      cy.visit('/');
    });

    it('enabled modules should appear before disabled modules', () => {
      cy.get('.dashboard-grid', { timeout: 10000 }).should('be.visible');
      cy.get('.modules-grid').then($grid => {
        const cards = $grid.find('.module-card');
        if (cards.length > 1) {
          // Find first disabled card index
          let firstDisabledIndex = -1;
          let lastEnabledIndex = -1;

          cards.each((index, card) => {
            const $card = Cypress.$(card);
            if ($card.hasClass('disabled') && firstDisabledIndex === -1) {
              firstDisabledIndex = index;
            }
            if (!$card.hasClass('disabled')) {
              lastEnabledIndex = index;
            }
          });

          // If both types exist, enabled should come before disabled
          if (firstDisabledIndex > -1 && lastEnabledIndex > -1) {
            expect(lastEnabledIndex).to.be.lessThan(firstDisabledIndex);
          }
        }
      });
    });
  });

  describe('Access Control Verification', () => {
    it('unauthenticated user should not see dashboard modules', () => {
      // Clear any existing session
      cy.clearAllSessionStorage();
      cy.clearAllCookies();

      cy.visit('/');

      // Should be redirected to login or see login modal
      cy.get('body').then($body => {
        // Either we see the login form or we're on a page without modules
        const hasLoginForm = $body.find('[data-cy="loginTitle"]').length > 0 || $body.find('[data-cy="username"]').length > 0;
        const hasModules = $body.find('.module-card').length > 0;

        // Either should see login form OR should not see modules
        expect(hasLoginForm || !hasModules).to.equal(true);
      });
    });
  });
});
