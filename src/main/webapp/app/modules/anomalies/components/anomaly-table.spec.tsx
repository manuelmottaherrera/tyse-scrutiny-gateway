import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import { TranslatorContext } from 'react-jhipster';
import configureStore from 'redux-mock-store';

import { AnomalyTable } from './anomaly-table';

const mockStore = configureStore([]);

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

describe('AnomalyTable', () => {
  beforeAll(() => {
    TranslatorContext.registerTranslations('es', {});
  });

  beforeEach(() => {
    mockNavigate.mockClear();
  });

  const createInitialState = (anomalies = [], pagination = {}) => ({
    anomaly: {
      anomalies,
      pagination: {
        page: 0,
        size: 10,
        totalElements: anomalies.length,
        totalPages: 1,
        ...pagination,
      },
    },
  });

  const mockAnomaly = {
    id: 1,
    type: 'PRECOUNT_DIFFERENCE',
    severity: 'HIGH',
    status: 'NEW',
    detectedAt: '2024-02-11T13:01:00Z',
    divipolKey: '31-001-10-01-001',
    depCode: '31',
    munCode: '001',
    votingTable: '001',
    candidateFirstName: 'Carlos',
    candidateLastName: 'García',
    precountVotes: 100,
    scrutinyVotes: 150,
    difference: 50,
  };

  describe('rendering', () => {
    it('should show loading spinner when loading is true', () => {
      const store = mockStore(createInitialState());

      render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={true} />
          </MemoryRouter>
        </Provider>,
      );

      expect(screen.getByRole('status')).toBeInTheDocument();
    });

    it('should show "no data" message when there are no anomalies', () => {
      const store = mockStore(createInitialState([]));

      render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      expect(screen.getByText(/No hay anomalías para mostrar/i)).toBeInTheDocument();
    });

    it('should render table with anomalies', () => {
      const store = mockStore(createInitialState([mockAnomaly]));

      const { container } = render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      // Check table headers exist
      expect(screen.getByText('Caso')).toBeInTheDocument();
      expect(screen.getByText('Fecha')).toBeInTheDocument();
      expect(screen.getByText('Tipo')).toBeInTheDocument();
      expect(screen.getByText('Severidad')).toBeInTheDocument();
      expect(screen.getByText('Estado')).toBeInTheDocument();

      // Check anomaly data is rendered
      expect(container.querySelector('tbody tr')).toBeInTheDocument();
    });

    it('should render case number with # prefix', () => {
      const store = mockStore(createInitialState([mockAnomaly]));

      render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      expect(screen.getByText('#1')).toBeInTheDocument();
    });

    it('should render candidate full name', () => {
      const store = mockStore(createInitialState([mockAnomaly]));

      render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      expect(screen.getByText('Carlos García')).toBeInTheDocument();
    });

    it('should show "-" when candidate name is not available', () => {
      const anomalyWithoutCandidate = { ...mockAnomaly, candidateFirstName: null, candidateLastName: null };
      const store = mockStore(createInitialState([anomalyWithoutCandidate]));

      const { container } = render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      const cells = container.querySelectorAll('tbody td');
      const cellTexts = Array.from(cells).map(cell => cell.textContent);
      expect(cellTexts).toContain('-');
    });

    it('should format difference with locale', () => {
      const anomalyWithLargeDiff = { ...mockAnomaly, difference: 1234 };
      const store = mockStore(createInitialState([anomalyWithLargeDiff]));

      render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      // es-CO uses dots for thousands
      expect(screen.getByText('1.234')).toBeInTheDocument();
    });
  });

  describe('severity badges', () => {
    it.each([
      ['CRITICAL', 'severity-critical'],
      ['HIGH', 'severity-high'],
      ['MEDIUM', 'severity-medium'],
      ['LOW', 'severity-low'],
    ])('should render %s severity with correct class', (severity, expectedClass) => {
      const anomaly = { ...mockAnomaly, severity };
      const store = mockStore(createInitialState([anomaly]));

      const { container } = render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      const badge = container.querySelector(`.${expectedClass}`);
      expect(badge).toBeInTheDocument();
    });
  });

  describe('status badges', () => {
    it.each([
      ['NEW', 'status-new'],
      ['REVIEWED', 'status-reviewed'],
      ['CLAIMED', 'status-claimed'],
      ['DISMISSED', 'status-dismissed'],
    ])('should render %s status with correct class', (status, expectedClass) => {
      const anomaly = { ...mockAnomaly, status };
      const store = mockStore(createInitialState([anomaly]));

      const { container } = render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      const badge = container.querySelector(`.${expectedClass}`);
      expect(badge).toBeInTheDocument();
    });
  });

  describe('navigation', () => {
    it('should navigate to detail page when row is clicked', () => {
      const store = mockStore(createInitialState([mockAnomaly]));

      const { container } = render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      const row = container.querySelector('tbody tr');
      fireEvent.click(row);

      expect(mockNavigate).toHaveBeenCalledWith('/anomalies/1');
    });

    it('should navigate when view button is clicked', () => {
      const store = mockStore(createInitialState([mockAnomaly]));

      const { container } = render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      const viewButton = container.querySelector('button[title="Ver detalle"]');
      fireEvent.click(viewButton);

      expect(mockNavigate).toHaveBeenCalledWith('/anomalies/1');
    });
  });

  describe('pagination', () => {
    it('should not show pagination when there is only one page', () => {
      const store = mockStore(createInitialState([mockAnomaly], { totalPages: 1 }));

      const { container } = render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      expect(container.querySelector('.pagination')).not.toBeInTheDocument();
    });

    it('should show pagination when there are multiple pages', () => {
      const anomalies = Array(15)
        .fill(null)
        .map((_, i) => ({ ...mockAnomaly, id: i + 1 }));
      const store = mockStore(
        createInitialState(anomalies, {
          page: 0,
          size: 10,
          totalElements: 15,
          totalPages: 2,
        }),
      );

      const { container } = render(
        <Provider store={store}>
          <MemoryRouter>
            <AnomalyTable loading={false} />
          </MemoryRouter>
        </Provider>,
      );

      expect(container.querySelector('.pagination')).toBeInTheDocument();
    });
  });
});
