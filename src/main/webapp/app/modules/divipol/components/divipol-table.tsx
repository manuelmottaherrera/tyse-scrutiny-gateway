import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppSelector } from 'app/config/store';
import { hasAnyAuthority } from 'app/shared/auth/private-route';

/**
 * Genera el código divipol concatenado según las reglas:
 * - codDepto: 2 dígitos (ej: '01')
 * - codMipio: 3 dígitos (ej: '001')
 * - codZona: 2 dígitos (ej: '02')
 * - codPuesto: 2 caracteres alfanuméricos (ej: '00', 'A1')
 * - Si no aplica, se completa con ceros
 * - Resultado: código de 9 dígitos sin espacios (ej: '010010201')
 */
export const generateDivipolCode = (
  codDepto?: number | string,
  codMipio?: number | string,
  codZona?: number | string,
  codPuesto?: number | string,
): string => {
  const formatDepto = codDepto != null ? String(codDepto).padStart(2, '0') : '00';
  const formatMipio = codMipio != null ? String(codMipio).padStart(3, '0') : '000';
  const formatZona = codZona != null ? String(codZona).padStart(2, '0') : '00';
  const formatPuesto = codPuesto != null ? String(codPuesto).padStart(2, '0') : '00';

  return `${formatDepto}${formatMipio}${formatZona}${formatPuesto}`;
};

export const DivipolTable: React.FC = () => {
  const navigate = useNavigate();
  const { departamentos, municipios, zonas, puestos, filters } = useAppSelector(state => state.divipol);
  const authorities = useAppSelector(state => state.authentication.account.authorities);
  const canViewDetail = hasAnyAuthority(authorities, ['puesto.detail.read', 'ROLE_ADMIN']);

  const handleShowDetail = (puestoId: number) => {
    navigate(`/divipol/puestos/${puestoId}`);
  };

  const showingPuestos = filters.zona && puestos.length > 0;

  const renderTableData = () => {
    if (filters.zona && puestos.length > 0) {
      return puestos.map(puesto => (
        <tr key={`${puesto.coddepto}-${puesto.codmipio}-${puesto.codzona}-${puesto.codpuesto}`}>
          <td>{generateDivipolCode(puesto.coddepto, puesto.codmipio, puesto.codzona, puesto.codpuesto)}</td>
          <td>{puesto.nompuesto}</td>
          <td>{puesto.potencialFemenino?.toLocaleString('es-CO')}</td>
          <td>{puesto.potencialMasculino?.toLocaleString('es-CO')}</td>
          <td>{puesto.potencialTotal?.toLocaleString('es-CO')}</td>
          <td>{puesto.mesas}</td>
          {canViewDetail && (
            <td className="text-center">
              <Button color="info" size="sm" outline onClick={() => handleShowDetail(puesto.iddivipol)} title="Ver detalle">
                <FontAwesomeIcon icon="eye" />
              </Button>
            </td>
          )}
        </tr>
      ));
    } else if (filters.municipio && zonas.length > 0) {
      return zonas.map(zona => (
        <tr key={`${zona.coddepto}-${zona.codmipio}-${zona.codzona}`}>
          <td>{generateDivipolCode(zona.coddepto, zona.codmipio, zona.codzona)}</td>
          <td>Zona {zona.codzona}</td>
          <td>{zona.potencialFemenino?.toLocaleString('es-CO')}</td>
          <td>{zona.potencialMasculino?.toLocaleString('es-CO')}</td>
          <td>{zona.potencialTotal?.toLocaleString('es-CO')}</td>
          <td>{zona.mesas}</td>
        </tr>
      ));
    } else if (filters.departamento && municipios.length > 0) {
      return municipios.map(municipio => (
        <tr key={`${municipio.coddepto}-${municipio.codmipio}`}>
          <td>{generateDivipolCode(municipio.coddepto, municipio.codmipio)}</td>
          <td>{municipio.nommipio}</td>
          <td>{municipio.potencialFemenino?.toLocaleString('es-CO')}</td>
          <td>{municipio.potencialMasculino?.toLocaleString('es-CO')}</td>
          <td>{municipio.potencialTotal?.toLocaleString('es-CO')}</td>
          <td>{municipio.mesas}</td>
        </tr>
      ));
    } else if (departamentos.length > 0) {
      return departamentos.map(depto => (
        <tr key={depto.coddepto}>
          <td>{generateDivipolCode(depto.coddepto)}</td>
          <td>{depto.nomdepto}</td>
          <td>{depto.mujeres?.toLocaleString('es-CO')}</td>
          <td>{depto.hombres?.toLocaleString('es-CO')}</td>
          <td>{depto.totalPotencial?.toLocaleString('es-CO')}</td>
          <td>{depto.mesas}</td>
        </tr>
      ));
    }
    return null;
  };

  const hasData = departamentos.length > 0 || municipios.length > 0 || zonas.length > 0 || puestos.length > 0;

  if (!hasData) {
    return (
      <div className="alert alert-info">
        <Translate contentKey="divipol.table.noData">No hay datos para mostrar</Translate>
      </div>
    );
  }

  return (
    <div className="divipol-table mt-4">
      <Table responsive striped hover>
        <thead>
          <tr>
            <th>
              <Translate contentKey="divipol.table.code">Cod. Divipol</Translate>
            </th>
            <th>
              <Translate contentKey="divipol.table.name">Nombre</Translate>
            </th>
            <th>
              <Translate contentKey="divipol.table.women">Mujeres</Translate>
            </th>
            <th>
              <Translate contentKey="divipol.table.men">Hombres</Translate>
            </th>
            <th>
              <Translate contentKey="divipol.table.total">Total</Translate>
            </th>
            <th>
              <Translate contentKey="divipol.table.mesas">Mesas</Translate>
            </th>
            {showingPuestos && canViewDetail && (
              <th className="text-center">
                <Translate contentKey="divipol.table.actions">Acciones</Translate>
              </th>
            )}
          </tr>
        </thead>
        <tbody>{renderTableData()}</tbody>
      </Table>
    </div>
  );
};
