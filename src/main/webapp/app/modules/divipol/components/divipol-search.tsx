import React, { useState, useCallback, useEffect, useRef } from 'react';
import { Input, InputGroup, InputGroupText, Button, FormGroup, Label, Alert, ListGroup, ListGroupItem, Spinner } from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { fetchSuggestions, setSearchMode, setSearchTerm, clearSuggestions } from '../divipol.reducer';
import { SearchMode, DivipolSearchResult } from 'app/shared/services/divipol.service';
import { DivipolCodeInput } from './divipol-code-input';
import './divipol-search.scss';

const MIN_SEARCH_LENGTH = 3;
const DEBOUNCE_MS = 300;

interface DivipolSearchProps {
  hasActiveFilters: boolean;
  onSearch: (params: { q: string; mode: SearchMode; page?: number; size?: number }) => void;
  onClear: () => void;
}

export const DivipolSearch: React.FC<DivipolSearchProps> = ({ hasActiveFilters, onSearch, onClear }) => {
  const dispatch = useAppDispatch();
  const { search } = useAppSelector(state => state.divipol);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const { mode, term, loading, suggestions, suggestionsLoading, active } = search;

  // Inicializar con ceros cuando está en modo código
  useEffect(() => {
    if (mode === 'code' && !term) {
      dispatch(setSearchTerm('000000000'));
    }
  }, [mode, term, dispatch]);

  // Cerrar sugerencias al hacer clic fuera
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Debounce para sugerencias
  const debouncedFetchSuggestions = useCallback(
    (q: string, searchMode: SearchMode) => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }

      if (searchMode === 'name' && q.length < MIN_SEARCH_LENGTH) {
        dispatch(clearSuggestions());
        return;
      }

      debounceRef.current = setTimeout(() => {
        dispatch(fetchSuggestions({ q, mode: searchMode }));
        setShowSuggestions(true);
      }, DEBOUNCE_MS);
    },
    [dispatch],
  );

  // Limpiar debounce al desmontar
  useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, []);

  const handleModeChange = (newMode: SearchMode) => {
    dispatch(setSearchMode(newMode));
    // En modo código, inicializar con ceros si está vacío
    if (newMode === 'code' && !term) {
      dispatch(setSearchTerm('000000000'));
    } else if (newMode === 'name' && term === '000000000') {
      // Limpiar si cambiamos a nombre y solo tenía ceros
      dispatch(setSearchTerm(''));
    }
    setShowSuggestions(false);
  };

  const handleTermChange = (newTerm: string) => {
    dispatch(setSearchTerm(newTerm));
    if (newTerm) {
      debouncedFetchSuggestions(newTerm, mode);
    } else {
      dispatch(clearSuggestions());
      setShowSuggestions(false);
    }
  };

  const handleSearch = useCallback(() => {
    // En modo nombre, requiere mínimo de caracteres
    if (mode === 'name' && term.length < MIN_SEARCH_LENGTH) {
      return;
    }
    // En modo código, permite búsqueda vacía (trae todos los resultados)

    onSearch({ q: term, mode, page: 0, size: 20 });
    setShowSuggestions(false);
  }, [term, mode, onSearch]);

  const handleSuggestionClick = (suggestion: DivipolSearchResult) => {
    // Primero cambiar modo (esto limpia el term), luego establecer el código
    dispatch(setSearchMode('code'));
    dispatch(setSearchTerm(suggestion.codigoDivipol));
    onSearch({ q: suggestion.codigoDivipol, mode: 'code', page: 0, size: 20 });
    setShowSuggestions(false);
  };

  const handleClearSearch = () => {
    onClear();
    setShowSuggestions(false);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleSearch();
    }
  };

  // Formatear nombre para mostrar en sugerencia
  const formatSuggestionName = (result: DivipolSearchResult): string => {
    switch (result.tipo) {
      case 'PAIS':
        return 'COLOMBIA';
      case 'DEPTO':
        return result.nomdepto;
      case 'MPIO':
        return `${result.nommipio}, ${result.nomdepto}`;
      case 'ZONA':
        return `Zona ${result.codzona}, ${result.nommipio}`;
      case 'PUESTO':
        return `${result.nompuesto}, ${result.nommipio}`;
      default:
        return result.nomdepto || result.nommipio || result.nompuesto || '';
    }
  };

  // Formato del tipo para badge
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

  const canSearch = mode === 'code' || term.length >= MIN_SEARCH_LENGTH;

  return (
    <div ref={containerRef} className="divipol-search">
      <div className="search-header">
        <h5 className="search-title">
          <FontAwesomeIcon icon="search" className="me-2" />
          <Translate contentKey="divipol.search.title">Buscar</Translate>
        </h5>

        {/* Switch de modo */}
        <div className="search-mode-toggle">
          <span className={`mode-label ${mode === 'name' ? 'active' : ''}`}>
            <Translate contentKey="divipol.search.modeName">Nombre</Translate>
          </span>
          <FormGroup switch inline className="mx-2 mb-0">
            <Input
              type="switch"
              role="switch"
              id="searchModeSwitch"
              checked={mode === 'code'}
              onChange={e => handleModeChange(e.target.checked ? 'code' : 'name')}
            />
          </FormGroup>
          <span className={`mode-label ${mode === 'code' ? 'active' : ''}`}>
            <Translate contentKey="divipol.search.modeCode">Código</Translate>
          </span>
        </div>
      </div>

      {/* Warning si hay filtros activos */}
      {hasActiveFilters && !active && (
        <Alert color="warning" className="search-warning mb-2" fade={false}>
          <FontAwesomeIcon icon="exclamation-triangle" className="me-2" />
          <Translate contentKey="divipol.search.filterWarning">Al buscar, los filtros actuales se eliminarán.</Translate>
        </Alert>
      )}

      {/* Campo de búsqueda */}
      <div className="search-input-container">
        {mode === 'code' ? (
          <DivipolCodeInput value={term} onChange={handleTermChange} onSearch={handleSearch} disabled={loading} />
        ) : (
          <InputGroup>
            <InputGroupText>
              <FontAwesomeIcon icon="search" />
            </InputGroupText>
            <Input
              type="text"
              value={term}
              onChange={e => handleTermChange(e.target.value)}
              onKeyDown={handleKeyDown}
              onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
              placeholder={translate('divipol.search.placeholder')}
              disabled={loading}
              minLength={MIN_SEARCH_LENGTH}
            />
            {term && (
              <Button color="light" onClick={() => handleTermChange('')} className="clear-input-btn">
                <FontAwesomeIcon icon="times" />
              </Button>
            )}
          </InputGroup>
        )}

        {/* Sugerencias */}
        {showSuggestions && suggestions.length > 0 && (
          <ListGroup className="suggestions-list">
            {suggestionsLoading && (
              <ListGroupItem className="suggestion-loading">
                <Spinner size="sm" className="me-2" />
                <Translate contentKey="divipol.search.loadingSuggestions">Cargando...</Translate>
              </ListGroupItem>
            )}
            {suggestions.map((suggestion, index) => (
              <ListGroupItem
                key={`${suggestion.codigoDivipol}-${index}`}
                tag="button"
                action
                onClick={() => handleSuggestionClick(suggestion)}
                className="suggestion-item"
              >
                <span className={`badge bg-${getTipoBadgeColor(suggestion.tipo)} me-2`}>{suggestion.tipo}</span>
                <span className="suggestion-name">{formatSuggestionName(suggestion)}</span>
                <span className="suggestion-code text-muted ms-auto">{suggestion.codigoDivipol}</span>
              </ListGroupItem>
            ))}
          </ListGroup>
        )}
      </div>

      {/* Botones de acción */}
      <div className="search-actions mt-2">
        <Button color="primary" onClick={handleSearch} disabled={loading || !canSearch} className="me-2">
          {loading ? (
            <>
              <Spinner size="sm" className="me-1" />
              <Translate contentKey="divipol.search.searching">Buscando...</Translate>
            </>
          ) : (
            <>
              <FontAwesomeIcon icon="search" className="me-1" />
              <Translate contentKey="divipol.search.searchButton">Buscar</Translate>
            </>
          )}
        </Button>

        {active && (
          <Button color="secondary" outline onClick={handleClearSearch}>
            <FontAwesomeIcon icon="times" className="me-1" />
            <Translate contentKey="divipol.search.clearButton">Limpiar búsqueda</Translate>
          </Button>
        )}
      </div>

      {/* Mensaje de mínimo caracteres */}
      {mode === 'name' && term.length > 0 && term.length < MIN_SEARCH_LENGTH && (
        <small className="text-muted mt-1 d-block">
          <Translate contentKey="divipol.search.minChars" interpolate={{ min: MIN_SEARCH_LENGTH }}>
            Mínimo {String(MIN_SEARCH_LENGTH)} caracteres
          </Translate>
        </small>
      )}
    </div>
  );
};

export default DivipolSearch;
