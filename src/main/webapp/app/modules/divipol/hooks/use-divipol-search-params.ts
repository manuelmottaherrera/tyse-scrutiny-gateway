import { useEffect, useCallback, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { searchDivipol, setSearchMode, setSearchTerm, clearSearch } from '../divipol.reducer';
import { SearchMode } from 'app/shared/services/divipol.service';

/**
 * Hook para sincronizar el estado de búsqueda con los query params de la URL.
 *
 * Formato de URL: /divipol?q=0100199A1&mode=code&page=0&size=20
 *
 * Parámetros:
 * - q: término de búsqueda
 * - mode: 'code' | 'name'
 * - page: número de página (0-indexed)
 * - size: tamaño de página (default: 20)
 */
export const useDivipolSearchParams = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { search } = useAppSelector(state => state.divipol);
  const initializedRef = useRef(false);

  // Leer parámetros de URL al montar (usando window.location para evitar suspense)
  useEffect(() => {
    if (initializedRef.current) return;
    initializedRef.current = true;

    // Usar URLSearchParams directamente desde window.location
    const urlParams = new URLSearchParams(window.location.search);
    const q = urlParams.get('q');
    const mode = urlParams.get('mode') as SearchMode | null;
    const page = parseInt(urlParams.get('page') || '0', 10);
    const size = parseInt(urlParams.get('size') || '20', 10);

    if (q) {
      const searchMode: SearchMode = mode === 'code' || mode === 'name' ? mode : 'name';

      // Ejecutar después de que el componente esté montado
      requestAnimationFrame(() => {
        dispatch(setSearchMode(searchMode));
        dispatch(setSearchTerm(q));
        dispatch(searchDivipol({ q, mode: searchMode, page, size }));
      });
    }
  }, [dispatch]);

  // Función para actualizar URL con nuevos parámetros de búsqueda
  const updateSearchParams = useCallback(
    (params: { q: string; mode: SearchMode; page: number; size?: number }) => {
      const { q, mode, page, size = 20 } = params;

      if (q) {
        const searchString = new URLSearchParams({
          q,
          mode,
          page: String(page),
          ...(size !== 20 && { size: String(size) }),
        }).toString();

        navigate(`${location.pathname}?${searchString}`, { replace: true });
      } else {
        // Limpiar params si no hay búsqueda
        navigate(location.pathname, { replace: true });
      }
    },
    [navigate, location.pathname],
  );

  // Función para limpiar búsqueda y URL
  const clearSearchAndParams = useCallback(() => {
    dispatch(clearSearch());
    navigate(location.pathname, { replace: true });
  }, [dispatch, navigate, location.pathname]);

  // Función para ejecutar búsqueda y actualizar URL
  const executeSearch = useCallback(
    (params: { q: string; mode: SearchMode; page?: number; size?: number }) => {
      const { q, mode, page = 0, size = 20 } = params;

      if (q) {
        dispatch(searchDivipol({ q, mode, page, size }));
        updateSearchParams({ q, mode, page, size });
      }
    },
    [dispatch, updateSearchParams],
  );

  // Función para cambiar de página
  const changePage = useCallback(
    (newPage: number) => {
      const { term, mode, pagination } = search;
      if (term) {
        dispatch(searchDivipol({ q: term, mode, page: newPage, size: pagination.size }));
        updateSearchParams({ q: term, mode, page: newPage, size: pagination.size });
      }
    },
    [dispatch, search, updateSearchParams],
  );

  return {
    executeSearch,
    clearSearchAndParams,
    changePage,
    updateSearchParams,
  };
};

export default useDivipolSearchParams;
