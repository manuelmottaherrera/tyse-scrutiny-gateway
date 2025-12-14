import React, { useState, useEffect, useCallback } from 'react';
import {
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Button,
  Input,
  InputGroup,
  ListGroup,
  ListGroupItem,
  Spinner,
  Alert,
} from 'reactstrap';
import { Translate } from 'react-jhipster';
import divipolService, { Testigo } from 'app/shared/services/divipol.service';
import './asignar-testigo-modal.scss';

interface AsignarTestigoModalProps {
  isOpen: boolean;
  onClose: () => void;
  puestoId: number;
  onTestigoAsignado: () => void;
  testigosYaAsignados: number[];
}

export const AsignarTestigoModal: React.FC<AsignarTestigoModalProps> = ({
  isOpen,
  onClose,
  puestoId,
  onTestigoAsignado,
  testigosYaAsignados,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [testigos, setTestigos] = useState<Testigo[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedTestigo, setSelectedTestigo] = useState<Testigo | null>(null);
  const [asignando, setAsignando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Debounce search
  useEffect(() => {
    if (searchTerm.length < 2) {
      setTestigos([]);
      return;
    }

    const timeoutId = setTimeout(() => {
      searchTestigos();
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [searchTerm]);

  const searchTestigos = async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await divipolService.searchTestigos(searchTerm, 0, 10);
      // Filtrar testigos que ya están asignados
      const filtered = result.content.filter(t => !testigosYaAsignados.includes(t.id));
      setTestigos(filtered);
    } catch (err) {
      console.error('Error searching testigos:', err);
      setError('Error al buscar testigos');
    } finally {
      setLoading(false);
    }
  };

  const handleAsignar = async () => {
    if (!selectedTestigo) return;

    setAsignando(true);
    setError(null);
    try {
      await divipolService.asignarTestigoAPuesto(puestoId, selectedTestigo.id);
      onTestigoAsignado();
    } catch (err: any) {
      console.error('Error asignando testigo:', err);
      if (err.response?.status === 409) {
        setError('El testigo ya está asignado a este puesto');
      } else {
        setError('Error al asignar el testigo');
      }
    } finally {
      setAsignando(false);
    }
  };

  const handleClose = () => {
    setSearchTerm('');
    setTestigos([]);
    setSelectedTestigo(null);
    setError(null);
    onClose();
  };

  return (
    <Modal isOpen={isOpen} toggle={handleClose} centered>
      <ModalHeader toggle={handleClose}>
        <i className="bi bi-person-plus me-2"></i>
        <Translate contentKey="divipol.asignarTestigo.title">Asignar Testigo Electoral</Translate>
      </ModalHeader>
      <ModalBody>
        {error && (
          <Alert color="danger" className="mb-3">
            <i className="bi bi-exclamation-triangle me-2"></i>
            {error}
          </Alert>
        )}

        <div className="mb-3">
          <label className="form-label">
            <Translate contentKey="divipol.asignarTestigo.buscar">Buscar testigo</Translate>
          </label>
          <InputGroup>
            <Input
              type="text"
              placeholder="Nombre o número de documento..."
              value={searchTerm}
              onChange={e => {
                setSearchTerm(e.target.value);
                setSelectedTestigo(null);
              }}
              autoFocus
            />
            {loading && (
              <span className="input-group-text">
                <Spinner size="sm" />
              </span>
            )}
          </InputGroup>
          <small className="text-muted">
            <Translate contentKey="divipol.asignarTestigo.hint">Ingrese al menos 2 caracteres para buscar</Translate>
          </small>
        </div>

        {testigos.length > 0 && (
          <div className="testigos-list">
            <ListGroup>
              {testigos.map(testigo => (
                <ListGroupItem
                  key={testigo.id}
                  tag="button"
                  action
                  active={selectedTestigo?.id === testigo.id}
                  onClick={() => setSelectedTestigo(testigo)}
                  className="d-flex justify-content-between align-items-center"
                >
                  <div>
                    <strong>
                      {testigo.nombres} {testigo.apellidos}
                    </strong>
                    <br />
                    <small className="text-muted">
                      {testigo.tipoDocumento} {testigo.numeroDocumento}
                      {testigo.telefono && ` • ${testigo.telefono}`}
                    </small>
                  </div>
                  {selectedTestigo?.id === testigo.id && <i className="bi bi-check-circle-fill text-primary"></i>}
                </ListGroupItem>
              ))}
            </ListGroup>
          </div>
        )}

        {searchTerm.length >= 2 && !loading && testigos.length === 0 && (
          <Alert color="warning" className="mb-0">
            <i className="bi bi-search me-2"></i>
            <Translate contentKey="divipol.asignarTestigo.noResults">No se encontraron testigos disponibles para asignar</Translate>
          </Alert>
        )}

        {selectedTestigo && (
          <div className="selected-testigo mt-3 p-3 border rounded bg-light">
            <strong>
              <Translate contentKey="divipol.asignarTestigo.seleccionado">Testigo seleccionado:</Translate>
            </strong>
            <div className="mt-2">
              <i className="bi bi-person-badge me-2"></i>
              {selectedTestigo.nombres} {selectedTestigo.apellidos}
              <br />
              <small className="text-muted">
                {selectedTestigo.tipoDocumento} {selectedTestigo.numeroDocumento}
              </small>
            </div>
          </div>
        )}
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose} disabled={asignando}>
          <Translate contentKey="entity.action.cancel">Cancelar</Translate>
        </Button>
        <Button color="primary" onClick={handleAsignar} disabled={!selectedTestigo || asignando}>
          {asignando ? (
            <>
              <Spinner size="sm" className="me-2" />
              <Translate contentKey="divipol.asignarTestigo.asignando">Asignando...</Translate>
            </>
          ) : (
            <>
              <i className="bi bi-check-circle me-2"></i>
              <Translate contentKey="divipol.asignarTestigo.confirmar">Confirmar Asignación</Translate>
            </>
          )}
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default AsignarTestigoModal;
