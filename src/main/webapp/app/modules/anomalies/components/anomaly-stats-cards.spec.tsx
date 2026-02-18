import React from 'react';
import { render, screen } from '@testing-library/react';
import { TranslatorContext } from 'react-jhipster';

import { AnomalyStatsCards } from './anomaly-stats-cards';

describe('AnomalyStatsCards', () => {
  beforeAll(() => {
    TranslatorContext.registerTranslations('es', {
      'anomalies.stats.total': 'Total Anomalías',
      'anomalies.stats.new': 'Por Revisar',
      'anomalies.stats.highSeverity': 'Alta Severidad',
      'anomalies.stats.claimed': 'Reclamaciones',
    });
  });

  const mockStats = {
    total: 100,
    new: 25,
    highSeverity: 10,
    claimed: 5,
  };

  describe('rendering', () => {
    it('should render all four stats cards', () => {
      render(<AnomalyStatsCards stats={mockStats} loading={false} />);

      expect(screen.getByText('Total Anomalías')).toBeInTheDocument();
      expect(screen.getByText('Por Revisar')).toBeInTheDocument();
      expect(screen.getByText('Alta Severidad')).toBeInTheDocument();
      expect(screen.getByText('Reclamaciones')).toBeInTheDocument();
    });

    it('should render stats values', () => {
      render(<AnomalyStatsCards stats={mockStats} loading={false} />);

      expect(screen.getByText('100')).toBeInTheDocument();
      expect(screen.getByText('25')).toBeInTheDocument();
      expect(screen.getByText('10')).toBeInTheDocument();
      expect(screen.getByText('5')).toBeInTheDocument();
    });

    it('should show loading spinner when loading is true', () => {
      render(<AnomalyStatsCards stats={null} loading={true} />);

      expect(screen.getByRole('status')).toBeInTheDocument();
    });

    it('should render nothing when stats is null and not loading', () => {
      const { container } = render(<AnomalyStatsCards stats={null} loading={false} />);

      expect(container.querySelector('.anomaly-stats-cards')).not.toBeInTheDocument();
    });
  });

  describe('card styling', () => {
    it('should render cards with stats-card class', () => {
      const { container } = render(<AnomalyStatsCards stats={mockStats} loading={false} />);

      const cards = container.querySelectorAll('.stats-card');
      expect(cards.length).toBe(4);
    });
  });

  describe('value formatting', () => {
    it('should format large numbers with locale', () => {
      const statsWithLargeNumbers = {
        total: 1234,
        new: 567,
        highSeverity: 89,
        claimed: 12,
      };

      render(<AnomalyStatsCards stats={statsWithLargeNumbers} loading={false} />);

      // es-CO uses dots for thousands
      expect(screen.getByText('1.234')).toBeInTheDocument();
    });

    it('should display 0 when values are undefined', () => {
      const incompleteStats = {
        total: undefined as unknown as number,
        new: undefined as unknown as number,
        highSeverity: undefined as unknown as number,
        claimed: undefined as unknown as number,
      };

      const { container } = render(<AnomalyStatsCards stats={incompleteStats} loading={false} />);

      const zeros = container.querySelectorAll('.stats-card-value');
      zeros.forEach(zero => {
        expect(zero.textContent).toBe('0');
      });
    });
  });
});
