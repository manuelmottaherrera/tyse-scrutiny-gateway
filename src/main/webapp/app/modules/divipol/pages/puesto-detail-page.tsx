import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { Container, Row, Col, Nav, NavItem, NavLink, TabContent, TabPane, Spinner, Alert, Button, Card, CardBody } from 'reactstrap';
import { Translate } from 'react-jhipster';
import classnames from 'classnames';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import divipolService, { PuestoDetalle, Jurado, TestigoAsignado } from 'app/shared/services/divipol.service';
import { PuestoInfoTab } from '../components/puesto-info-tab';
import { PuestoJuradosTab } from '../components/puesto-jurados-tab';
import { PuestoTestigosTab } from '../components/puesto-testigos-tab';
import './puesto-detail-page.scss';

type TabType = 'info' | 'jurados' | 'testigos';

/**
 * Página de detalle de puesto - Reemplaza PuestoDetailModal.
 * Usa URL params para el ID del puesto y query params para el tab activo.
 * URL: /divipol/puestos/:puestoId?tab=info|jurados|testigos
 */
export const PuestoDetailPage: React.FC = () => {
  const { puestoId } = useParams<{ puestoId: string }>();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // Tab activo desde URL o default 'info'
  const tabFromUrl = (searchParams.get('tab') as TabType) || 'info';
  const [activeTab, setActiveTab] = useState<TabType>(tabFromUrl);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [puestoDetalle, setPuestoDetalle] = useState<PuestoDetalle | null>(null);
  const [jurados, setJurados] = useState<Jurado[]>([]);
  const [testigos, setTestigos] = useState<TestigoAsignado[]>([]);
  const [loadingJurados, setLoadingJurados] = useState(false);
  const [loadingTestigos, setLoadingTestigos] = useState(false);

  const puestoIdNum = puestoId ? parseInt(puestoId, 10) : null;

  // Cargar detalle del puesto al montar
  useEffect(() => {
    if (puestoIdNum) {
      loadPuestoDetalle();
    }
  }, [puestoIdNum]);

  // Cargar datos del tab cuando cambia
  useEffect(() => {
    if (activeTab === 'jurados' && puestoIdNum && jurados.length === 0) {
      loadJurados();
    } else if (activeTab === 'testigos' && puestoIdNum) {
      loadTestigos();
    }
  }, [activeTab, puestoIdNum]);

  // Sincronizar tab con URL
  useEffect(() => {
    if (tabFromUrl !== activeTab) {
      setActiveTab(tabFromUrl);
    }
  }, [tabFromUrl]);

  const loadPuestoDetalle = async () => {
    if (!puestoIdNum) return;

    setLoading(true);
    setError(null);

    try {
      const detalle = await divipolService.getPuestoDetalle(puestoIdNum);
      setPuestoDetalle(detalle);
    } catch (err) {
      setError('Error al cargar el detalle del puesto');
      console.error('Error loading puesto detail:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadJurados = async () => {
    if (!puestoIdNum) return;

    setLoadingJurados(true);
    try {
      const data = await divipolService.getJuradosByPuesto(puestoIdNum);
      setJurados(data);
    } catch (err) {
      console.error('Error loading jurados:', err);
    } finally {
      setLoadingJurados(false);
    }
  };

  const loadTestigos = async () => {
    if (!puestoIdNum) return;

    setLoadingTestigos(true);
    try {
      const data = await divipolService.getTestigosByPuesto(puestoIdNum);
      setTestigos(data);
    } catch (err) {
      console.error('Error loading testigos:', err);
    } finally {
      setLoadingTestigos(false);
    }
  };

  const handleTestigoAsignado = () => {
    loadTestigos();
    loadPuestoDetalle();
  };

  const handleTestigoDesasignado = () => {
    loadTestigos();
    loadPuestoDetalle();
  };

  const toggleTab = (tab: TabType) => {
    if (activeTab !== tab) {
      setActiveTab(tab);
      setSearchParams({ tab });
    }
  };

  const handleBack = () => {
    navigate('/divipol');
  };

  // Generar código divipol
  const generateCode = () => {
    if (!puestoDetalle) return '';
    const d = String(puestoDetalle.coddepto).padStart(2, '0');
    const m = String(puestoDetalle.codmipio).padStart(3, '0');
    const z = String(puestoDetalle.codzona).padStart(2, '0');
    const p = String(puestoDetalle.codpuesto).padStart(2, '0');
    return `${d}${m}${z}${p}`;
  };

  if (!puestoIdNum) {
    return (
      <Container className="py-4">
        <Alert color="danger">
          <FontAwesomeIcon icon="exclamation-triangle" className="me-2" />
          ID de puesto inválido
        </Alert>
        <Button color="secondary" onClick={handleBack}>
          <FontAwesomeIcon icon="arrow-left" className="me-2" />
          Volver
        </Button>
      </Container>
    );
  }

  return (
    <Container fluid className="puesto-detail-page py-4">
      {/* Breadcrumb / Back Navigation */}
      <Row className="mb-3">
        <Col>
          <div className="d-flex align-items-center">
            <Button color="link" className="p-0 me-3 text-decoration-none" onClick={handleBack}>
              <FontAwesomeIcon icon="arrow-left" className="me-2" />
              <span className="d-none d-sm-inline">
                <Translate contentKey="divipol.title">DIVIPOL</Translate>
              </span>
            </Button>
            {puestoDetalle && (
              <nav aria-label="breadcrumb" className="breadcrumb-nav">
                <ol className="breadcrumb mb-0">
                  <li className="breadcrumb-item">
                    <span className="breadcrumb-badge depto">{puestoDetalle.nomdepto}</span>
                  </li>
                  <li className="breadcrumb-item">
                    <span className="breadcrumb-badge mpio">{puestoDetalle.nommipio}</span>
                  </li>
                  <li className="breadcrumb-item">
                    <span className="breadcrumb-badge zona">Zona {puestoDetalle.codzona}</span>
                  </li>
                  <li className="breadcrumb-item active" aria-current="page">
                    <span className="breadcrumb-badge puesto">{puestoDetalle.nompuesto}</span>
                  </li>
                </ol>
              </nav>
            )}
          </div>
        </Col>
      </Row>

      {/* Loading State */}
      {loading && (
        <div className="text-center py-5">
          <Spinner color="primary" />
          <p className="mt-2">
            <Translate contentKey="divipol.loading">Cargando...</Translate>
          </p>
        </div>
      )}

      {/* Error State */}
      {error && (
        <Alert color="danger">
          <FontAwesomeIcon icon="exclamation-triangle" className="me-2" />
          {error}
          <Button color="link" className="p-0 ms-3" onClick={loadPuestoDetalle}>
            Reintentar
          </Button>
        </Alert>
      )}

      {/* Content */}
      {!loading && !error && puestoDetalle && (
        <>
          {/* Header */}
          <Card className="mb-4">
            <CardBody>
              <Row className="align-items-center">
                <Col>
                  <h2 className="mb-1">
                    <FontAwesomeIcon icon="map-marker-alt" className="me-2 text-primary" />
                    {puestoDetalle.nompuesto}
                  </h2>
                  <p className="text-muted mb-0">
                    <span className="badge bg-secondary me-2">{generateCode()}</span>
                    {puestoDetalle.nomdepto} / {puestoDetalle.nommipio} / Zona {puestoDetalle.codzona}
                  </p>
                </Col>
                <Col xs="auto">
                  <div className="d-flex gap-3">
                    <div className="text-center">
                      <div className="h4 mb-0 text-primary">{puestoDetalle.nummesas}</div>
                      <small className="text-muted">Mesas</small>
                    </div>
                    <div className="text-center">
                      <div className="h4 mb-0 text-success">{puestoDetalle.totalJurados}</div>
                      <small className="text-muted">Jurados</small>
                    </div>
                    <div className="text-center">
                      <div className="h4 mb-0 text-info">{puestoDetalle.totalTestigos}</div>
                      <small className="text-muted">Testigos</small>
                    </div>
                  </div>
                </Col>
              </Row>
            </CardBody>
          </Card>

          {/* Tabs */}
          <Nav tabs className="mb-3">
            <NavItem>
              <NavLink
                className={classnames({ active: activeTab === 'info' })}
                onClick={() => toggleTab('info')}
                style={{ cursor: 'pointer' }}
              >
                <FontAwesomeIcon icon="info-circle" className="me-1" />
                <Translate contentKey="divipol.puestoDetail.tabs.info">Información</Translate>
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink
                className={classnames({ active: activeTab === 'jurados' })}
                onClick={() => toggleTab('jurados')}
                style={{ cursor: 'pointer' }}
              >
                <FontAwesomeIcon icon="users" className="me-1" />
                <Translate contentKey="divipol.puestoDetail.tabs.jurados">Jurados</Translate>
                <span className="badge bg-secondary ms-1">{puestoDetalle.totalJurados}</span>
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink
                className={classnames({ active: activeTab === 'testigos' })}
                onClick={() => toggleTab('testigos')}
                style={{ cursor: 'pointer' }}
              >
                <FontAwesomeIcon icon="user-check" className="me-1" />
                <Translate contentKey="divipol.puestoDetail.tabs.testigos">Testigos</Translate>
                <span className="badge bg-secondary ms-1">{puestoDetalle.totalTestigos}</span>
              </NavLink>
            </NavItem>
          </Nav>

          {/* Tab Content */}
          <TabContent activeTab={activeTab}>
            <TabPane tabId="info">
              <Card>
                <CardBody>
                  <PuestoInfoTab puesto={puestoDetalle} />
                </CardBody>
              </Card>
            </TabPane>
            <TabPane tabId="jurados">
              <Card>
                <CardBody>
                  <PuestoJuradosTab jurados={jurados} loading={loadingJurados} />
                </CardBody>
              </Card>
            </TabPane>
            <TabPane tabId="testigos">
              <Card>
                <CardBody>
                  <PuestoTestigosTab
                    testigos={testigos}
                    loading={loadingTestigos}
                    puestoId={puestoIdNum}
                    onTestigoAsignado={handleTestigoAsignado}
                    onTestigoDesasignado={handleTestigoDesasignado}
                  />
                </CardBody>
              </Card>
            </TabPane>
          </TabContent>
        </>
      )}
    </Container>
  );
};

export default PuestoDetailPage;
