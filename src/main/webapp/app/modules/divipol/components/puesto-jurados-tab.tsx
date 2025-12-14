import React from 'react';
import { Table, Spinner, Alert } from 'reactstrap';
import { Translate } from 'react-jhipster';
import type { Jurado } from 'app/shared/services/divipol.service';
import './puesto-jurados-tab.scss';

interface PuestoJuradosTabProps {
  jurados: Jurado[];
  loading: boolean;
}

export const PuestoJuradosTab: React.FC<PuestoJuradosTabProps> = ({ jurados, loading }) => {
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

  if (jurados.length === 0) {
    return (
      <Alert color="info" className="puesto-jurados-empty">
        <i className="bi bi-info-circle me-2"></i>
        <Translate contentKey="divipol.puestoDetail.jurados.empty">No hay jurados asignados a este puesto</Translate>
      </Alert>
    );
  }

  return (
    <div className="puesto-jurados-tab">
      <div className="table-responsive">
        <Table hover size="sm" className="jurados-table">
          <thead>
            <tr>
              <th>
                <Translate contentKey="divipol.puestoDetail.jurados.documento">Documento</Translate>
              </th>
              <th>
                <Translate contentKey="divipol.puestoDetail.jurados.nombres">Nombres</Translate>
              </th>
              <th>
                <Translate contentKey="divipol.puestoDetail.jurados.apellidos">Apellidos</Translate>
              </th>
            </tr>
          </thead>
          <tbody>
            {jurados.map(jurado => (
              <tr key={jurado.id}>
                <td>
                  <span className="badge bg-secondary me-1">{jurado.tipoDocumento}</span>
                  <span className="font-monospace">{jurado.numeroDocumento}</span>
                </td>
                <td>{jurado.nombres}</td>
                <td>{jurado.apellidos}</td>
              </tr>
            ))}
          </tbody>
        </Table>
      </div>
      <div className="text-muted small mt-2">
        <i className="bi bi-lock me-1"></i>
        <Translate contentKey="divipol.puestoDetail.jurados.readOnly">
          Los jurados son datos de solo lectura provenientes de integración externa
        </Translate>
      </div>
    </div>
  );
};

export default PuestoJuradosTab;
