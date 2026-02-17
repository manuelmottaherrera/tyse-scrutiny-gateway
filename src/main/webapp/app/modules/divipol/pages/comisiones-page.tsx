import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
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
import divipolService, { ComisionEscrutadora, ComisionEscrutadoraCreate, TipoComision } from 'app/shared/services/divipol.service';
import './comisiones-page.scss';

const TIPOS_COMISION: TipoComision[] = ['AUXILIAR', 'MUNICIPAL', 'DISTRITAL', 'GENERAL'];

export const ComisionesPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [comisiones, setComisiones] = useState<ComisionEscrutadora[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filterTipo, setFilterTipo] = useState<TipoComision | ''>('');

  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [editingComision, setEditingComision] = useState<ComisionEscrutadora | null>(null);
  const [formData, setFormData] = useState<ComisionEscrutadoraCreate>({
    nombre: '',
    tipo: 'AUXILIAR',
    ubicacion: '',
  });
  const [saving, setSaving] = useState(false);

  // Delete confirmation
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deletingComision, setDeletingComision] = useState<ComisionEscrutadora | null>(null);

  useEffect(() => {
    loadComisiones();
  }, [page, filterTipo]);

  const loadComisiones = async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await divipolService.getAllComisiones(page, 20, filterTipo || undefined);
      setComisiones(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError('Error al cargar las comisiones escrutadoras');
      console.error('Error loading comisiones:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreate = () => {
    setModalMode('create');
    setEditingComision(null);
    setFormData({ nombre: '', tipo: 'AUXILIAR', ubicacion: '' });
    setShowModal(true);
  };

  const handleOpenEdit = (comision: ComisionEscrutadora) => {
    setModalMode('edit');
    setEditingComision(comision);
    setFormData({
      nombre: comision.nombre,
      tipo: comision.tipo,
      ubicacion: comision.ubicacion || '',
    });
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingComision(null);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      if (modalMode === 'create') {
        await divipolService.createComision(formData);
      } else if (editingComision) {
        await divipolService.updateComision(editingComision.id, formData);
      }
      handleCloseModal();
      loadComisiones();
    } catch (err) {
      console.error('Error saving comision:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleConfirmDelete = (comision: ComisionEscrutadora) => {
    setDeletingComision(comision);
    setShowDeleteConfirm(true);
  };

  const handleDelete = async () => {
    if (!deletingComision) return;
    try {
      await divipolService.deleteComision(deletingComision.id);
      setShowDeleteConfirm(false);
      setDeletingComision(null);
      loadComisiones();
    } catch (err) {
      console.error('Error deleting comision:', err);
    }
  };

  const handleViewDetail = (comisionId: number) => {
    navigate(`/divipol/comisiones/${comisionId}`);
  };

  const getTipoBadgeColor = (tipo: TipoComision): string => {
    switch (tipo) {
      case 'AUXILIAR':
        return 'info';
      case 'MUNICIPAL':
        return 'primary';
      case 'DISTRITAL':
        return 'success';
      case 'GENERAL':
        return 'warning';
      default:
        return 'light';
    }
  };

  return (
    <Container fluid className="comisiones-page py-4">
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
                <FontAwesomeIcon icon="gavel" className="me-2 text-primary" />
                <Translate contentKey="divipol.comisiones.title">Comisiones Escrutadoras</Translate>
              </h2>
            </div>
            <Button color="primary" onClick={handleOpenCreate}>
              <FontAwesomeIcon icon="plus" className="me-2" />
              <Translate contentKey="divipol.comisiones.nuevo">Nueva Comisión</Translate>
            </Button>
          </div>
        </Col>
      </Row>

      {/* Filters */}
      <Row className="mb-3">
        <Col md="4">
          <FormGroup>
            <Label for="filterTipo">
              <Translate contentKey="divipol.comisiones.tipo">Tipo</Translate>
            </Label>
            <Input
              type="select"
              id="filterTipo"
              value={filterTipo}
              onChange={e => {
                setFilterTipo(e.target.value as TipoComision | '');
                setPage(0);
              }}
            >
              <option value="">Todos los tipos</option>
              {TIPOS_COMISION.map(tipo => (
                <option key={tipo} value={tipo}>
                  {translate(`divipol.comisiones.tipos.${tipo}`)}
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
          <Button color="link" className="p-0 ms-3" onClick={loadComisiones}>
            Reintentar
          </Button>
        </Alert>
      )}

      {/* Table */}
      {!loading && !error && (
        <Card>
          <CardBody>
            {comisiones.length === 0 ? (
              <div className="text-center py-4 text-muted">
                <FontAwesomeIcon icon="gavel" size="3x" className="mb-3" />
                <p>
                  <Translate contentKey="divipol.comisiones.empty">No hay comisiones escrutadoras registradas</Translate>
                </p>
              </div>
            ) : (
              <>
                <Table responsive striped hover>
                  <thead>
                    <tr>
                      <th>
                        <Translate contentKey="divipol.comisiones.nombre">Nombre</Translate>
                      </th>
                      <th>
                        <Translate contentKey="divipol.comisiones.tipo">Tipo</Translate>
                      </th>
                      <th>
                        <Translate contentKey="divipol.comisiones.ubicacion">Ubicación</Translate>
                      </th>
                      <th className="text-center">
                        <Translate contentKey="divipol.table.actions">Acciones</Translate>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {comisiones.map(comision => (
                      <tr key={comision.id}>
                        <td>{comision.nombre}</td>
                        <td>
                          <Badge color={getTipoBadgeColor(comision.tipo)}>{translate(`divipol.comisiones.tipos.${comision.tipo}`)}</Badge>
                        </td>
                        <td>{comision.ubicacion || '-'}</td>
                        <td className="text-center">
                          <Button
                            color="primary"
                            size="sm"
                            outline
                            className="me-2"
                            onClick={() => handleViewDetail(comision.id)}
                            title="Ver detalle"
                          >
                            <FontAwesomeIcon icon="eye" />
                          </Button>
                          <Button color="info" size="sm" outline className="me-2" onClick={() => handleOpenEdit(comision)} title="Editar">
                            <FontAwesomeIcon icon="edit" />
                          </Button>
                          <Button color="danger" size="sm" outline onClick={() => handleConfirmDelete(comision)} title="Eliminar">
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
            <Translate contentKey="divipol.comisiones.nuevo">Nueva Comisión</Translate>
          ) : (
            <Translate contentKey="divipol.comisiones.editar">Editar Comisión</Translate>
          )}
        </ModalHeader>
        <ModalBody>
          <Form>
            <FormGroup>
              <Label for="nombre">
                <Translate contentKey="divipol.comisiones.nombre">Nombre</Translate> *
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
              <Label for="tipo">
                <Translate contentKey="divipol.comisiones.tipo">Tipo</Translate> *
              </Label>
              <Input
                type="select"
                id="tipo"
                value={formData.tipo}
                onChange={e => setFormData({ ...formData, tipo: e.target.value as TipoComision })}
              >
                {TIPOS_COMISION.map(tipo => (
                  <option key={tipo} value={tipo}>
                    {translate(`divipol.comisiones.tipos.${tipo}`)}
                  </option>
                ))}
              </Input>
            </FormGroup>
            <FormGroup>
              <Label for="ubicacion">
                <Translate contentKey="divipol.comisiones.ubicacion">Ubicación</Translate>
              </Label>
              <Input
                type="text"
                id="ubicacion"
                value={formData.ubicacion}
                onChange={e => setFormData({ ...formData, ubicacion: e.target.value })}
              />
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
          ¿Está seguro que desea eliminar esta comisión?
          {deletingComision && <p className="mt-2 fw-bold">{deletingComision.nombre}</p>}
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

export default ComisionesPage;
