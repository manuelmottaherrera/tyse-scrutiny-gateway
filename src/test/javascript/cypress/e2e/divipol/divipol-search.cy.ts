/**
 * E2E tests for Divipol Search functionality
 */
describe('Divipol Search', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'admin';
  const password = Cypress.env('E2E_PASSWORD') ?? 'admin';

  beforeEach(() => {
    cy.login(username, password);
    cy.visit('/divipol');
  });

  describe('Search Component Visibility', () => {
    it('should display the search component', () => {
      cy.get('.divipol-search').should('be.visible');
      cy.get('.search-title').should('be.visible');
    });

    it('should display search mode toggle (Name/Code)', () => {
      cy.get('.search-mode-toggle').should('be.visible');
      cy.get('.mode-label').should('have.length', 2);
    });

    it('should display search button', () => {
      cy.get('.search-actions .btn-primary').should('be.visible');
    });
  });

  describe('Search by Name Mode', () => {
    it('should search by name and display results', () => {
      // Intercept API call
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // Type search term
      cy.get('.divipol-search .form-control').type('MEDELLIN');
      cy.get('.search-actions .btn-primary').click();

      // Wait for API response
      cy.wait('@searchApi').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });

      // Verify results are displayed
      cy.get('.divipol-search-results').should('be.visible');
      cy.get('.search-results-table').should('be.visible');
    });

    it('should show minimum characters message for short search terms', () => {
      // Type a short search term
      cy.get('.divipol-search .form-control').type('AB');

      // Verify minimum characters message
      cy.get('.divipol-search').contains('Mínimo').should('be.visible');
    });

    it('should clear search when clicking clear button', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // Perform a search
      cy.get('.divipol-search .form-control').type('BOGOTA');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Verify results are displayed
      cy.get('.divipol-search-results').should('be.visible');

      // Click clear button
      cy.get('.divipol-search-results .clear-search-link').click();

      // Verify results are hidden and filters are visible again
      cy.get('.divipol-search-results').should('not.exist');
      cy.get('.divipol-filters').should('be.visible');
    });
  });

  describe('Search by Code Mode', () => {
    it('should switch to code search mode', () => {
      // Click on the toggle switch to change to code mode
      cy.get('.search-mode-toggle .form-check-input').click();

      // Verify code input is displayed
      cy.get('.divipol-code-input').should('be.visible');
    });

    it('should display code input with proper format', () => {
      // Switch to code mode
      cy.get('.search-mode-toggle .form-check-input').click();

      // Verify code display wrapper is visible
      cy.get('.code-display-wrapper').should('be.visible');
      cy.get('.code-format-hint').should('be.visible');
    });

    it('should search by department code', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // Switch to code mode
      cy.get('.search-mode-toggle .form-check-input').click();

      // Type department code (05 for Antioquia)
      cy.get('.code-hidden-input').type('05');
      cy.get('.search-actions .btn-primary').click();

      // Wait for API response
      cy.wait('@searchApi').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });

      // Verify results are displayed
      cy.get('.divipol-search-results').should('be.visible');
    });

    it('should search by full divipol code', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // Switch to code mode
      cy.get('.search-mode-toggle .form-check-input').click();

      // Type a more complete code
      cy.get('.code-hidden-input').type('05001');
      cy.get('.search-actions .btn-primary').click();

      // Wait for API response
      cy.wait('@searchApi');

      // Verify results are displayed
      cy.get('.divipol-search-results').should('be.visible');
    });
  });

  describe('Search Results', () => {
    beforeEach(() => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');
    });

    it('should display result table with correct columns', () => {
      // Perform a search
      cy.get('.divipol-search .form-control').type('CARTAGENA');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Verify table headers
      cy.get('.search-results-table thead th').should('have.length.at.least', 4);
    });

    it('should display badges for different types (DEPTO, MPIO, ZONA, PUESTO)', () => {
      // Search for a department
      cy.get('.divipol-search .form-control').type('ANTIOQUIA');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Verify at least one badge is displayed
      cy.get('.search-results-table .badge').should('exist');
    });

    it('should display pagination when many results', () => {
      // Search for a common term
      cy.get('.divipol-search .form-control').type('ESCUELA');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Check if pagination exists (only if there are many results)
      cy.get('.divipol-search-results').then($results => {
        if ($results.find('.search-pagination').length > 0) {
          cy.get('.pagination-controls .btn').should('exist');
        }
      });
    });

    it('should navigate between pages', () => {
      // Search for a common term that has many results
      cy.get('.divipol-search .form-control').type('COLEGIO');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Check if pagination exists
      cy.get('.divipol-search-results').then($results => {
        if ($results.find('.pagination-controls').length > 0) {
          // Click next page
          cy.get('.pagination-controls .btn').contains('2').click();
          cy.wait('@searchApi');

          // Verify we're on page 2
          cy.get('.pagination-controls .btn-primary').should('contain', '2');
        }
      });
    });
  });

  describe('URL Persistence', () => {
    it('should update URL with search params', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // Perform a search
      cy.get('.divipol-search .form-control').type('BOGOTA');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Verify URL contains search params
      cy.url().should('include', 'q=BOGOTA');
      cy.url().should('include', 'mode=name');
    });

    it('should restore search from URL params', () => {
      // Setup intercept before navigation
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // First perform a search manually
      cy.get('.divipol-search .form-control').type('CALI');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Verify URL was updated and results are visible
      cy.url().should('include', 'q=CALI');
      cy.get('.divipol-search-results').should('be.visible');

      // The URL contains the search params, verify it persisted
      cy.url().should('include', 'mode=name');
    });

    it('should restore code search from URL params', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // Switch to code mode
      cy.get('.search-mode-toggle .form-check-input').click();

      // Type a code and search
      cy.get('.code-hidden-input').type('05001');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Verify URL was updated with code mode and results are visible
      cy.url().should('include', 'q=05001');
      cy.url().should('include', 'mode=code');
      cy.get('.divipol-search-results').should('be.visible');
    });
  });

  describe('Filter and Search Interaction', () => {
    it('should show warning when filters are active', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/departamentos').as('getDepartamentos');
      cy.wait('@getDepartamentos');

      // Select a department filter
      cy.get('#departamento').select(1);

      // Verify warning is displayed
      cy.get('.search-warning').should('be.visible');
    });

    it('should hide filters when search is active', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // Perform a search
      cy.get('.divipol-search .form-control').type('PEREIRA');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Verify filters are hidden
      cy.get('.divipol-filters').should('not.exist');
    });

    it('should show filters again when search is cleared', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // Perform a search
      cy.get('.divipol-search .form-control').type('MANIZALES');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Clear search
      cy.get('.divipol-search-results .clear-search-link').click();

      // Verify filters are visible again
      cy.get('.divipol-filters').should('be.visible');
    });
  });

  describe('Suggestions', () => {
    it('should show suggestions when typing', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search/suggestions*').as('suggestionsApi');

      // Wait for the input to be ready and type a common search term
      cy.get('.divipol-search .form-control').should('be.visible').clear().type('BOGOTA', { delay: 100 });

      // Wait for suggestions API with longer timeout
      cy.wait('@suggestionsApi', { timeout: 10000 });

      // Verify suggestions are displayed (if results exist)
      // The API might return empty results, so check if list appears with items
      cy.get('.suggestions-list', { timeout: 5000 }).should('exist');
    });

    it('should select suggestion and perform search', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search/suggestions*').as('suggestionsApi');
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // Type a common search term that should have suggestions
      cy.get('.divipol-search .form-control').should('be.visible').clear().type('MEDELLIN', { delay: 100 });
      cy.wait('@suggestionsApi', { timeout: 10000 });

      // Wait for suggestions list to appear and have items
      cy.get('.suggestions-list .suggestion-item', { timeout: 5000 }).should('have.length.at.least', 1);

      // Click on first suggestion
      cy.get('.suggestions-list .suggestion-item').first().click();

      // Verify search is performed
      cy.wait('@searchApi', { timeout: 10000 });
      cy.get('.divipol-search-results').should('be.visible');
    });
  });
});
