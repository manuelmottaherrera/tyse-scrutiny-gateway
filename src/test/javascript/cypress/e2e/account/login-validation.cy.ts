import { passwordLoginSelector, usernameLoginSelector } from '../../support/commands';

describe('Login Form Validation Messages (Issue #16)', () => {
  beforeEach(() => {
    cy.visit('');
  });

  describe('Validation messages in Spanish', () => {
    beforeEach(() => {
      // Set locale to Spanish
      cy.window().then(win => {
        win.localStorage.setItem('locale', 'es');
      });
      cy.reload();
      cy.clickOnLoginItem();
    });

    it('should show username validation error in Spanish when field is empty', () => {
      // When: User clicks submit without entering username
      cy.get(usernameLoginSelector).focus();
      cy.get(usernameLoginSelector).blur();

      // Then: Validation message should be in Spanish
      cy.get(usernameLoginSelector)
        .closest('.mb-3')
        .find('.invalid-feedback')
        .should('be.visible')
        .and('contain', 'El nombre de usuario no puede estar vacío');
    });

    it('should show password validation error in Spanish when field is empty', () => {
      // When: User clicks submit without entering password
      cy.get(passwordLoginSelector).focus();
      cy.get(passwordLoginSelector).blur();

      // Then: Validation message should be in Spanish
      cy.get(passwordLoginSelector)
        .closest('.mb-3')
        .find('.invalid-feedback')
        .should('be.visible')
        .and('contain', 'La contraseña no puede estar vacía');
    });

    it('should show both validation errors in Spanish', () => {
      // When: User tries to submit empty form
      cy.get(usernameLoginSelector).focus();
      cy.get(usernameLoginSelector).blur();
      cy.get(passwordLoginSelector).focus();
      cy.get(passwordLoginSelector).blur();

      // Then: Both validation messages should be in Spanish
      cy.get(usernameLoginSelector).closest('.mb-3').find('.invalid-feedback').should('be.visible').and('contain', 'usuario');

      cy.get(passwordLoginSelector).closest('.mb-3').find('.invalid-feedback').should('be.visible').and('contain', 'contraseña');
    });
  });

  describe('Validation messages in English', () => {
    beforeEach(() => {
      // Set locale to English
      cy.window().then(win => {
        win.localStorage.setItem('locale', 'en');
      });
      cy.reload();
      cy.clickOnLoginItem();
    });

    it('should show username validation error in English when field is empty', () => {
      // When: User clicks submit without entering username
      cy.get(usernameLoginSelector).focus();
      cy.get(usernameLoginSelector).blur();

      // Then: Validation message should be in English
      cy.get(usernameLoginSelector)
        .closest('.mb-3')
        .find('.invalid-feedback')
        .should('be.visible')
        .and('contain', 'Username cannot be empty');
    });

    it('should show password validation error in English when field is empty', () => {
      // When: User clicks submit without entering password
      cy.get(passwordLoginSelector).focus();
      cy.get(passwordLoginSelector).blur();

      // Then: Validation message should be in English
      cy.get(passwordLoginSelector)
        .closest('.mb-3')
        .find('.invalid-feedback')
        .should('be.visible')
        .and('contain', 'Password cannot be empty');
    });

    it('should show both validation errors in English', () => {
      // When: User tries to submit empty form
      cy.get(usernameLoginSelector).focus();
      cy.get(usernameLoginSelector).blur();
      cy.get(passwordLoginSelector).focus();
      cy.get(passwordLoginSelector).blur();

      // Then: Both validation messages should be in English
      cy.get(usernameLoginSelector).closest('.mb-3').find('.invalid-feedback').should('be.visible').and('contain', 'Username');

      cy.get(passwordLoginSelector).closest('.mb-3').find('.invalid-feedback').should('be.visible').and('contain', 'Password');
    });
  });

  describe('Language switching preserves validation behavior', () => {
    it('should update validation messages when language is changed', () => {
      // Given: User is on login page in Spanish
      cy.window().then(win => {
        win.localStorage.setItem('locale', 'es');
      });
      cy.reload();
      cy.clickOnLoginItem();

      // When: User triggers validation error
      cy.get(usernameLoginSelector).focus();
      cy.get(usernameLoginSelector).blur();

      // Then: Error should be in Spanish
      cy.get(usernameLoginSelector).closest('.mb-3').find('.invalid-feedback').should('contain', 'usuario');

      // When: User changes language to English
      cy.window().then(win => {
        win.localStorage.setItem('locale', 'en');
      });
      cy.reload();
      cy.clickOnLoginItem();
      cy.get(usernameLoginSelector).focus();
      cy.get(usernameLoginSelector).blur();

      // Then: Error should now be in English
      cy.get(usernameLoginSelector).closest('.mb-3').find('.invalid-feedback').should('contain', 'Username');
    });
  });
});
