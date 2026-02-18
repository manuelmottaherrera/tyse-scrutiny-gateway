import React, { useState } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button, FormGroup, Label, Input } from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AnomalyStatus } from 'app/shared/services/anomaly.service';

interface AnomalyStatusModalProps {
  isOpen: boolean;
  currentStatus: AnomalyStatus;
  onClose: () => void;
  onSubmit: (status: AnomalyStatus, notes?: string) => void;
  loading?: boolean;
}

interface StatusOption {
  value: AnomalyStatus;
  icon: string;
  color: string;
  description: string;
}

const statusOptions: StatusOption[] = [
  {
    value: 'NEW',
    icon: 'circle',
    color: '#0d6efd',
    description: 'Anomalía sin revisar',
  },
  {
    value: 'REVIEWED',
    icon: 'check-circle',
    color: '#198754',
    description: 'Anomalía revisada y verificada',
  },
  {
    value: 'CLAIMED',
    icon: 'gavel',
    color: '#6f42c1',
    description: 'Reclamación presentada ante autoridad electoral',
  },
  {
    value: 'DISMISSED',
    icon: 'times-circle',
    color: '#6c757d',
    description: 'Anomalía descartada (falso positivo)',
  },
];

export const AnomalyStatusModal: React.FC<AnomalyStatusModalProps> = ({ isOpen, currentStatus, onClose, onSubmit, loading }) => {
  const [selectedStatus, setSelectedStatus] = useState<AnomalyStatus>(currentStatus);
  const [notes, setNotes] = useState('');

  const handleSubmit = () => {
    onSubmit(selectedStatus, notes || undefined);
  };

  const handleClose = () => {
    setSelectedStatus(currentStatus);
    setNotes('');
    onClose();
  };

  return (
    <Modal isOpen={isOpen} toggle={handleClose} className="anomaly-status-modal" size="md">
      <ModalHeader toggle={handleClose}>
        <Translate contentKey="anomalies.modal.changeStatus">Cambiar Estado</Translate>
      </ModalHeader>
      <ModalBody>
        <div className="mb-4">
          {statusOptions.map(option => (
            <div
              key={option.value}
              className={`status-option ${selectedStatus === option.value ? 'selected' : ''}`}
              onClick={() => setSelectedStatus(option.value)}
            >
              <div className="d-flex align-items-start">
                <FontAwesomeIcon icon={option.icon as any} className="status-icon" style={{ color: option.color }} />
                <div>
                  <div className="status-label">{translate(`anomalies.status.${option.value}`)}</div>
                  <div className="status-description">{option.description}</div>
                </div>
              </div>
            </div>
          ))}
        </div>

        <FormGroup>
          <Label for="notes">
            <Translate contentKey="anomalies.modal.notes">Notas (opcional)</Translate>
          </Label>
          <Input
            type="textarea"
            id="notes"
            value={notes}
            onChange={e => setNotes(e.target.value)}
            placeholder={translate('anomalies.modal.notesPlaceholder')}
            rows={3}
          />
        </FormGroup>
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose} disabled={loading}>
          <Translate contentKey="entity.action.cancel">Cancelar</Translate>
        </Button>
        <Button color="primary" onClick={handleSubmit} disabled={loading || selectedStatus === currentStatus}>
          {loading ? (
            <>
              <span className="spinner-border spinner-border-sm me-2" />
              <Translate contentKey="entity.action.saving">Guardando...</Translate>
            </>
          ) : (
            <Translate contentKey="entity.action.save">Guardar</Translate>
          )}
        </Button>
      </ModalFooter>
    </Modal>
  );
};
