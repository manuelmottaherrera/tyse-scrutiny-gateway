import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Container,
  Row,
  Col,
  Card,
  CardBody,
  Table,
  Button,
  Spinner,
  Alert,
  Badge,
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Form,
  FormGroup,
  Label,
  Input,
} from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import divipolService, { OrganizacionPolitica, OrganizacionPoliticaCreate, TipoOrganizacion } from 'app/shared/services/divipol.service';
import './organizaciones-page.scss';

const TIPOS_ORGANIZACION: TipoOrganizacion[] = ['PARTIDO', 'MOVIMIENTO', 'COALICION', 'GRUPO_SIGNIFICATIVO', 'COMITE_VOTO_BLANCO'];

export const OrganizacionesPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [organizaciones, setOrganizaciones] = useState<OrganizacionPolitica[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filterTipo, setFilterTipo] = useState<TipoOrganizacion | ''>('');

  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [editingOrg, setEditingOrg] = useState<OrganizacionPolitica | null>(null);
  const [formData, setFormData] = useState<OrganizacionPoliticaCreate>({
    nombre: '',
    sigla: '',
    tipo: 'PARTIDO',
  });
  const [saving, setSaving] = useState(false);

  // Delete confirmation
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deletingOrg, setDeletingOrg] = useState<OrganizacionPolitica | null>(null);

  useEffect(() => {
    loadOrganizaciones();
  }, [page, filterTipo]);

  const loadOrganizaciones = async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await divipolService.getAllOrganizaciones(page, 20, filterTipo || undefined);
      setOrganizaciones(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError('Error al cargar las organizaciones políticas');
      console.error('Error loading organizaciones:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreate = () => {
    setModalMode('create');
    setEditingOrg(null);
    setFormData({ nombre: '', sigla: '', tipo: 'PARTIDO' });
    setShowModal(true);
  };

  const handleOpenEdit = (org: OrganizacionPolitica) => {
    setModalMode('edit');
    setEditingOrg(org);
    setFormData({ nombre: org.nombre, sigla: org.sigla || '', tipo: org.tipo });
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingOrg(null);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      if (modalMode === 'create') {
        await divipolService.createOrganizacion(formData);
      } else if (editingOrg) {
        await divipolService.updateOrganizacion(editingOrg.id, formData);
      }
      handleCloseModal();
      loadOrganizaciones();
    } catch (err) {
      console.error('Error saving organizacion:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleConfirmDelete = (org: OrganizacionPolitica) => {
    setDeletingOrg(org);
    setShowDeleteConfirm(true);
  };

  const handleDelete = async () => {
    if (!deletingOrg) return;
    try {
      await divipolService.deleteOrganizacion(deletingOrg.id);
      setShowDeleteConfirm(false);
      setDeletingOrg(null);
      loadOrganizaciones();
    } catch (err) {
      console.error('Error deleting organizacion:', err);
    }
  };

  const getTipoBadgeColor = (tipo: TipoOrganizacion): string => {
    switch (tipo) {
      case 'PARTIDO':
        return 'primary';
      case 'MOVIMIENTO':
        return 'info';
      case 'COALICION':
        return 'success';
      case 'GRUPO_SIGNIFICATIVO':
        return 'warning';
      case 'COMITE_VOTO_BLANCO':
        return 'secondary';
      default:
        return 'light';
    }
  };

  return (
    <Container fluid className="organizaciones-page py-4">
      {/* Header */}
      <Row className="mb-3">
        <Col>
          <div className="d-flex align-items-center justify-content-between">
            <div className="d-flex align-items-center">
              <Link to="/divipol" className="btn btn-link p-0 me-3 text-decoration-none">
                <FontAwesomeIcon icon="arrow-left" className="me-2" />
                <span className="d-none d-sm-inline">
                  <Translate contentKey="divipol.title">DIVIPOL</Translate>
                </span>
              </Link>
              <h2 className="mb-0">
                <FontAwesomeIcon icon="building" className="me-2 text-primary" />
                <Translate contentKey="divipol.organizaciones.title">Organizaciones Políticas</Translate>
              </h2>
            </div>
            <Button color="primary" onClick={handleOpenCreate}>
              <FontAwesomeIcon icon="plus" className="me-2" />
              <Translate contentKey="divipol.organizaciones.nuevo">Nueva Organización</Translate>
            </Button>
          </div>
        </Col>
      </Row>

      {/* Filters */}
      <Row className="mb-3">
        <Col md="4">
          <FormGroup>
            <Label for="filterTipo">
              <Translate contentKey="divipol.organizaciones.tipo">Tipo</Translate>
            </Label>
            <Input
              type="select"
              id="filterTipo"
              value={filterTipo}
              onChange={e => {
                setFilterTipo(e.target.value as TipoOrganizacion | '');
                setPage(0);
              }}
            >
              <option value="">Todos los tipos</option>
              {TIPOS_ORGANIZACION.map(tipo => (
                <option key={tipo} value={tipo}>
                  {translate(`divipol.organizaciones.tipos.${tipo}`)}
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
          <Button color="link" className="p-0 ms-3" onClick={loadOrganizaciones}>
            Reintentar
          </Button>
        </Alert>
      )}

      {/* Table */}
      {!loading && !error && (
        <Card>
          <CardBody>
            {organizaciones.length === 0 ? (
              <div className="text-center py-4 text-muted">
                <FontAwesomeIcon icon="building" size="3x" className="mb-3" />
                <p>
                  <Translate contentKey="divipol.organizaciones.empty">No hay organizaciones políticas registradas</Translate>
                </p>
              </div>
            ) : (
              <>
                <Table responsive striped hover>
                  <thead>
                    <tr>
                      <th>
                        <Translate contentKey="divipol.organizaciones.nombre">Nombre</Translate>
                      </th>
                      <th>
                        <Translate contentKey="divipol.organizaciones.sigla">Sigla</Translate>
                      </th>
                      <th>
                        <Translate contentKey="divipol.organizaciones.tipo">Tipo</Translate>
                      </th>
                      <th className="text-center">
                        <Translate contentKey="divipol.table.actions">Acciones</Translate>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {organizaciones.map(org => (
                      <tr key={org.id}>
                        <td>{org.nombre}</td>
                        <td>{org.sigla || '-'}</td>
                        <td>
                          <Badge color={getTipoBadgeColor(org.tipo)}>{translate(`divipol.organizaciones.tipos.${org.tipo}`)}</Badge>
                        </td>
                        <td className="text-center">
                          <Button color="info" size="sm" outline className="me-2" onClick={() => handleOpenEdit(org)} title="Editar">
                            <FontAwesomeIcon icon="edit" />
                          </Button>
                          <Button color="danger" size="sm" outline onClick={() => handleConfirmDelete(org)} title="Eliminar">
                            <FontAwesomeIcon icon="trash" />
                          </Button>
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

      {/* Create/Edit Modal */}
      <Modal isOpen={showModal} toggle={handleCloseModal}>
        <ModalHeader toggle={handleCloseModal}>
          {modalMode === 'create' ? (
            <Translate contentKey="divipol.organizaciones.nuevo">Nueva Organización</Translate>
          ) : (
            <Translate contentKey="divipol.organizaciones.editar">Editar Organización</Translate>
          )}
        </ModalHeader>
        <ModalBody>
          <Form>
            <FormGroup>
              <Label for="nombre">
                <Translate contentKey="divipol.organizaciones.nombre">Nombre</Translate> *
              </Label>
              <Input
                type="text"
                id="nombre"
                value={formData.nombre}
                onChange={e => setFormData({ ...formData, nombre: e.target.value })}
                required
              />
            </FormGroup>
            <FormGroup>
              <Label for="sigla">
                <Translate contentKey="divipol.organizaciones.sigla">Sigla</Translate>
              </Label>
              <Input
                type="text"
                id="sigla"
                value={formData.sigla}
                onChange={e => setFormData({ ...formData, sigla: e.target.value })}
                maxLength={20}
              />
            </FormGroup>
            <FormGroup>
              <Label for="tipo">
                <Translate contentKey="divipol.organizaciones.tipo">Tipo</Translate> *
              </Label>
              <Input
                type="select"
                id="tipo"
                value={formData.tipo}
                onChange={e => setFormData({ ...formData, tipo: e.target.value as TipoOrganizacion })}
              >
                {TIPOS_ORGANIZACION.map(tipo => (
                  <option key={tipo} value={tipo}>
                    {translate(`divipol.organizaciones.tipos.${tipo}`)}
                  </option>
                ))}
              </Input>
            </FormGroup>
          </Form>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={handleCloseModal}>
            <Translate contentKey="divipol.common.cancelar">Cancelar</Translate>
          </Button>
          <Button color="primary" onClick={handleSave} disabled={saving || !formData.nombre.trim()}>
            {saving ? <Spinner size="sm" /> : <FontAwesomeIcon icon="save" className="me-2" />}
            <Translate contentKey="divipol.common.guardar">Guardar</Translate>
          </Button>
        </ModalFooter>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal isOpen={showDeleteConfirm} toggle={() => setShowDeleteConfirm(false)}>
        <ModalHeader toggle={() => setShowDeleteConfirm(false)}>Confirmar Eliminación</ModalHeader>
        <ModalBody>
          <Translate contentKey="divipol.organizaciones.confirmarEliminar">¿Está seguro que desea eliminar esta organización?</Translate>
          {deletingOrg && <p className="mt-2 fw-bold">{deletingOrg.nombre}</p>}
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={() => setShowDeleteConfirm(false)}>
            <Translate contentKey="divipol.common.cancelar">Cancelar</Translate>
          </Button>
          <Button color="danger" onClick={handleDelete}>
            <FontAwesomeIcon icon="trash" className="me-2" />
            <Translate contentKey="divipol.common.eliminar">Eliminar</Translate>
          </Button>
        </ModalFooter>
      </Modal>
    </Container>
  );
};

export default OrganizacionesPage;
