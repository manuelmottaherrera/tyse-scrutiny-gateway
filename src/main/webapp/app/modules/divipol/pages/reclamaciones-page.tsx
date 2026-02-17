import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col, Card, CardBody, Table, Button, Spinner, Alert, Badge, FormGroup, Label, Input } from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import divipolService, { Reclamacion, EstadoReclamacion, TipoReclamacion } from 'app/shared/services/divipol.service';
import './reclamaciones-page.scss';

const TIPOS_RECLAMACION: TipoReclamacion[] = [
  'IRREGULARIDAD_MESA',
  'EXCESO_VOTANTES',
  'ERROR_ARITMETICO',
  'ERROR_NOMBRES',
  'FIRMAS_INSUFICIENTES',
  'DISCREPANCIA_ACTAS',
  'OTRO',
];

const ESTADOS_RECLAMACION: EstadoReclamacion[] = ['PRESENTADA', 'EN_REVISION', 'ACEPTADA', 'RECHAZADA'];

export const ReclamacionesPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reclamaciones, setReclamaciones] = useState<Reclamacion[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filterEstado, setFilterEstado] = useState<EstadoReclamacion | ''>('');
  const [filterTipo, setFilterTipo] = useState<TipoReclamacion | ''>('');

  useEffect(() => {
    loadReclamaciones();
  }, [page, filterEstado, filterTipo]);

  const loadReclamaciones = async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await divipolService.getAllReclamaciones(page, 20, filterEstado || undefined, filterTipo || undefined);
      setReclamaciones(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError('Error al cargar las reclamaciones');
      console.error('Error loading reclamaciones:', err);
    } finally {
      setLoading(false);
    }
  };

  const getEstadoBadgeColor = (estado: EstadoReclamacion): string => {
    switch (estado) {
      case 'PRESENTADA':
        return 'info';
      case 'EN_REVISION':
        return 'warning';
      case 'ACEPTADA':
        return 'success';
      case 'RECHAZADA':
        return 'danger';
      default:
        return 'light';
    }
  };

  const getTipoBadgeColor = (tipo: TipoReclamacion): string => {
    switch (tipo) {
      case 'IRREGULARIDAD_MESA':
        return 'danger';
      case 'EXCESO_VOTANTES':
        return 'warning';
      case 'ERROR_ARITMETICO':
        return 'info';
      case 'ERROR_NOMBRES':
        return 'secondary';
      case 'FIRMAS_INSUFICIENTES':
        return 'primary';
      case 'DISCREPANCIA_ACTAS':
        return 'dark';
      case 'OTRO':
        return 'light';
      default:
        return 'light';
    }
  };

  const formatDate = (dateStr: string): string => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-CO', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <Container fluid className="reclamaciones-page py-4">
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
              <FontAwesomeIcon icon="exclamation-circle" className="me-2 text-warning" />
              <Translate contentKey="divipol.reclamaciones.title">Reclamaciones Electorales</Translate>
            </h2>
          </div>
        </Col>
      </Row>

      {/* Filters */}
      <Row className="mb-3">
        <Col md="4">
          <FormGroup>
            <Label for="filterEstado">
              <Translate contentKey="divipol.reclamaciones.estado">Estado</Translate>
            </Label>
            <Input
              type="select"
              id="filterEstado"
              value={filterEstado}
              onChange={e => {
                setFilterEstado(e.target.value as EstadoReclamacion | '');
                setPage(0);
              }}
            >
              <option value="">Todos los estados</option>
              {ESTADOS_RECLAMACION.map(estado => (
                <option key={estado} value={estado}>
                  {translate(`divipol.reclamaciones.estados.${estado}`)}
                </option>
              ))}
            </Input>
          </FormGroup>
        </Col>
        <Col md="4">
          <FormGroup>
            <Label for="filterTipo">
              <Translate contentKey="divipol.reclamaciones.tipo">Tipo</Translate>
            </Label>
            <Input
              type="select"
              id="filterTipo"
              value={filterTipo}
              onChange={e => {
                setFilterTipo(e.target.value as TipoReclamacion | '');
                setPage(0);
              }}
            >
              <option value="">Todos los tipos</option>
              {TIPOS_RECLAMACION.map(tipo => (
                <option key={tipo} value={tipo}>
                  {translate(`divipol.reclamaciones.tipos.${tipo}`)}
                </option>
              ))}
            </Input>
          </FormGroup>
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
          <Button color="link" className="p-0 ms-3" onClick={loadReclamaciones}>
            Reintentar
          </Button>
        </Alert>
      )}

      {/* Table */}
      {!loading && !error && (
        <Card>
          <CardBody>
            {reclamaciones.length === 0 ? (
              <div className="text-center py-4 text-muted">
                <FontAwesomeIcon icon="exclamation-circle" size="3x" className="mb-3" />
                <p>
                  <Translate contentKey="divipol.reclamaciones.empty">No hay reclamaciones registradas</Translate>
                </p>
              </div>
            ) : (
              <>
                <Table responsive striped hover>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Testigo</th>
                      <th>
                        <Translate contentKey="divipol.reclamaciones.tipo">Tipo</Translate>
                      </th>
                      <th>
                        <Translate contentKey="divipol.reclamaciones.estado">Estado</Translate>
                      </th>
                      <th>
                        <Translate contentKey="divipol.reclamaciones.fechaPresentacion">Fecha</Translate>
                      </th>
                      <th>Mesa/Comisión</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reclamaciones.map(reclamacion => (
                      <tr key={reclamacion.id}>
                        <td>#{reclamacion.id}</td>
                        <td>
                          <div>{reclamacion.testigoNombreCompleto}</div>
                          <small className="text-muted">{reclamacion.testigoNumeroDocumento}</small>
                        </td>
                        <td>
                          <Badge color={getTipoBadgeColor(reclamacion.tipoReclamacion)}>
                            {translate(`divipol.reclamaciones.tipos.${reclamacion.tipoReclamacion}`)}
                          </Badge>
                        </td>
                        <td>
                          <Badge color={getEstadoBadgeColor(reclamacion.estado)}>
                            {translate(`divipol.reclamaciones.estados.${reclamacion.estado}`)}
                          </Badge>
                        </td>
                        <td>{formatDate(reclamacion.fechaPresentacion)}</td>
                        <td>
                          {reclamacion.mesaDescripcion && (
                            <span>
                              <FontAwesomeIcon icon="vote-yea" className="me-1" />
                              {reclamacion.mesaDescripcion}
                            </span>
                          )}
                          {reclamacion.comisionNombre && (
                            <span>
                              <FontAwesomeIcon icon="gavel" className="me-1" />
                              {reclamacion.comisionNombre}
                            </span>
                          )}
                          {!reclamacion.mesaDescripcion && !reclamacion.comisionNombre && <span className="text-muted">-</span>}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>

                {/* Pagination */}
                {totalPages > 1 && (
                  <div className="d-flex justify-content-center mt-3">
                    <Button color="secondary" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)} className="me-2">
                      <FontAwesomeIcon icon="chevron-left" />
                    </Button>
                    <span className="align-self-center">
                      Página {page + 1} de {totalPages}
                    </span>
                    <Button
                      color="secondary"
                      size="sm"
                      disabled={page >= totalPages - 1}
                      onClick={() => setPage(p => p + 1)}
                      className="ms-2"
                    >
                      <FontAwesomeIcon icon="chevron-right" />
                    </Button>
                  </div>
                )}
              </>
            )}
          </CardBody>
        </Card>
      )}
    </Container>
  );
};

export default ReclamacionesPage;
