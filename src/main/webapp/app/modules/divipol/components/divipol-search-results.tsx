import React from 'react';
import { Table, Badge, Spinner, Alert, Button } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppSelector } from 'app/config/store';
import { DivipolSearchResult } from 'app/shared/services/divipol.service';
import './divipol-search-results.scss';

interface PaginationProps {
  page: number;
  totalPages: number;
  totalElements: number;
  onPageChange: (page: number) => void;
}

const SearchPagination: React.FC<PaginationProps> = ({ page, totalPages, totalElements, onPageChange }) => {
  const pages = [];
  const maxVisiblePages = 5;

  let startPage = Math.max(0, page - Math.floor(maxVisiblePages / 2));
  const endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

  if (endPage - startPage < maxVisiblePages - 1) {
    startPage = Math.max(0, endPage - maxVisiblePages + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }

  return (
    <div className="search-pagination">
      <div className="pagination-info">
        <Translate contentKey="divipol.search.results.count" interpolate={{ count: totalElements }}>
          {String(totalElements)} resultados
        </Translate>
      </div>
      <div className="pagination-controls">
        <Button color="light" size="sm" disabled={page === 0} onClick={() => onPageChange(0)} className="me-1">
          <FontAwesomeIcon icon="angles-left" />
        </Button>
        <Button color="light" size="sm" disabled={page === 0} onClick={() => onPageChange(page - 1)} className="me-1">
          <FontAwesomeIcon icon="angle-left" />
        </Button>

        {pages.map(p => (
          <Button key={p} color={p === page ? 'primary' : 'light'} size="sm" onClick={() => onPageChange(p)} className="me-1">
            {p + 1}
          </Button>
        ))}

        <Button color="light" size="sm" disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)} className="me-1">
          <FontAwesomeIcon icon="angle-right" />
        </Button>
        <Button color="light" size="sm" disabled={page >= totalPages - 1} onClick={() => onPageChange(totalPages - 1)}>
          <FontAwesomeIcon icon="angles-right" />
        </Button>
      </div>
    </div>
  );
};

interface DivipolSearchResultsProps {
  onPageChange: (page: number) => void;
  onClear: () => void;
}

export const DivipolSearchResults: React.FC<DivipolSearchResultsProps> = ({ onPageChange, onClear }) => {
  const { search } = useAppSelector(state => state.divipol);
  const { active, results, loading, pagination, term, mode } = search;

  if (!active) {
    return null;
  }

  const getTipoBadgeColor = (tipo: string): string => {
    switch (tipo) {
      case 'PAIS':
        return 'dark';
      case 'DEPTO':
        return 'primary';
      case 'MPIO':
        return 'success';
      case 'ZONA':
        return 'warning';
      case 'PUESTO':
        return 'info';
      default:
        return 'secondary';
    }
  };

  const formatName = (result: DivipolSearchResult): string => {
    switch (result.tipo) {
      case 'PAIS':
        return 'COLOMBIA';
      case 'DEPTO':
        return result.nomdepto;
      case 'MPIO':
        return result.nommipio;
      case 'ZONA':
        return `Zona ${result.codzona}`;
      case 'PUESTO':
        return result.nompuesto;
      default:
        return '-';
    }
  };

  const formatLocation = (result: DivipolSearchResult): string => {
    const parts: string[] = [];
    if (result.tipo !== 'PAIS' && result.tipo !== 'DEPTO' && result.nomdepto) {
      parts.push(result.nomdepto);
    }
    if (result.tipo === 'ZONA' || result.tipo === 'PUESTO') {
      if (result.nommipio) parts.push(result.nommipio);
    }
    if (result.tipo === 'PUESTO' && result.codzona != null) {
      parts.push(`Zona ${result.codzona}`);
    }
    return parts.join(', ');
  };

  return (
    <div className="divipol-search-results">
      {/* Header con info de búsqueda */}
      <div className="search-results-header">
        <div className="search-info">
          <FontAwesomeIcon icon="search" className="me-2" />
          <span>
            <Translate contentKey="divipol.search.results.searching">Búsqueda:</Translate>
          </span>
          <Badge color="dark" className="ms-2">
            {term}
          </Badge>
          <Badge color={mode === 'code' ? 'info' : 'secondary'} className="ms-1">
            {mode === 'code' ? 'Código' : 'Nombre'}
          </Badge>
        </div>
        <Button color="link" onClick={onClear} className="clear-search-link">
          <FontAwesomeIcon icon="times" className="me-1" />
          <Translate contentKey="divipol.search.clearButton">Limpiar</Translate>
        </Button>
      </div>

      {/* Loading state */}
      {loading && (
        <div className="search-loading text-center py-4">
          <Spinner color="primary" />
          <p className="mt-2 text-muted">
            <Translate contentKey="divipol.search.searching">Buscando...</Translate>
          </p>
        </div>
      )}

      {/* No results */}
      {!loading && results.length === 0 && (
        <Alert color="info" className="text-center" fade={false}>
          <FontAwesomeIcon icon="info-circle" className="me-2" />
          <Translate contentKey="divipol.search.noResults">No se encontraron resultados.</Translate>
        </Alert>
      )}

      {/* Results table */}
      {!loading && results.length > 0 && (
        <>
          <Table responsive hover className="search-results-table">
            <thead>
              <tr>
                <th>
                  <Translate contentKey="divipol.search.results.code">Cod. Divipol</Translate>
                </th>
                <th>
                  <Translate contentKey="divipol.search.results.type">Tipo</Translate>
                </th>
                <th>
                  <Translate contentKey="divipol.search.results.name">Nombre</Translate>
                </th>
                <th>
                  <Translate contentKey="divipol.search.results.location">Ubicación</Translate>
                </th>
                <th className="text-end">
                  <Translate contentKey="divipol.search.results.potential">Potencial</Translate>
                </th>
                <th className="text-end">
                  <Translate contentKey="divipol.search.results.tables">Mesas</Translate>
                </th>
              </tr>
            </thead>
            <tbody>
              {results.map((result, index) => (
                <tr key={`${result.codigoDivipol}-${index}`}>
                  <td className="code-cell">
                    <code>{result.codigoDivipol}</code>
                  </td>
                  <td>
                    <Badge color={getTipoBadgeColor(result.tipo)}>{result.tipo}</Badge>
                  </td>
                  <td className="name-cell">{formatName(result)}</td>
                  <td className="location-cell text-muted">{formatLocation(result)}</td>
                  <td className="text-end">{result.potencialTotal?.toLocaleString() || '-'}</td>
                  <td className="text-end">{result.mesas?.toLocaleString() || '-'}</td>
                </tr>
              ))}
            </tbody>
          </Table>

          {/* Pagination */}
          {pagination.totalPages > 1 && (
            <SearchPagination
              page={pagination.page}
              totalPages={pagination.totalPages}
              totalElements={pagination.totalElements}
              onPageChange={onPageChange}
            />
          )}
        </>
      )}
    </div>
  );
};

export default DivipolSearchResults;
