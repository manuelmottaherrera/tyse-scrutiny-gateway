/**
 * E2E tests for Divipol Export functionality
 */
describe('Divipol Export', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'admin';
  const password = Cypress.env('E2E_PASSWORD') ?? 'admin';

  beforeEach(() => {
    cy.login(username, password);
    // Setup intercept before visiting
    cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/departamentos').as('getDepartamentos');
    cy.visit('/divipol');
    cy.wait('@getDepartamentos');
  });

  describe('Export Component Visibility', () => {
    it('should display the export dropdown when data is available', () => {
      cy.get('.divipol-export').should('be.visible');
      cy.get('.divipol-export .dropdown-toggle').should('be.visible');
    });

    it('should show CSV and PDF options in dropdown', () => {
      cy.get('.divipol-export .dropdown-toggle').click();
      cy.get('.divipol-export .dropdown-menu').should('be.visible');
      cy.get('.divipol-export .dropdown-item').should('have.length', 2);
      cy.get('.divipol-export .dropdown-item').first().should('contain.text', 'CSV');
      cy.get('.divipol-export .dropdown-item').last().should('contain.text', 'PDF');
    });
  });

  describe('Export in Filters Mode - CSV', () => {
    it('should download CSV file with all departamentos', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/export/filters/csv*').as('exportCsv');

      // Click export dropdown and select CSV
      cy.get('.divipol-export .dropdown-toggle').click();
      cy.get('.divipol-export .dropdown-item').contains('CSV').click();

      // Wait for API response
      cy.wait('@exportCsv').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
        expect(response?.headers['content-type']).to.include('text/csv');
      });
    });

    it('should download CSV with filtered data (by departamento)', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/municipios*').as('getMunicipios');
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/export/filters/csv*').as('exportCsv');

      // Select a departamento
      cy.get('#departamento').select(1);
      cy.wait('@getMunicipios');

      // Export CSV
      cy.get('.divipol-export .dropdown-toggle').click();
      cy.get('.divipol-export .dropdown-item').contains('CSV').click();

      // Verify API was called with codDepto param
      cy.wait('@exportCsv').then(({ request, response }) => {
        expect(response?.statusCode).to.equal(200);
        expect(request.url).to.include('codDepto=');
      });
    });
  });

  describe('Export in Filters Mode - PDF', () => {
    it('should download PDF file with all departamentos', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/export/filters/pdf*').as('exportPdf');

      // Click export dropdown and select PDF
      cy.get('.divipol-export .dropdown-toggle').click();
      cy.get('.divipol-export .dropdown-item').contains('PDF').click();

      // Wait for API response
      cy.wait('@exportPdf').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
        expect(response?.headers['content-type']).to.include('application/pdf');
      });
    });

    it('should show spinner while exporting', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/export/filters/pdf*', req => {
        req.on('response', res => {
          res.setDelay(500);
        });
      }).as('exportPdfDelayed');

      cy.get('.divipol-export .dropdown-toggle').click();
      cy.get('.divipol-export .dropdown-item').contains('PDF').click();

      // Verify spinner is shown
      cy.get('.divipol-export .spinner-border').should('be.visible');
      cy.get('.divipol-export .dropdown-toggle').should('be.disabled');

      cy.wait('@exportPdfDelayed');

      // Spinner should be gone after export completes
      cy.get('.divipol-export .spinner-border').should('not.exist');
    });
  });

  describe('Export in Search Mode', () => {
    beforeEach(() => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');
    });

    it('should export search results in search mode', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/export/search/csv*').as('exportSearchCsv');

      // Perform a search
      cy.get('.divipol-search .form-control').type('BOGOTA');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Click export dropdown and select CSV
      cy.get('.divipol-export .dropdown-toggle').click();
      cy.get('.divipol-export .dropdown-item').contains('CSV').click();

      // Handle modal if it appears (multiple pages) or direct export (single page)
      cy.get('body').then($body => {
        if ($body.find('.modal').length > 0) {
          // Modal appeared - click download button
          cy.get('.modal .btn-primary').click();
        }
        // Wait for export API call
        cy.wait('@exportSearchCsv').then(({ request, response }) => {
          expect(response?.statusCode).to.equal(200);
          expect(request.url).to.include('q=BOGOTA');
          expect(request.url).to.include('mode=name');
        });
      });
    });

    it('should open modal when search has multiple pages', () => {
      // Search for a common term that returns many results
      cy.get('.divipol-search .form-control').type('ESCUELA');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Check if there are multiple pages
      cy.get('.divipol-search-results').then($results => {
        if ($results.find('.pagination-controls').length > 0) {
          // Click export
          cy.get('.divipol-export .dropdown-toggle').click();
          cy.get('.divipol-export .dropdown-item').contains('CSV').click();

          // Modal should appear
          cy.get('.modal').should('be.visible');
          cy.get('.modal-header').should('contain.text', 'Exportar');
        }
      });
    });
  });

  describe('Export Modal', () => {
    beforeEach(() => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');

      // Search for something that returns many results to trigger modal
      cy.get('.divipol-search .form-control').type('COLEGIO');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');
    });

    it('should display format options (CSV and PDF)', () => {
      cy.get('.divipol-search-results').then($results => {
        if ($results.find('.pagination-controls').length > 0) {
          cy.get('.divipol-export .dropdown-toggle').click();
          cy.get('.divipol-export .dropdown-item').first().click();

          // Check format radio buttons
          cy.get('.modal input[value="csv"]').should('exist');
          cy.get('.modal input[value="pdf"]').should('exist');
          cy.get('.modal input[value="csv"]').should('be.checked');
        }
      });
    });

    it('should display scope options (current page vs all)', () => {
      cy.get('.divipol-search-results').then($results => {
        if ($results.find('.pagination-controls').length > 0) {
          cy.get('.divipol-export .dropdown-toggle').click();
          cy.get('.divipol-export .dropdown-item').first().click();

          // Check scope radio buttons
          cy.get('.modal input[value="currentPage"]').should('exist');
          cy.get('.modal input[value="all"]').should('exist');
          cy.get('.modal input[value="currentPage"]').should('be.checked');
        }
      });
    });

    it('should export all results when "all" scope is selected', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/export/search/csv*').as('exportSearchCsv');

      cy.get('.divipol-search-results').then($results => {
        if ($results.find('.pagination-controls').length > 0) {
          cy.get('.divipol-export .dropdown-toggle').click();
          cy.get('.divipol-export .dropdown-item').first().click();

          // Select "all" scope
          cy.get('.modal input[value="all"]').click();

          // Click download
          cy.get('.modal .btn-primary').click();

          cy.wait('@exportSearchCsv').then(({ request }) => {
            expect(request.url).to.include('exportAll=true');
          });
        }
      });
    });

    it('should close modal when cancel is clicked', () => {
      cy.get('.divipol-search-results').then($results => {
        if ($results.find('.pagination-controls').length > 0) {
          cy.get('.divipol-export .dropdown-toggle').click();
          cy.get('.divipol-export .dropdown-item').first().click();

          cy.get('.modal').should('be.visible');
          cy.get('.modal .btn-secondary').click();
          cy.get('.modal').should('not.exist');
        }
      });
    });
  });

  describe('Export by Code Search Mode', () => {
    it('should export search results by code', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/search*').as('searchApi');
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/export/search/csv*').as('exportSearchCsv');

      // Switch to code mode
      cy.get('.search-mode-toggle .form-check-input').click();

      // Search by code (using a more specific code to limit results)
      cy.get('.code-hidden-input').type('05001001');
      cy.get('.search-actions .btn-primary').click();
      cy.wait('@searchApi');

      // Click export dropdown and select CSV
      cy.get('.divipol-export .dropdown-toggle').click();
      cy.get('.divipol-export .dropdown-item').contains('CSV').click();

      // Handle modal if it appears (multiple pages) or direct export (single page)
      cy.get('body').then($body => {
        if ($body.find('.modal').length > 0) {
          // Modal appeared - click download button
          cy.get('.modal .btn-primary').click();
        }
        // Wait for export API call
        cy.wait('@exportSearchCsv').then(({ request, response }) => {
          expect(response?.statusCode).to.equal(200);
          expect(request.url).to.include('q=05001001');
          expect(request.url).to.include('mode=code');
        });
      });
    });
  });

  describe('Toast Notifications', () => {
    it('should show success toast after successful export', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/export/filters/csv*').as('exportCsv');

      cy.get('.divipol-export .dropdown-toggle').click();
      cy.get('.divipol-export .dropdown-item').contains('CSV').click();

      cy.wait('@exportCsv');

      // Verify success toast
      cy.get('.Toastify__toast--success', { timeout: 10000 }).should('be.visible');
    });

    it('should show error toast when export fails', () => {
      cy.intercept('GET', '/services/tysescrutinymicrodivipol/api/divipol/export/filters/csv*', {
        statusCode: 500,
        body: { message: 'Internal Server Error' },
      }).as('exportCsvError');

      cy.get('.divipol-export .dropdown-toggle').click();
      cy.get('.divipol-export .dropdown-item').contains('CSV').click();

      cy.wait('@exportCsvError');

      // Verify error toast
      cy.get('.Toastify__toast--error', { timeout: 10000 }).should('be.visible');
    });
  });
});
