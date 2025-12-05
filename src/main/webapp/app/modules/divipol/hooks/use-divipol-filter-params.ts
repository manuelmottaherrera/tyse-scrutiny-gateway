import { useEffect, useCallback, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import {
  fetchDepartamentos,
  fetchMunicipios,
  fetchZonas,
  fetchPuestos,
  setFilterDepartamento,
  setFilterMunicipio,
  setFilterZona,
  setFilterPuesto,
  resetFilters,
  fetchGeneralStats,
  fetchStatsByDepartamento,
  fetchStatsByMunicipio,
  fetchStatsByZona,
} from '../divipol.reducer';

/**
 * Hook para sincronizar el estado de filtros con los query params de la URL.
 *
 * Formato de URL: /divipol?depto=05&mpio=001&zona=01&puesto=A1
 *
 * Parámetros:
 * - depto: código de departamento (2 dígitos)
 * - mpio: código de municipio (3 dígitos)
 * - zona: código de zona (1-2 dígitos)
 * - puesto: código de puesto (2 caracteres alfanuméricos)
 */
export const useDivipolFilterParams = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { filters, search } = useAppSelector(state => state.divipol);
  const initializedRef = useRef(false);
  const isUpdatingFromUrl = useRef(false);

  // Leer parámetros de URL al montar
  useEffect(() => {
    if (initializedRef.current) return;
    initializedRef.current = true;

    // Cargar departamentos siempre
    dispatch(fetchDepartamentos());

    // Usar URLSearchParams directamente desde window.location
    const urlParams = new URLSearchParams(window.location.search);
    const depto = urlParams.get('depto');
    const mpio = urlParams.get('mpio');
    const zona = urlParams.get('zona');
    const puesto = urlParams.get('puesto');

    // Si hay parámetros de filtro en la URL, restaurar el estado
    if (depto) {
      const codDepto = parseInt(depto, 10);
      if (!isNaN(codDepto)) {
        isUpdatingFromUrl.current = true;
        requestAnimationFrame(() => {
          dispatch(setFilterDepartamento(codDepto));
          dispatch(fetchMunicipios(codDepto));

          if (mpio) {
            const codMpio = parseInt(mpio, 10);
            if (!isNaN(codMpio)) {
              // Esperar a que se carguen los municipios antes de establecer el filtro
              setTimeout(() => {
                dispatch(setFilterMunicipio(codMpio));
                dispatch(fetchZonas({ codDepto, codMpio }));

                if (zona) {
                  const codZona = parseInt(zona, 10);
                  if (!isNaN(codZona)) {
                    setTimeout(() => {
                      dispatch(setFilterZona(codZona));
                      dispatch(fetchPuestos({ codDepto, codMpio, codZona }));

                      if (puesto) {
                        setTimeout(() => {
                          dispatch(setFilterPuesto(puesto));
                          dispatch(fetchStatsByZona({ codDepto, codMpio, codZona }));
                          isUpdatingFromUrl.current = false;
                        }, 100);
                      } else {
                        dispatch(fetchStatsByZona({ codDepto, codMpio, codZona }));
                        isUpdatingFromUrl.current = false;
                      }
                    }, 100);
                  }
                } else {
                  dispatch(fetchStatsByMunicipio({ codDepto, codMpio }));
                  isUpdatingFromUrl.current = false;
                }
              }, 100);
            }
          } else {
            dispatch(fetchStatsByDepartamento(codDepto));
            isUpdatingFromUrl.current = false;
          }
        });
      }
    } else {
      // No hay filtros en URL, cargar stats generales
      dispatch(fetchGeneralStats());
    }
  }, [dispatch]);

  // Actualizar URL cuando cambian los filtros (solo si no estamos leyendo de URL)
  useEffect(() => {
    if (isUpdatingFromUrl.current) return;
    if (!initializedRef.current) return;
    // No actualizar URL si hay búsqueda activa
    if (search.active) return;

    const params = new URLSearchParams();

    if (filters.departamento) {
      params.set('depto', String(filters.departamento).padStart(2, '0'));
    }
    if (filters.municipio) {
      params.set('mpio', String(filters.municipio).padStart(3, '0'));
    }
    if (filters.zona) {
      params.set('zona', String(filters.zona));
    }
    if (filters.puesto) {
      params.set('puesto', filters.puesto);
    }

    const searchString = params.toString();
    const newUrl = searchString ? `${location.pathname}?${searchString}` : location.pathname;

    // Solo actualizar si la URL cambió
    const currentUrl = `${location.pathname}${location.search}`;
    if (newUrl !== currentUrl) {
      navigate(newUrl, { replace: true });
    }
  }, [filters, navigate, location.pathname, search.active]);

  // Handler para cambio de departamento
  const handleDepartamentoChange = useCallback(
    (codDepto: number | null) => {
      dispatch(setFilterDepartamento(codDepto));

      if (codDepto) {
        dispatch(fetchMunicipios(codDepto));
        dispatch(fetchStatsByDepartamento(codDepto));
      } else {
        dispatch(fetchGeneralStats());
      }
    },
    [dispatch],
  );

  // Handler para cambio de municipio
  const handleMunicipioChange = useCallback(
    (codMpio: number | null) => {
      dispatch(setFilterMunicipio(codMpio));

      if (codMpio && filters.departamento) {
        dispatch(fetchZonas({ codDepto: filters.departamento, codMpio }));
        dispatch(fetchStatsByMunicipio({ codDepto: filters.departamento, codMpio }));
      } else if (filters.departamento) {
        dispatch(fetchStatsByDepartamento(filters.departamento));
      }
    },
    [dispatch, filters.departamento],
  );

  // Handler para cambio de zona
  const handleZonaChange = useCallback(
    (codZona: number | null) => {
      dispatch(setFilterZona(codZona));

      if (codZona && filters.departamento && filters.municipio) {
        dispatch(fetchPuestos({ codDepto: filters.departamento, codMpio: filters.municipio, codZona }));
        dispatch(fetchStatsByZona({ codDepto: filters.departamento, codMpio: filters.municipio, codZona }));
      } else if (filters.departamento && filters.municipio) {
        dispatch(fetchStatsByMunicipio({ codDepto: filters.departamento, codMpio: filters.municipio }));
      }
    },
    [dispatch, filters.departamento, filters.municipio],
  );

  // Handler para cambio de puesto
  const handlePuestoChange = useCallback(
    (codPuesto: string | null) => {
      dispatch(setFilterPuesto(codPuesto));
    },
    [dispatch],
  );

  // Handler para reset
  const handleReset = useCallback(() => {
    dispatch(resetFilters());
    dispatch(fetchGeneralStats());
    navigate(location.pathname, { replace: true });
  }, [dispatch, navigate, location.pathname]);

  return {
    handleDepartamentoChange,
    handleMunicipioChange,
    handleZonaChange,
    handlePuestoChange,
    handleReset,
  };
};

export default useDivipolFilterParams;
