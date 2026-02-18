import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Alert, Button, Badge, Spinner } from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppSelector, useAppDispatch } from 'app/config/store';
import { fetchAnomalyById, updateAnomalyStatus, clearSelectedAnomaly } from './anomaly.reducer';
import { AnomalyStatusModal } from './components/anomaly-status-modal';
import { AnomalySeverity, AnomalyStatus, AnomalyType } from 'app/shared/services/anomaly.service';
import divipolService from 'app/shared/services/divipol.service';
import './anomalies.scss';

const getSeverityBadgeClass = (severity: AnomalySeverity): string => {
  const classes: Record<AnomalySeverity, string> = {
    CRITICAL: 'severity-critical',
    HIGH: 'severity-high',
    MEDIUM: 'severity-medium',
    LOW: 'severity-low',
  };
  return classes[severity] || '';
};

const getStatusBadgeClass = (status: AnomalyStatus): string => {
  const classes: Record<AnomalyStatus, string> = {
    NEW: 'status-new',
    REVIEWED: 'status-reviewed',
    CLAIMED: 'status-claimed',
    DISMISSED: 'status-dismissed',
  };
  return classes[status] || '';
};

const formatDate = (dateStr: string | undefined): string => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleDateString('es-CO', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const getTypeLabel = (type: AnomalyType): string => {
  const labels: Record<AnomalyType, string> = {
    PRECOUNT_DIFFERENCE: 'Diferencia entre Preconteo y Escrutinio',
    VOTES_EXCEED_VOTERS: 'Votos exceden el número de votantes',
    SUM_MISMATCH: 'Suma de votos inconsistente',
  };
  return labels[type] || type;
};

export const AnomalyDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { selectedAnomaly, loadingDetail, error } = useAppSelector(state => state.anomaly);
  const currentUser = useAppSelector(state => state.authentication.account);
  const [isStatusModalOpen, setStatusModalOpen] = useState(false);
  const [updating, setUpdating] = useState(false);
  const [deptoName, setDeptoName] = useState<string | null>(null);
  const [mpioName, setMpioName] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      dispatch(fetchAnomalyById(Number(id)));
    }
    return () => {
      dispatch(clearSelectedAnomaly());
    };
  }, [dispatch, id]);

  // Buscar nombres de departamento y municipio cuando se carga la anomalía
  useEffect(() => {
    const fetchLocationNames = async () => {
      if (selectedAnomaly?.depCode) {
        try {
          const depCode = Number(selectedAnomaly.depCode);
          const departamentos = await divipolService.getDepartamentos();
          const depto = departamentos.find(d => d.coddepto === depCode);
          if (depto) {
            setDeptoName(depto.nomdepto);
          }

          if (selectedAnomaly.munCode) {
            const munCode = Number(selectedAnomaly.munCode);
            const municipios = await divipolService.getMunicipios(depCode);
            const mpio = municipios.find(m => m.codmipio === munCode);
            if (mpio) {
              setMpioName(mpio.nommipio);
            }
          }
        } catch (e) {
          console.error('Error fetching location names:', e);
        }
      }
    };
    fetchLocationNames();
  }, [selectedAnomaly?.depCode, selectedAnomaly?.munCode]);

  const handleStatusChange = async (status: AnomalyStatus, notes?: string) => {
    if (!selectedAnomaly) return;

    setUpdating(true);
    try {
      await dispatch(
        updateAnomalyStatus({
          id: selectedAnomaly.id,
          request: {
            status,
            reviewedBy: currentUser?.login,
            notes,
          },
        }),
      ).unwrap();
      setStatusModalOpen(false);
    } finally {
      setUpdating(false);
    }
  };

  if (loadingDetail) {
    return (
      <Container fluid className="anomaly-detail-page p-4">
        <div className="text-center py-5">
          <Spinner color="primary" />
          <p className="mt-3">
            <Translate contentKey="anomalies.detail.loading">Cargando anomalía...</Translate>
          </p>
        </div>
      </Container>
    );
  }

  if (error) {
    return (
      <Container fluid className="anomaly-detail-page p-4">
        <Alert color="danger">
          <Translate contentKey="anomalies.detail.error">Error al cargar la anomalía</Translate>: {error}
        </Alert>
        <Button color="secondary" onClick={() => navigate('/anomalies')}>
          <FontAwesomeIcon icon="arrow-left" className="me-2" />
          <Translate contentKey="anomalies.detail.back">Volver</Translate>
        </Button>
      </Container>
    );
  }

  if (!selectedAnomaly) {
    return (
      <Container fluid className="anomaly-detail-page p-4">
        <Alert color="warning">
          <Translate contentKey="anomalies.detail.notFound">Anomalía no encontrada</Translate>
        </Alert>
        <Button color="secondary" onClick={() => navigate('/anomalies')}>
          <FontAwesomeIcon icon="arrow-left" className="me-2" />
          <Translate contentKey="anomalies.detail.back">Volver</Translate>
        </Button>
      </Container>
    );
  }

  return (
    <Container fluid className="anomaly-detail-page p-4">
      <div className="detail-header">
        <Link to="/anomalies" className="back-link">
          <FontAwesomeIcon icon="arrow-left" className="me-2" />
          <Translate contentKey="anomalies.detail.backToList">Volver a la lista</Translate>
        </Link>
        <h1 className="mt-3">
          <Translate contentKey="anomalies.detail.title">Detalle de Anomalía</Translate> #{selectedAnomaly.id}
        </h1>
        <div className="d-flex gap-2 mt-2">
          <Badge className={`badge-severity ${getSeverityBadgeClass(selectedAnomaly.severity)}`}>
            {translate(`anomalies.severity.${selectedAnomaly.severity}`)}
          </Badge>
          <Badge className={`badge-status ${getStatusBadgeClass(selectedAnomaly.status)}`}>
            {translate(`anomalies.status.${selectedAnomaly.status}`)}
          </Badge>
        </div>
      </div>

      <Row>
        <Col lg={8}>
          <div className="detail-card">
            <h5>
              <FontAwesomeIcon icon="exclamation-triangle" className="me-2" />
              <Translate contentKey="anomalies.detail.anomalyInfo">Información de la Anomalía</Translate>
            </h5>
            <div className="detail-row">
              <span className="detail-label">
                <Translate contentKey="anomalies.detail.type">Tipo</Translate>
              </span>
              <span className="detail-value">{getTypeLabel(selectedAnomaly.type)}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">
                <Translate contentKey="anomalies.detail.description">Descripción</Translate>
              </span>
              <span className="detail-value">{selectedAnomaly.description || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">
                <Translate contentKey="anomalies.detail.detectedAt">Detectada el</Translate>
              </span>
              <span className="detail-value">{formatDate(selectedAnomaly.detectedAt)}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">
                <Translate contentKey="anomalies.detail.precountVotes">Votos Preconteo</Translate>
              </span>
              <span className="detail-value">{selectedAnomaly.precountVotes?.toLocaleString('es-CO') || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">
                <Translate contentKey="anomalies.detail.scrutinyVotes">Votos Escrutinio</Translate>
              </span>
              <span className="detail-value">{selectedAnomaly.scrutinyVotes?.toLocaleString('es-CO') || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">
                <Translate contentKey="anomalies.detail.difference">Diferencia</Translate>
              </span>
              <span className={`detail-value ${selectedAnomaly.difference && selectedAnomaly.difference > 0 ? 'text-danger fw-bold' : ''}`}>
                {selectedAnomaly.difference?.toLocaleString('es-CO') || '-'}
              </span>
            </div>
          </div>

          <div className="detail-card">
            <h5>
              <FontAwesomeIcon icon="map-marker-alt" className="me-2" />
              <Translate contentKey="anomalies.detail.locationInfo">Ubicación</Translate>
            </h5>
            <div className="detail-row">
              <span className="detail-label">
                <Translate contentKey="anomalies.detail.divipolKey">Código DIVIPOL</Translate>
              </span>
              <span className="detail-value">{selectedAnomaly.divipolKey}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">
                <Translate contentKey="anomalies.detail.department">Departamento</Translate>
              </span>
              <span className="detail-value">
                {deptoName ? `${selectedAnomaly.depCode} - ${deptoName}` : selectedAnomaly.depCode || '-'}
              </span>
            </div>
            <div className="detail-row">
              <span className="detail-label">
                <Translate contentKey="anomalies.detail.municipality">Municipio</Translate>
              </span>
              <span className="detail-value">{mpioName ? `${selectedAnomaly.munCode} - ${mpioName}` : selectedAnomaly.munCode || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">
                <Translate contentKey="anomalies.detail.votingTable">Mesa de Votación</Translate>
              </span>
              <span className="detail-value">{selectedAnomaly.votingTable || '-'}</span>
            </div>
          </div>

          {(selectedAnomaly.candidateId || selectedAnomaly.partyNumber) && (
            <div className="detail-card">
              <h5>
                <FontAwesomeIcon icon="user" className="me-2" />
                <Translate contentKey="anomalies.detail.candidateInfo">Candidato</Translate>
              </h5>
              <div className="detail-row">
                <span className="detail-label">
                  <Translate contentKey="anomalies.detail.candidateName">Nombre</Translate>
                </span>
                <span className="detail-value">
                  {selectedAnomaly.candidateFirstName || selectedAnomaly.candidateLastName
                    ? `${selectedAnomaly.candidateFirstName || ''} ${selectedAnomaly.candidateLastName || ''}`.trim()
                    : '-'}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-label">
                  <Translate contentKey="anomalies.detail.candidateId">ID Candidato</Translate>
                </span>
                <span className="detail-value">{selectedAnomaly.candidateId || '-'}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">
                  <Translate contentKey="anomalies.detail.partyNumber">Número de Partido</Translate>
                </span>
                <span className="detail-value">{selectedAnomaly.partyNumber || '-'}</span>
              </div>
            </div>
          )}
        </Col>

        <Col lg={4}>
          <div className="detail-card">
            <h5>
              <FontAwesomeIcon icon="cogs" className="me-2" />
              <Translate contentKey="anomalies.detail.actions">Acciones</Translate>
            </h5>
            <div className="action-buttons">
              <Button color="primary" block className="mb-2" onClick={() => setStatusModalOpen(true)}>
                <FontAwesomeIcon icon="edit" className="me-2" />
                <Translate contentKey="anomalies.detail.changeStatus">Cambiar Estado</Translate>
              </Button>
              {selectedAnomaly.status === 'NEW' && (
                <Button
                  color="success"
                  block
                  className="mb-2"
                  onClick={() =>
                    handleStatusChange('REVIEWED', `Revisado por ${currentUser?.login} el ${new Date().toLocaleDateString('es-CO')}`)
                  }
                >
                  <FontAwesomeIcon icon="check" className="me-2" />
                  <Translate contentKey="anomalies.detail.markReviewed">Marcar como Revisada</Translate>
                </Button>
              )}
              {(selectedAnomaly.status === 'NEW' || selectedAnomaly.status === 'REVIEWED') && (
                <Button
                  color="warning"
                  block
                  className="mb-2"
                  onClick={() => handleStatusChange('CLAIMED', `Reclamación presentada por ${currentUser?.login}`)}
                >
                  <FontAwesomeIcon icon="gavel" className="me-2" />
                  <Translate contentKey="anomalies.detail.fileClaim">Presentar Reclamación</Translate>
                </Button>
              )}
            </div>
          </div>

          {(selectedAnomaly.reviewedBy || selectedAnomaly.reviewDate || selectedAnomaly.notes) && (
            <div className="detail-card">
              <h5>
                <FontAwesomeIcon icon="history" className="me-2" />
                <Translate contentKey="anomalies.detail.reviewInfo">Revisión</Translate>
              </h5>
              <div className="detail-row">
                <span className="detail-label">
                  <Translate contentKey="anomalies.detail.reviewedBy">Revisado por</Translate>
                </span>
                <span className="detail-value">{selectedAnomaly.reviewedBy || '-'}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">
                  <Translate contentKey="anomalies.detail.reviewDate">Fecha de revisión</Translate>
                </span>
                <span className="detail-value">{formatDate(selectedAnomaly.reviewDate)}</span>
              </div>
              {selectedAnomaly.notes && (
                <div className="detail-row">
                  <span className="detail-label">
                    <Translate contentKey="anomalies.detail.notes">Notas</Translate>
                  </span>
                  <span className="detail-value">{selectedAnomaly.notes}</span>
                </div>
              )}
            </div>
          )}
        </Col>
      </Row>

      <AnomalyStatusModal
        isOpen={isStatusModalOpen}
        currentStatus={selectedAnomaly.status}
        onClose={() => setStatusModalOpen(false)}
        onSubmit={handleStatusChange}
        loading={updating}
      />
    </Container>
  );
};

export default AnomalyDetailPage;
