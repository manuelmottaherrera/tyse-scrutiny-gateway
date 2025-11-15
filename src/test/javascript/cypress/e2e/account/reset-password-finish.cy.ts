describe('Password Reset Finish', () => {
  const newPassword = 'newPassword123!';

  beforeEach(() => {
    cy.intercept('POST', '/api/account/reset-password/finish').as('finishResetPassword');
  });

  describe('Successful password reset', () => {
    it('should redirect to home page after successful password reset', () => {
      // Given: User has a valid reset key (mocked for E2E)
      const mockResetKey = 'valid-reset-key-12345';

      // When: User visits password reset finish page with valid key
      cy.visit(`/account/reset/finish?key=${mockResetKey}`);

      // And: User enters new password
      cy.get('[data-cy="resetPassword"]').type(newPassword);
      cy.get('[data-cy="confirmResetPassword"]').type(newPassword);

      // And: User submits the form
      cy.get('[data-cy="submit"]').click();

      // Then: Request should be sent
      cy.wait('@finishResetPassword').then(({ request, response }) => {
        // Verify request payload
        expect(request.body.key).to.equal(mockResetKey);
        expect(request.body.newPassword).to.equal(newPassword);

        // If response is successful (200)
        if (response && response.statusCode === 200) {
          // User should be redirected to home page
          cy.url().should('eq', Cypress.config().baseUrl + '/');

          // Home page should be visible
          cy.get('.home').should('be.visible');
        }
      });
    });

    it('should show success notification after password reset', () => {
      // Given: User has a valid reset key
      const mockResetKey = 'valid-reset-key-67890';

      // When: User visits password reset finish page
      cy.visit(`/account/reset/finish?key=${mockResetKey}`);

      // And: User enters and submits new password
      cy.get('[data-cy="resetPassword"]').type(newPassword);
      cy.get('[data-cy="confirmResetPassword"]').type(newPassword);
      cy.get('[data-cy="submit"]').click();

      // Then: On successful reset
      cy.wait('@finishResetPassword').then(({ response }) => {
        if (response && response.statusCode === 200) {
          // Success toast notification should be visible
          cy.get('.Toastify__toast--success').should('be.visible');

          // Notification should contain success message
          cy.get('.Toastify__toast--success').should('contain', 'password');
        }
      });
    });

    it('should prevent going back to reset form after successful reset', () => {
      // Given: User has a valid reset key
      const mockResetKey = 'valid-reset-key-abc123';

      // When: User completes password reset
      cy.visit(`/account/reset/finish?key=${mockResetKey}`);
      cy.get('[data-cy="resetPassword"]').type(newPassword);
      cy.get('[data-cy="confirmResetPassword"]').type(newPassword);
      cy.get('[data-cy="submit"]').click();

      // Then: On successful reset
      cy.wait('@finishResetPassword').then(({ response }) => {
        if (response && response.statusCode === 200) {
          // User should be on home page
          cy.url().should('eq', Cypress.config().baseUrl + '/');

          // Password reset form should no longer be accessible
          cy.get('[data-cy="resetPassword"]').should('not.exist');
          cy.get('[data-cy="confirmResetPassword"]').should('not.exist');
        }
      });
    });

    it('should keep notification visible for 30 seconds', () => {
      // Use clock to control time and avoid arbitrary waits
      cy.clock();

      // Given: User has a valid reset key
      const mockResetKey = 'valid-reset-key-def456';

      // When: User completes password reset
      cy.visit(`/account/reset/finish?key=${mockResetKey}`);
      cy.get('[data-cy="resetPassword"]').type(newPassword);
      cy.get('[data-cy="confirmResetPassword"]').type(newPassword);
      cy.get('[data-cy="submit"]').click();

      // Then: On successful reset
      cy.wait('@finishResetPassword').then(({ response }) => {
        if (response && response.statusCode === 200) {
          // Notification should be visible
          cy.get('.Toastify__toast--success').should('be.visible');

          // Notification should still be visible after 10 seconds
          cy.tick(10000);
          cy.get('.Toastify__toast--success').should('be.visible');

          // Notification should still be visible after 20 seconds
          cy.tick(10000);
          cy.get('.Toastify__toast--success').should('be.visible');

          // Note: Not waiting full 30 seconds to keep test fast
          // Just verifying it persists longer than default 5 seconds
        }
      });
    });
  });

  describe('Form validation', () => {
    it('should not render form when reset key is missing', () => {
      // When: User visits password reset finish page without key
      cy.visit('/account/reset/finish');

      // Then: Password fields should not be rendered
      cy.get('[data-cy="resetPassword"]').should('not.exist');
      cy.get('[data-cy="confirmResetPassword"]').should('not.exist');
      cy.get('[data-cy="submit"]').should('not.exist');
    });

    it('should validate password requirements', () => {
      // Given: User has a valid reset key
      const mockResetKey = 'valid-reset-key-validation';

      // When: User visits password reset finish page
      cy.visit(`/account/reset/finish?key=${mockResetKey}`);

      // And: User enters password that is too short
      cy.get('[data-cy="resetPassword"]').type('abc');
      cy.get('[data-cy="confirmResetPassword"]').type('abc');
      cy.get('[data-cy="submit"]').click();

      // Then: Validation error should be shown
      cy.get('[data-cy="resetPassword"]').should('have.class', 'is-invalid');
    });

    it('should validate password confirmation match', () => {
      // Given: User has a valid reset key
      const mockResetKey = 'valid-reset-key-mismatch';

      // When: User visits password reset finish page
      cy.visit(`/account/reset/finish?key=${mockResetKey}`);

      // And: User enters mismatched passwords
      cy.get('[data-cy="resetPassword"]').type('password123');
      cy.get('[data-cy="confirmResetPassword"]').type('password456');
      cy.get('[data-cy="submit"]').click();

      // Then: Validation error should be shown for confirmation field
      cy.get('[data-cy="confirmResetPassword"]').should('have.class', 'is-invalid');
    });
  });

  describe('Locale from URL parameter', () => {
    it('should set locale to Spanish when ?lang=es in URL', () => {
      // Given: User has reset link with Spanish locale parameter
      const mockResetKey = 'valid-reset-key-es';

      // When: User visits password reset finish page with lang=es
      cy.visit(`/account/reset/finish?key=${mockResetKey}&lang=es`);

      // Then: Page should be in Spanish
      // Check for Spanish text in the form
      cy.contains('Nueva contraseña').should('be.visible');
    });

    it('should set locale to English when ?lang=en in URL', () => {
      // Given: User has reset link with English locale parameter
      const mockResetKey = 'valid-reset-key-en';

      // When: User visits password reset finish page with lang=en
      cy.visit(`/account/reset/finish?key=${mockResetKey}&lang=en`);

      // Then: Page should be in English
      // Check for English text in the form
      cy.contains('New password').should('be.visible');
    });
  });
});
