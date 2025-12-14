import React, { useState } from 'react';
import { Table, Spinner, Button, Alert } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { useAppSelector } from 'app/config/store';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import divipolService, { TestigoAsignado } from 'app/shared/services/divipol.service';
import { AsignarTestigoModal } from './asignar-testigo-modal';
import './puesto-testigos-tab.scss';

interface PuestoTestigosTabProps {
  testigos: TestigoAsignado[];
  loading: boolean;
  puestoId: number;
  onTestigoAsignado: () => void;
  onTestigoDesasignado: () => void;
}

export const PuestoTestigosTab: React.FC<PuestoTestigosTabProps> = ({
  testigos,
  loading,
  puestoId,
  onTestigoAsignado,
  onTestigoDesasignado,
}) => {
  const [showAsignarModal, setShowAsignarModal] = useState(false);
  const [desasignando, setDesasignando] = useState<number | null>(null);

  const authorities = useAppSelector(state => state.authentication.account.authorities);
  const canAssign = hasAnyAuthority(authorities, ['testigo.assign', 'ROLE_ADMIN']);
  const canUnassign = hasAnyAuthority(authorities, ['testigo.unassign', 'ROLE_ADMIN']);

  const handleDesasignar = async (testigoId: number) => {
    if (!window.confirm('¿Está seguro de desasignar este testigo del puesto?')) {
      return;
    }

    setDesasignando(testigoId);
    try {
      await divipolService.desasignarTestigoDePuesto(puestoId, testigoId);
      onTestigoDesasignado();
    } catch (err) {
      console.error('Error desasignando testigo:', err);
      alert('Error al desasignar el testigo');
    } finally {
      setDesasignando(null);
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-CO', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="text-center py-4">
        <Spinner size="sm" color="primary" />
        <span className="ms-2">
          <Translate contentKey="divipol.loading">Cargando...</Translate>
        </span>
      </div>
    );
  }

  return (
    <div className="puesto-testigos-tab">
      {canAssign && (
        <div className="mb-3 d-flex justify-content-end">
          <Button color="primary" size="sm" onClick={() => setShowAsignarModal(true)}>
            <i className="bi bi-plus-circle me-1"></i>
            <Translate contentKey="divipol.puestoDetail.testigos.asignar">Asignar Testigo</Translate>
          </Button>
        </div>
      )}

      {testigos.length === 0 ? (
        <Alert color="info" className="puesto-testigos-empty">
          <i className="bi bi-info-circle me-2"></i>
          <Translate contentKey="divipol.puestoDetail.testigos.empty">No hay testigos asignados a este puesto</Translate>
        </Alert>
      ) : (
        <div className="table-responsive">
          <Table hover size="sm" className="testigos-table">
            <thead>
              <tr>
                <th>
                  <Translate contentKey="divipol.puestoDetail.testigos.documento">Documento</Translate>
                </th>
                <th>
                  <Translate contentKey="divipol.puestoDetail.testigos.nombre">Nombre</Translate>
                </th>
                <th>
                  <Translate contentKey="divipol.puestoDetail.testigos.telefono">Teléfono</Translate>
                </th>
                <th>
                  <Translate contentKey="divipol.puestoDetail.testigos.asignadoPor">Asignado por</Translate>
                </th>
                <th>
                  <Translate contentKey="divipol.puestoDetail.testigos.fechaAsignacion">Fecha</Translate>
                </th>
                {canUnassign && (
                  <th className="text-center">
                    <Translate contentKey="divipol.puestoDetail.testigos.acciones">Acciones</Translate>
                  </th>
                )}
              </tr>
            </thead>
            <tbody>
              {testigos.map(testigo => (
                <tr key={testigo.testigoId}>
                  <td>
                    <span className="badge bg-secondary me-1">{testigo.tipoDocumento}</span>
                    <span className="font-monospace">{testigo.numeroDocumento}</span>
                  </td>
                  <td>{testigo.nombreCompleto}</td>
                  <td>{testigo.telefono || '-'}</td>
                  <td>{testigo.assignedBy || '-'}</td>
                  <td className="small">{formatDate(testigo.assignedDate)}</td>
                  {canUnassign && (
                    <td className="text-center">
                      <Button
                        color="danger"
                        size="sm"
                        outline
                        onClick={() => handleDesasignar(testigo.testigoId)}
                        disabled={desasignando === testigo.testigoId}
                      >
                        {desasignando === testigo.testigoId ? (
                          <Spinner size="sm" />
                        ) : (
                          <>
                            <i className="bi bi-x-circle me-1"></i>
                            <Translate contentKey="divipol.puestoDetail.testigos.desasignar">Desasignar</Translate>
                          </>
                        )}
                      </Button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      )}

      <AsignarTestigoModal
        isOpen={showAsignarModal}
        onClose={() => setShowAsignarModal(false)}
        puestoId={puestoId}
        onTestigoAsignado={() => {
          setShowAsignarModal(false);
          onTestigoAsignado();
        }}
        testigosYaAsignados={testigos.map(t => t.testigoId)}
      />
    </div>
  );
};

export default PuestoTestigosTab;
