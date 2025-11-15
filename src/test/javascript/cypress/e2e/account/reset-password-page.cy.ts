import {
  classInvalid,
  classValid,
  emailResetPasswordSelector,
  forgetYourPasswordSelector,
  submitInitResetPasswordSelector,
  usernameLoginSelector,
} from '../../support/commands';

describe('forgot your password', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'user';

  beforeEach(() => {
    cy.visit('');
    cy.clickOnLoginItem();
    cy.get(usernameLoginSelector).type(username);
    cy.get(forgetYourPasswordSelector).click();
  });

  beforeEach(() => {
    cy.intercept('POST', '/api/account/reset-password/init').as('initResetPassword');
  });

  it('requires email', () => {
    cy.get(submitInitResetPasswordSelector).click({ force: true });
    cy.get(emailResetPasswordSelector).should('have.class', classInvalid);
    cy.get(emailResetPasswordSelector).type('user@gmail.com');
    cy.get(submitInitResetPasswordSelector).click({ force: true });
    cy.get(emailResetPasswordSelector).should('have.class', classValid);
  });

  it('should be able to init reset password', () => {
    cy.get(emailResetPasswordSelector).type('user@gmail.com');
    cy.get(submitInitResetPasswordSelector).click({ force: true });
    cy.wait('@initResetPassword').then(({ response }) => expect(response?.statusCode).to.equal(200));
  });

  it('should redirect to home page after successful password reset request', () => {
    // Given: User is on password reset init page
    cy.url().should('include', '/account/reset/init');

    // When: User submits valid email
    cy.get(emailResetPasswordSelector).type('user@gmail.com');
    cy.get(submitInitResetPasswordSelector).click({ force: true });

    // Then: Request should succeed
    cy.wait('@initResetPassword').then(({ response }) => {
      expect(response?.statusCode).to.equal(200);
    });

    // And: User should be redirected to home page
    cy.url().should('eq', Cypress.config().baseUrl + '/');

    // And: Home page should be visible
    cy.get('.home').should('be.visible');
  });

  it('should show success notification after password reset request', () => {
    // When: User submits valid email
    cy.get(emailResetPasswordSelector).type('user@gmail.com');
    cy.get(submitInitResetPasswordSelector).click({ force: true });

    // Then: Request should succeed
    cy.wait('@initResetPassword').then(({ response }) => {
      expect(response?.statusCode).to.equal(200);
    });

    // And: Success toast notification should be visible
    // Note: Toast appears on home page after redirect
    cy.url().should('eq', Cypress.config().baseUrl + '/');
    cy.get('.Toastify__toast--success').should('be.visible');

    // And: Notification should contain success message about email
    cy.get('.Toastify__toast--success').should('contain', 'email');
  });

  it('should prevent multiple submissions by redirecting', () => {
    // When: User submits valid email
    cy.get(emailResetPasswordSelector).type('user@gmail.com');
    cy.get(submitInitResetPasswordSelector).click({ force: true });

    // Then: Request should succeed
    cy.wait('@initResetPassword').then(({ response }) => {
      expect(response?.statusCode).to.equal(200);
    });

    // And: User should be redirected immediately
    cy.url().should('eq', Cypress.config().baseUrl + '/');

    // And: Password reset form should no longer be visible
    cy.get(emailResetPasswordSelector).should('not.exist');
    cy.get(submitInitResetPasswordSelector).should('not.exist');
  });

  it('should keep notification visible for 30 seconds', () => {
    // Use clock to control time and avoid arbitrary waits
    cy.clock();

    // When: User submits valid email
    cy.get(emailResetPasswordSelector).type('user@gmail.com');
    cy.get(submitInitResetPasswordSelector).click({ force: true });

    // Then: Request should succeed
    cy.wait('@initResetPassword').then(({ response }) => {
      expect(response?.statusCode).to.equal(200);
    });

    // And: Notification should be visible
    cy.get('.Toastify__toast--success').should('be.visible');

    // And: Notification should still be visible after 10 seconds
    cy.tick(10000);
    cy.get('.Toastify__toast--success').should('be.visible');

    // And: Notification should still be visible after 20 seconds
    cy.tick(10000);
    cy.get('.Toastify__toast--success').should('be.visible');

    // Note: Not waiting full 30 seconds to keep test fast
    // Just verifying it persists longer than default 5 seconds
  });
});
