import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col, Card, CardBody, CardHeader, Button, Spinner, Alert, Badge, Form, FormGroup, Label, Input } from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import divipolService, { ConfiguracionElectoral, ConfiguracionInscripcion } from 'app/shared/services/divipol.service';
import './configuracion-electoral-page.scss';

export const ConfiguracionElectoralPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [configuraciones, setConfiguraciones] = useState<ConfiguracionElectoral[]>([]);
  const [inscripcion, setInscripcion] = useState<ConfiguracionInscripcion | null>(null);
  const [saving, setSaving] = useState<string | null>(null);

  useEffect(() => {
    loadConfiguracion();
  }, []);

  const loadConfiguracion = async () => {
    setLoading(true);
    setError(null);
    try {
      const [configData, inscripcionData] = await Promise.all([
        divipolService.getAllConfiguracion(),
        divipolService.getConfiguracionInscripcion(),
      ]);
      setConfiguraciones(configData);
      setInscripcion(inscripcionData);
    } catch (err) {
      setError('Error al cargar la configuración electoral');
      console.error('Error loading configuracion:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateConfig = async (clave: string, valor: string) => {
    setSaving(clave);
    try {
      await divipolService.updateConfiguracion(clave, valor);
      await loadConfiguracion();
    } catch (err) {
      console.error('Error updating configuracion:', err);
    } finally {
      setSaving(null);
    }
  };

  const getConfigValue = (clave: string): string => {
    const config = configuraciones.find(c => c.clave === clave);
    return config?.valor || '';
  };

  const formatDate = (dateStr: string | undefined): string => {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-CO', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  return (
    <Container fluid className="configuracion-electoral-page py-4">
      {/* Header */}
      <Row className="mb-3">
        <Col>
          <div className="d-flex align-items-center">
            <Link to="/divipol" className="btn btn-link p-0 me-3 text-decoration-none">
              <FontAwesomeIcon icon="arrow-left" className="me-2" />
              <span className="d-none d-sm-inline">
                <Translate contentKey="divipol.title">DIVIPOL</Translate>
              </span>
            </Link>
            <h2 className="mb-0">
              <FontAwesomeIcon icon="cog" className="me-2 text-primary" />
              <Translate contentKey="divipol.configuracion.title">Configuración Electoral</Translate>
            </h2>
          </div>
        </Col>
      </Row>

      {/* Loading */}
      {loading && (
        <div className="text-center py-5">
          <Spinner color="primary" />
          <p className="mt-2">
            <Translate contentKey="divipol.loading">Cargando...</Translate>
          </p>
        </div>
      )}

      {/* Error */}
      {error && (
        <Alert color="danger">
          <FontAwesomeIcon icon="exclamation-triangle" className="me-2" />
          {error}
          <Button color="link" className="p-0 ms-3" onClick={loadConfiguracion}>
            Reintentar
          </Button>
        </Alert>
      )}

      {/* Content */}
      {!loading && !error && (
        <Row>
          {/* Inscripción Status Card */}
          <Col md="6" className="mb-4">
            <Card>
              <CardHeader>
                <FontAwesomeIcon icon="calendar-check" className="me-2" />
                <Translate contentKey="divipol.configuracion.inscripcion.titulo">Período de Inscripción de Testigos</Translate>
              </CardHeader>
              <CardBody>
                {inscripcion && (
                  <>
                    <div className="mb-3">
                      <Label className="fw-bold">
                        <Translate contentKey="divipol.configuracion.inscripcion.estado">Estado</Translate>
                      </Label>
                      <div>
                        {inscripcion.inscripcionAbierta ? (
                          <Badge color="success" className="fs-6">
                            <FontAwesomeIcon icon="check-circle" className="me-1" />
                            <Translate contentKey="divipol.configuracion.inscripcion.abierta">Inscripciones Abiertas</Translate>
                          </Badge>
                        ) : (
                          <Badge color="danger" className="fs-6">
                            <FontAwesomeIcon icon="times-circle" className="me-1" />
                            <Translate contentKey="divipol.configuracion.inscripcion.cerrada">Inscripciones Cerradas</Translate>
                          </Badge>
                        )}
                      </div>
                    </div>
                    <Row>
                      <Col sm="6">
                        <div className="mb-3">
                          <Label className="text-muted small">
                            <Translate contentKey="divipol.configuracion.inscripcion.fechaInicio">Fecha de Inicio</Translate>
                          </Label>
                          <div className="fw-bold">{formatDate(inscripcion.fechaInicio)}</div>
                        </div>
                      </Col>
                      <Col sm="6">
                        <div className="mb-3">
                          <Label className="text-muted small">
                            <Translate contentKey="divipol.configuracion.inscripcion.fechaFin">Fecha de Fin</Translate>
                          </Label>
                          <div className="fw-bold">{formatDate(inscripcion.fechaFin)}</div>
                        </div>
                      </Col>
                    </Row>
                    <div>
                      <Label className="text-muted small">
                        <Translate contentKey="divipol.configuracion.fechaElecciones">Fecha de Elecciones</Translate>
                      </Label>
                      <div className="fw-bold">{formatDate(inscripcion.fechaElecciones)}</div>
                    </div>
                  </>
                )}
              </CardBody>
            </Card>
          </Col>

          {/* Límites de Remanentes */}
          <Col md="6" className="mb-4">
            <Card>
              <CardHeader>
                <FontAwesomeIcon icon="users" className="me-2" />
                <Translate contentKey="divipol.configuracion.limiteRemanentes.titulo">Límite de Testigos Remanentes</Translate>
              </CardHeader>
              <CardBody>
                <div className="mb-3">
                  <Label className="text-muted small">
                    <Translate contentKey="divipol.configuracion.limiteRemanentes.menos10Mesas">Puestos con menos de 10 mesas</Translate>
                  </Label>
                  <div className="fw-bold">{getConfigValue('max_remanentes_menos_10_mesas') || '1'} remanente máximo</div>
                </div>
                <div>
                  <Label className="text-muted small">
                    <Translate contentKey="divipol.configuracion.limiteRemanentes.porcentaje">
                      Porcentaje máximo del total de mesas
                    </Translate>
                  </Label>
                  <div className="fw-bold">{getConfigValue('max_remanentes_porcentaje') || '10'}%</div>
                </div>
              </CardBody>
            </Card>
          </Col>

          {/* All Configurations */}
          <Col md="12">
            <Card>
              <CardHeader>
                <FontAwesomeIcon icon="list" className="me-2" />
                Todas las Configuraciones
              </CardHeader>
              <CardBody>
                {configuraciones.length === 0 ? (
                  <div className="text-center py-4 text-muted">No hay configuraciones disponibles</div>
                ) : (
                  <Form>
                    {configuraciones.map(config => (
                      <FormGroup key={config.id} row className="mb-3">
                        <Label sm="4" className="fw-bold">
                          {config.clave.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}
                          {config.descripcion && <small className="d-block text-muted fw-normal">{config.descripcion}</small>}
                        </Label>
                        <Col sm="6">
                          <Input
                            type="text"
                            value={config.valor}
                            onChange={e => {
                              const newValue = e.target.value;
                              setConfiguraciones(prev => prev.map(c => (c.id === config.id ? { ...c, valor: newValue } : c)));
                            }}
                          />
                        </Col>
                        <Col sm="2">
                          <Button
                            color="primary"
                            size="sm"
                            onClick={() => handleUpdateConfig(config.clave, config.valor)}
                            disabled={saving === config.clave}
                          >
                            {saving === config.clave ? <Spinner size="sm" /> : <FontAwesomeIcon icon="save" />}
                          </Button>
                        </Col>
                      </FormGroup>
                    ))}
                  </Form>
                )}
              </CardBody>
            </Card>
          </Col>
        </Row>
      )}
    </Container>
  );
};

export default ConfiguracionElectoralPage;
