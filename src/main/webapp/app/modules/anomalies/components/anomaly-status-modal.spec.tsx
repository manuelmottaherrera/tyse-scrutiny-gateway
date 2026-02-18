import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { TranslatorContext } from 'react-jhipster';

import { AnomalyStatusModal } from './anomaly-status-modal';

describe('AnomalyStatusModal', () => {
  beforeAll(() => {
    TranslatorContext.registerTranslations('es', {
      'anomalies.modal.changeStatus': 'Cambiar Estado',
      'anomalies.modal.notes': 'Notas (opcional)',
      'anomalies.modal.notesPlaceholder': 'Agregar notas sobre esta acción...',
      'anomalies.status.NEW': 'Nueva',
      'anomalies.status.REVIEWED': 'Revisada',
      'anomalies.status.CLAIMED': 'Reclamada',
      'anomalies.status.DISMISSED': 'Descartada',
      'entity.action.cancel': 'Cancelar',
      'entity.action.save': 'Guardar',
      'entity.action.saving': 'Guardando...',
    });
  });

  const defaultProps = {
    isOpen: true,
    currentStatus: 'NEW' as const,
    onClose: jest.fn(),
    onSubmit: jest.fn(),
    loading: false,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('rendering', () => {
    it('should render modal when isOpen is true', () => {
      render(<AnomalyStatusModal {...defaultProps} />);

      expect(screen.getByText('Cambiar Estado')).toBeInTheDocument();
    });

    it('should not render modal content when isOpen is false', () => {
      render(<AnomalyStatusModal {...defaultProps} isOpen={false} />);

      expect(screen.queryByText('Cambiar Estado')).not.toBeInTheDocument();
    });

    it('should render status option descriptions', () => {
      render(<AnomalyStatusModal {...defaultProps} />);

      expect(screen.getByText('Anomalía sin revisar')).toBeInTheDocument();
      expect(screen.getByText('Anomalía revisada y verificada')).toBeInTheDocument();
      expect(screen.getByText('Reclamación presentada ante autoridad electoral')).toBeInTheDocument();
      expect(screen.getByText('Anomalía descartada (falso positivo)')).toBeInTheDocument();
    });

    it('should render cancel and save buttons', () => {
      render(<AnomalyStatusModal {...defaultProps} />);

      expect(screen.getByRole('button', { name: /Cancelar/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Guardar/i })).toBeInTheDocument();
    });
  });

  describe('status selection', () => {
    it('should render all four status descriptions', () => {
      render(<AnomalyStatusModal {...defaultProps} />);

      // Verify all status descriptions are rendered
      expect(screen.getByText('Anomalía sin revisar')).toBeInTheDocument();
      expect(screen.getByText('Anomalía revisada y verificada')).toBeInTheDocument();
      expect(screen.getByText('Reclamación presentada ante autoridad electoral')).toBeInTheDocument();
      expect(screen.getByText('Anomalía descartada (falso positivo)')).toBeInTheDocument();
    });

    it('should allow selecting a different status', () => {
      const { container } = render(<AnomalyStatusModal {...defaultProps} currentStatus="NEW" />);

      // Click on the "Revisada" option
      const reviewedDescription = screen.getByText('Anomalía revisada y verificada');
      const reviewedOption = reviewedDescription.closest('.status-option');
      fireEvent.click(reviewedOption);

      expect(reviewedOption).toHaveClass('selected');
    });
  });

  describe('form submission', () => {
    it('should call onSubmit with selected status when save is clicked', () => {
      const onSubmit = jest.fn();
      render(<AnomalyStatusModal {...defaultProps} onSubmit={onSubmit} />);

      // Select a different status
      const reviewedDescription = screen.getByText('Anomalía revisada y verificada');
      const reviewedOption = reviewedDescription.closest('.status-option');
      fireEvent.click(reviewedOption);

      // Click save
      const saveButton = screen.getByRole('button', { name: /Guardar/i });
      fireEvent.click(saveButton);

      expect(onSubmit).toHaveBeenCalledWith('REVIEWED', undefined);
    });

    it('should call onSubmit with notes when provided', () => {
      const onSubmit = jest.fn();
      render(<AnomalyStatusModal {...defaultProps} onSubmit={onSubmit} />);

      // Select a different status
      const claimedDescription = screen.getByText('Reclamación presentada ante autoridad electoral');
      const claimedOption = claimedDescription.closest('.status-option');
      fireEvent.click(claimedOption);

      // Enter notes
      const notesInput = screen.getByRole('textbox');
      fireEvent.change(notesInput, { target: { value: 'Test notes' } });

      // Click save
      const saveButton = screen.getByRole('button', { name: /Guardar/i });
      fireEvent.click(saveButton);

      expect(onSubmit).toHaveBeenCalledWith('CLAIMED', 'Test notes');
    });

    it('should call onClose when cancel is clicked', () => {
      const onClose = jest.fn();
      render(<AnomalyStatusModal {...defaultProps} onClose={onClose} />);

      const cancelButton = screen.getByRole('button', { name: /Cancelar/i });
      fireEvent.click(cancelButton);

      expect(onClose).toHaveBeenCalled();
    });
  });
});
