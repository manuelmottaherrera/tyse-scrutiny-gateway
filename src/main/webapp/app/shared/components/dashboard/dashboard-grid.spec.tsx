import React from 'react';
import { MemoryRouter } from 'react-router';
import { render } from '@testing-library/react';
import { TranslatorContext } from 'react-jhipster';
import configureStore from 'redux-mock-store';
import { Provider } from 'react-redux';

import { DashboardGrid } from './dashboard-grid';

describe('DashboardGrid component', () => {
  const mockStore = configureStore();

  beforeAll(() => {
    TranslatorContext.registerTranslations('es', {
      'dashboardGrid.title': 'Panel Principal',
      'dashboardGrid.description': 'Seleccione un módulo para comenzar',
      'dashboardGrid.items.divipol.title': 'Divipol',
      'dashboardGrid.items.divipol.description': 'Gestión de división política',
      'dashboardGrid.items.statistics.title': 'Estadísticas',
      'dashboardGrid.items.statistics.description': 'Reportes estadísticos',
      'dashboardGrid.items.heatMap.title': 'Mapa de Calor',
      'dashboardGrid.items.heatMap.description': 'Visualización geográfica',
      'dashboardGrid.items.voteCount.title': 'Conteo de Votos',
      'dashboardGrid.items.voteCount.description': 'Sistema de conteo',
      'dashboardGrid.general.comingSoon': 'Próximamente',
    });
  });

  const renderWithProviders = (permissions: string[] = []) => {
    const store = mockStore({
      authentication: {
        account: {
          permissions,
        },
      },
    });

    return render(
      <Provider store={store}>
        <MemoryRouter>
          <DashboardGrid />
        </MemoryRouter>
      </Provider>,
    );
  };

  it('should display modules user has permissions for', () => {
    // Given: User has divipol.read and statistics.read permissions
    const { container } = renderWithProviders(['divipol.read', 'statistics.read']);

    // Then: Both modules should be visible
    const moduleCards = container.querySelectorAll('.module-card');
    expect(moduleCards.length).toBe(2);
  });

  it('should hide modules user lacks permissions for', () => {
    // Given: User only has divipol.read permission
    const { container } = renderWithProviders(['divipol.read']);

    // Then: Only divipol should be visible
    const moduleCards = container.querySelectorAll('.module-card');
    expect(moduleCards.length).toBe(1);
  });

  it('should show empty grid when user has no permissions', () => {
    // Given: User has no permissions
    const { container } = renderWithProviders([]);

    // Then: No module cards should be visible
    const moduleCards = container.querySelectorAll('.module-card');
    expect(moduleCards.length).toBe(0);

    // But the dashboard header should still be visible
    const header = container.querySelector('.dashboard-header');
    expect(header).toBeTruthy();
  });

  it('should handle undefined permissions gracefully', () => {
    // Given: Store has account without permissions field
    const store = mockStore({
      authentication: {
        account: {
          // permissions is undefined
        },
      },
    });

    // When: Render component
    const { container } = render(
      <Provider store={store}>
        <MemoryRouter>
          <DashboardGrid />
        </MemoryRouter>
      </Provider>,
    );

    // Then: Should not crash and show no modules
    const moduleCards = container.querySelectorAll('.module-card');
    expect(moduleCards.length).toBe(0);
  });

  it('should handle null account gracefully', () => {
    // Given: Store has null account
    const store = mockStore({
      authentication: {
        account: null,
      },
    });

    // When: Render component
    const { container } = render(
      <Provider store={store}>
        <MemoryRouter>
          <DashboardGrid />
        </MemoryRouter>
      </Provider>,
    );

    // Then: Should not crash and show no modules
    const moduleCards = container.querySelectorAll('.module-card');
    expect(moduleCards.length).toBe(0);
  });

  it('should sort enabled modules before disabled modules', () => {
    // Given: User has permissions for both enabled and disabled modules
    // heatmap.read is for a disabled (coming soon) module
    // divipol.read is for an enabled module
    const { container } = renderWithProviders(['heatmap.read', 'divipol.read']);

    // Then: Two modules should be visible
    const moduleCards = container.querySelectorAll('.module-card');
    expect(moduleCards.length).toBe(2);

    // Verify that enabled modules appear before disabled modules
    // by checking that a <a> or <Link> element (enabled) comes before <div> (disabled)
    const enabledModules = container.querySelectorAll('a.module-card');
    const disabledModules = container.querySelectorAll('div.module-card.disabled');

    expect(enabledModules.length).toBe(1); // divipol is enabled
    expect(disabledModules.length).toBe(1); // heatMap is disabled

    // The enabled module should be first in the grid (check DOM order)
    const modulesGrid = container.querySelector('.modules-grid');
    const firstChild = modulesGrid?.firstElementChild;
    expect(firstChild?.tagName).toBe('A'); // Enabled modules render as <a> or <Link>
    expect(firstChild?.classList.contains('disabled')).toBe(false);
  });

  it('should display all accessible modules for admin-like permissions', () => {
    // Given: User has permissions for all modules
    const allPermissions = [
      'divipol.read',
      'statistics.read',
      'heatmap.read',
      'votecount.read',
      'fileupload.read',
      'votingjuries.read',
      'trainings.read',
      'witnesses.read',
      'analytics.read',
    ];

    const { container } = renderWithProviders(allPermissions);

    // Then: All 9 modules should be visible
    const moduleCards = container.querySelectorAll('.module-card');
    expect(moduleCards.length).toBe(9);
  });

  it('should render dashboard title and description', () => {
    const { container } = renderWithProviders(['divipol.read']);

    // The title and description should be rendered (using fallback English text)
    expect(container.innerHTML).toContain('Main Panel');
    expect(container.innerHTML).toContain('Select a module to begin');
  });
});
