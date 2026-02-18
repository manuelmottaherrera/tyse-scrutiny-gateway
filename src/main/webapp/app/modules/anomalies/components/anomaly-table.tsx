import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button, Badge, Pagination, PaginationItem, PaginationLink } from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppSelector, useAppDispatch } from 'app/config/store';
import { setPage } from '../anomaly.reducer';
import { Anomaly, AnomalySeverity, AnomalyStatus, AnomalyType } from 'app/shared/services/anomaly.service';

interface AnomalyTableProps {
  loading: boolean;
}

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

const formatDate = (dateStr: string): { date: string; time: string } => {
  const date = new Date(dateStr);
  const dateFormatted = date.toLocaleDateString('es-CO', {
    day: '2-digit',
    month: '2-digit',
    year: '2-digit',
  });
  const timeFormatted = date.toLocaleTimeString('es-CO', {
    hour: '2-digit',
    minute: '2-digit',
  });
  return { date: dateFormatted, time: timeFormatted };
};

const getTypeLabel = (type: AnomalyType): string => {
  const labels: Record<AnomalyType, string> = {
    PRECOUNT_DIFFERENCE: 'Diferencia Preconteo',
    VOTES_EXCEED_VOTERS: 'Votos > Votantes',
    SUM_MISMATCH: 'Suma Inconsistente',
  };
  return labels[type] || type;
};

export const AnomalyTable: React.FC<AnomalyTableProps> = ({ loading }) => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { anomalies, pagination } = useAppSelector(state => state.anomaly);

  const handleRowClick = (anomaly: Anomaly) => {
    navigate(`/anomalies/${anomaly.id}`);
  };

  const handlePageChange = (page: number) => {
    dispatch(setPage(page));
  };

  if (loading) {
    return (
      <div className="anomaly-table">
        <div className="text-center p-5">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
        </div>
      </div>
    );
  }

  if (!anomalies || anomalies.length === 0) {
    return (
      <div className="anomaly-table">
        <div className="alert alert-info mb-0">
          <Translate contentKey="anomalies.table.noData">No hay anomalías para mostrar</Translate>
        </div>
      </div>
    );
  }

  const renderPagination = () => {
    if (pagination.totalPages <= 1) return null;

    const pages = [];
    const maxVisiblePages = 5;
    let startPage = Math.max(0, pagination.page - Math.floor(maxVisiblePages / 2));
    const endPage = Math.min(pagination.totalPages - 1, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <PaginationItem key={i} active={i === pagination.page}>
          <PaginationLink onClick={() => handlePageChange(i)}>{i + 1}</PaginationLink>
        </PaginationItem>,
      );
    }

    return (
      <div className="d-flex justify-content-between align-items-center mt-3">
        <span className="pagination-info">
          Mostrando {pagination.page * pagination.size + 1} - {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)}{' '}
          de {pagination.totalElements}
        </span>
        <Pagination>
          <PaginationItem disabled={pagination.page === 0}>
            <PaginationLink first onClick={() => handlePageChange(0)} />
          </PaginationItem>
          <PaginationItem disabled={pagination.page === 0}>
            <PaginationLink previous onClick={() => handlePageChange(pagination.page - 1)} />
          </PaginationItem>
          {pages}
          <PaginationItem disabled={pagination.page >= pagination.totalPages - 1}>
            <PaginationLink next onClick={() => handlePageChange(pagination.page + 1)} />
          </PaginationItem>
          <PaginationItem disabled={pagination.page >= pagination.totalPages - 1}>
            <PaginationLink last onClick={() => handlePageChange(pagination.totalPages - 1)} />
          </PaginationItem>
        </Pagination>
      </div>
    );
  };

  return (
    <div className="anomaly-table">
      <Table responsive striped hover>
        <thead>
          <tr>
            <th>
              <Translate contentKey="anomalies.table.case">Caso</Translate>
            </th>
            <th>
              <Translate contentKey="anomalies.table.date">Fecha</Translate>
            </th>
            <th>
              <Translate contentKey="anomalies.table.type">Tipo</Translate>
            </th>
            <th>
              <Translate contentKey="anomalies.table.severity">Severidad</Translate>
            </th>
            <th>
              <Translate contentKey="anomalies.table.status">Estado</Translate>
            </th>
            <th>
              <Translate contentKey="anomalies.table.mesa">Mesa</Translate>
            </th>
            <th>
              <Translate contentKey="anomalies.table.candidate">Candidato</Translate>
            </th>
            <th>
              <Translate contentKey="anomalies.table.difference">Diferencia</Translate>
            </th>
            <th className="text-center">
              <Translate contentKey="anomalies.table.actions">Acciones</Translate>
            </th>
          </tr>
        </thead>
        <tbody>
          {anomalies.map(anomaly => (
            <tr key={anomaly.id} style={{ cursor: 'pointer' }} onClick={() => handleRowClick(anomaly)}>
              <td>
                <strong>#{anomaly.id}</strong>
              </td>
              <td>
                {formatDate(anomaly.detectedAt).date}
                <br />
                <small className="text-muted">{formatDate(anomaly.detectedAt).time}</small>
              </td>
              <td>
                <Badge className="badge-type">{getTypeLabel(anomaly.type)}</Badge>
              </td>
              <td>
                <Badge className={`badge-severity ${getSeverityBadgeClass(anomaly.severity)}`}>
                  {translate(`anomalies.severity.${anomaly.severity}`)}
                </Badge>
              </td>
              <td>
                <Badge className={`badge-status ${getStatusBadgeClass(anomaly.status)}`}>
                  {translate(`anomalies.status.${anomaly.status}`)}
                </Badge>
              </td>
              <td>{anomaly.votingTable || anomaly.divipolKey}</td>
              <td>
                {anomaly.candidateFirstName || anomaly.candidateLastName
                  ? `${anomaly.candidateFirstName || ''} ${anomaly.candidateLastName || ''}`.trim()
                  : '-'}
              </td>
              <td className={anomaly.difference && anomaly.difference > 0 ? 'text-danger fw-bold' : ''}>
                {anomaly.difference != null ? anomaly.difference.toLocaleString('es-CO') : '-'}
              </td>
              <td className="text-center" onClick={e => e.stopPropagation()}>
                <Button color="info" size="sm" outline onClick={() => handleRowClick(anomaly)} title="Ver detalle">
                  <FontAwesomeIcon icon="eye" />
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
      {renderPagination()}
    </div>
  );
};
