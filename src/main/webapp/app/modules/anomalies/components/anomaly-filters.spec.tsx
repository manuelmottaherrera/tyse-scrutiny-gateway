import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { Provider } from 'react-redux';
import { TranslatorContext } from 'react-jhipster';
import configureStore from 'redux-mock-store';

import { AnomalyFilters } from './anomaly-filters';

const mockStore = configureStore([]);

describe('AnomalyFilters', () => {
  beforeAll(() => {
    TranslatorContext.registerTranslations('es', {
      'anomalies.filters.status': 'Estado',
      'anomalies.filters.type': 'Tipo',
      'anomalies.filters.severity': 'Severidad',
      'anomalies.filters.allStatuses': 'Todos',
      'anomalies.filters.allTypes': 'Todos',
      'anomalies.filters.allSeverities': 'Todas',
      'anomalies.filters.reset': 'Limpiar',
      'anomalies.status.NEW': 'Nueva',
      'anomalies.status.REVIEWED': 'Revisada',
      'anomalies.status.CLAIMED': 'Reclamada',
      'anomalies.status.DISMISSED': 'Descartada',
      'anomalies.type.PRECOUNT_DIFFERENCE': 'Diferencia Preconteo',
      'anomalies.type.VOTES_EXCEED_VOTERS': 'Votos exceden votantes',
      'anomalies.type.SUM_MISMATCH': 'Suma inconsistente',
      'anomalies.severity.CRITICAL': 'Crítica',
      'anomalies.severity.HIGH': 'Alta',
      'anomalies.severity.MEDIUM': 'Media',
      'anomalies.severity.LOW': 'Baja',
    });
  });

  const createInitialState = (filters = {}) => ({
    anomaly: {
      filters: {
        status: null,
        type: null,
        severity: null,
        ...filters,
      },
    },
  });

  describe('rendering', () => {
    it('should render all filter dropdowns', () => {
      const store = mockStore(createInitialState());

      render(
        <Provider store={store}>
          <AnomalyFilters />
        </Provider>,
      );

      expect(screen.getByText('Estado')).toBeInTheDocument();
      expect(screen.getByText('Tipo')).toBeInTheDocument();
      expect(screen.getByText('Severidad')).toBeInTheDocument();
    });

    it('should render status select element', () => {
      const store = mockStore(createInitialState());

      const { container } = render(
        <Provider store={store}>
          <AnomalyFilters />
        </Provider>,
      );

      const statusSelect = container.querySelector('#status');
      expect(statusSelect).toBeInTheDocument();
    });

    it('should render type select element', () => {
      const store = mockStore(createInitialState());

      const { container } = render(
        <Provider store={store}>
          <AnomalyFilters />
        </Provider>,
      );

      const typeSelect = container.querySelector('#type');
      expect(typeSelect).toBeInTheDocument();
    });

    it('should render severity select element', () => {
      const store = mockStore(createInitialState());

      const { container } = render(
        <Provider store={store}>
          <AnomalyFilters />
        </Provider>,
      );

      const severitySelect = container.querySelector('#severity');
      expect(severitySelect).toBeInTheDocument();
    });

    it('should render reset button', () => {
      const store = mockStore(createInitialState());

      render(
        <Provider store={store}>
          <AnomalyFilters />
        </Provider>,
      );

      expect(screen.getByText('Limpiar')).toBeInTheDocument();
    });
  });

  describe('filter interactions', () => {
    it('should dispatch action when status filter changes', () => {
      const store = mockStore(createInitialState());

      const { container } = render(
        <Provider store={store}>
          <AnomalyFilters />
        </Provider>,
      );

      const statusSelect = container.querySelector('#status');
      fireEvent.change(statusSelect, { target: { value: 'REVIEWED' } });

      const actions = store.getActions();
      expect(actions).toHaveLength(1);
      expect(actions[0].type).toContain('setFilterStatus');
    });

    it('should dispatch action when type filter changes', () => {
      const store = mockStore(createInitialState());

      const { container } = render(
        <Provider store={store}>
          <AnomalyFilters />
        </Provider>,
      );

      const typeSelect = container.querySelector('#type');
      fireEvent.change(typeSelect, { target: { value: 'PRECOUNT_DIFFERENCE' } });

      const actions = store.getActions();
      expect(actions).toHaveLength(1);
      expect(actions[0].type).toContain('setFilterType');
    });

    it('should dispatch action when severity filter changes', () => {
      const store = mockStore(createInitialState());

      const { container } = render(
        <Provider store={store}>
          <AnomalyFilters />
        </Provider>,
      );

      const severitySelect = container.querySelector('#severity');
      fireEvent.change(severitySelect, { target: { value: 'CRITICAL' } });

      const actions = store.getActions();
      expect(actions).toHaveLength(1);
      expect(actions[0].type).toContain('setFilterSeverity');
    });

    it('should dispatch clearFilters action when reset button is clicked', () => {
      const store = mockStore(createInitialState({ status: 'NEW', type: 'PRECOUNT_DIFFERENCE' }));

      render(
        <Provider store={store}>
          <AnomalyFilters />
        </Provider>,
      );

      const resetButton = screen.getByText('Limpiar');
      fireEvent.click(resetButton);

      const actions = store.getActions();
      expect(actions).toHaveLength(1);
      expect(actions[0].type).toContain('clearFilters');
    });
  });
});
