import React, { useState, useEffect } from 'react';
import { Modal, ModalHeader, ModalBody, Nav, NavItem, NavLink, TabContent, TabPane, Spinner, Alert } from 'reactstrap';
import { Translate } from 'react-jhipster';
import classnames from 'classnames';
import divipolService, { PuestoDetalle, Jurado, TestigoAsignado } from 'app/shared/services/divipol.service';
import { PuestoInfoTab } from './puesto-info-tab';
import { PuestoJuradosTab } from './puesto-jurados-tab';
import { PuestoTestigosTab } from './puesto-testigos-tab';
import './puesto-detail-modal.scss';

interface PuestoDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  puestoId: number | null;
}

type TabType = 'info' | 'jurados' | 'testigos';

export const PuestoDetailModal: React.FC<PuestoDetailModalProps> = ({ isOpen, onClose, puestoId }) => {
  const [activeTab, setActiveTab] = useState<TabType>('info');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [puestoDetalle, setPuestoDetalle] = useState<PuestoDetalle | null>(null);
  const [jurados, setJurados] = useState<Jurado[]>([]);
  const [testigos, setTestigos] = useState<TestigoAsignado[]>([]);
  const [loadingJurados, setLoadingJurados] = useState(false);
  const [loadingTestigos, setLoadingTestigos] = useState(false);

  useEffect(() => {
    if (isOpen && puestoId) {
      loadPuestoDetalle();
    }
  }, [isOpen, puestoId]);

  useEffect(() => {
    if (activeTab === 'jurados' && puestoId && jurados.length === 0) {
      loadJurados();
    } else if (activeTab === 'testigos' && puestoId) {
      loadTestigos();
    }
  }, [activeTab, puestoId]);

  const loadPuestoDetalle = async () => {
    if (!puestoId) return;

    setLoading(true);
    setError(null);

    try {
      const detalle = await divipolService.getPuestoDetalle(puestoId);
      setPuestoDetalle(detalle);
    } catch (err) {
      setError('Error al cargar el detalle del puesto');
      console.error('Error loading puesto detail:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadJurados = async () => {
    if (!puestoId) return;

    setLoadingJurados(true);
    try {
      const data = await divipolService.getJuradosByPuesto(puestoId);
      setJurados(data);
    } catch (err) {
      console.error('Error loading jurados:', err);
    } finally {
      setLoadingJurados(false);
    }
  };

  const loadTestigos = async () => {
    if (!puestoId) return;

    setLoadingTestigos(true);
    try {
      const data = await divipolService.getTestigosByPuesto(puestoId);
      setTestigos(data);
    } catch (err) {
      console.error('Error loading testigos:', err);
    } finally {
      setLoadingTestigos(false);
    }
  };

  const handleClose = () => {
    setActiveTab('info');
    setPuestoDetalle(null);
    setJurados([]);
    setTestigos([]);
    setError(null);
    onClose();
  };

  const handleTestigoAsignado = () => {
    loadTestigos();
    // Recargar detalle para actualizar conteo
    loadPuestoDetalle();
  };

  const handleTestigoDesasignado = () => {
    loadTestigos();
    loadPuestoDetalle();
  };

  const toggleTab = (tab: TabType) => {
    if (activeTab !== tab) {
      setActiveTab(tab);
    }
  };

  return (
    <Modal isOpen={isOpen} toggle={handleClose} size="lg" centered className="puesto-detail-modal">
      <ModalHeader toggle={handleClose}>
        <i className="bi bi-geo-alt-fill me-2"></i>
        <Translate contentKey="divipol.puestoDetail.title">Detalle del Puesto</Translate>
        {puestoDetalle && <span className="ms-2 text-muted">- {puestoDetalle.nompuesto}</span>}
      </ModalHeader>
      <ModalBody>
        {loading && (
          <div className="text-center py-5">
            <Spinner color="primary" />
            <p className="mt-2">
              <Translate contentKey="divipol.loading">Cargando...</Translate>
            </p>
          </div>
        )}

        {error && (
          <Alert color="danger">
            <i className="bi bi-exclamation-triangle me-2"></i>
            {error}
          </Alert>
        )}

        {!loading && !error && puestoDetalle && (
          <>
            <Nav tabs className="mb-3">
              <NavItem>
                <NavLink className={classnames({ active: activeTab === 'info' })} onClick={() => toggleTab('info')}>
                  <i className="bi bi-info-circle me-1"></i>
                  <Translate contentKey="divipol.puestoDetail.tabs.info">Información</Translate>
                </NavLink>
              </NavItem>
              <NavItem>
                <NavLink className={classnames({ active: activeTab === 'jurados' })} onClick={() => toggleTab('jurados')}>
                  <i className="bi bi-people me-1"></i>
                  <Translate contentKey="divipol.puestoDetail.tabs.jurados">Jurados</Translate>
                  <span className="badge bg-secondary ms-1">{puestoDetalle.totalJurados}</span>
                </NavLink>
              </NavItem>
              <NavItem>
                <NavLink className={classnames({ active: activeTab === 'testigos' })} onClick={() => toggleTab('testigos')}>
                  <i className="bi bi-person-badge me-1"></i>
                  <Translate contentKey="divipol.puestoDetail.tabs.testigos">Testigos</Translate>
                  <span className="badge bg-secondary ms-1">{puestoDetalle.totalTestigos}</span>
                </NavLink>
              </NavItem>
            </Nav>

            <TabContent activeTab={activeTab}>
              <TabPane tabId="info">
                <PuestoInfoTab puesto={puestoDetalle} />
              </TabPane>
              <TabPane tabId="jurados">
                <PuestoJuradosTab jurados={jurados} loading={loadingJurados} />
              </TabPane>
              <TabPane tabId="testigos">
                <PuestoTestigosTab
                  testigos={testigos}
                  loading={loadingTestigos}
                  puestoId={puestoId!}
                  onTestigoAsignado={handleTestigoAsignado}
                  onTestigoDesasignado={handleTestigoDesasignado}
                />
              </TabPane>
            </TabContent>
          </>
        )}
      </ModalBody>
    </Modal>
  );
};

export default PuestoDetailModal;
