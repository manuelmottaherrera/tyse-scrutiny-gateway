import React, { useEffect } from 'react';
import { Form, FormGroup, Label, Input, Row, Col, Button } from 'reactstrap';
import { Translate } from 'react-jhipster';
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
import './divipol-filters.scss';

export const DivipolFilters: React.FC = () => {
  const dispatch = useAppDispatch();
  const { departamentos, municipios, zonas, puestos, filters } = useAppSelector(state => state.divipol);

  useEffect(() => {
    dispatch(fetchDepartamentos());
    dispatch(fetchGeneralStats());
  }, []);

  const handleDepartamentoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const codDepto = e.target.value ? Number(e.target.value) : null;
    dispatch(setFilterDepartamento(codDepto));

    if (codDepto) {
      dispatch(fetchMunicipios(codDepto));
      dispatch(fetchStatsByDepartamento(codDepto));
    } else {
      dispatch(fetchGeneralStats());
    }
  };

  const handleMunicipioChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const codMpio = e.target.value ? Number(e.target.value) : null;
    dispatch(setFilterMunicipio(codMpio));

    if (codMpio && filters.departamento) {
      dispatch(fetchZonas({ codDepto: filters.departamento, codMpio }));
      dispatch(fetchStatsByMunicipio({ codDepto: filters.departamento, codMpio }));
    } else if (filters.departamento) {
      dispatch(fetchStatsByDepartamento(filters.departamento));
    }
  };

  const handleZonaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const codZona = e.target.value ? Number(e.target.value) : null;
    dispatch(setFilterZona(codZona));

    if (codZona && filters.departamento && filters.municipio) {
      dispatch(fetchPuestos({ codDepto: filters.departamento, codMpio: filters.municipio, codZona }));
      dispatch(fetchStatsByZona({ codDepto: filters.departamento, codMpio: filters.municipio, codZona }));
    } else if (filters.departamento && filters.municipio) {
      dispatch(fetchStatsByMunicipio({ codDepto: filters.departamento, codMpio: filters.municipio }));
    }
  };

  const handlePuestoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    dispatch(setFilterPuesto(e.target.value || null));
  };

  const handleReset = () => {
    dispatch(resetFilters());
    dispatch(fetchGeneralStats());
  };

  return (
    <div className="divipol-filters mb-4 p-3">
      <Form>
        <Row>
          <Col md={3}>
            <FormGroup>
              <Label for="departamento">
                <Translate contentKey="divipol.filters.departamento">Departamento</Translate>
              </Label>
              <Input type="select" id="departamento" value={filters.departamento || ''} onChange={handleDepartamentoChange}>
                <option value="">
                  <Translate contentKey="divipol.filters.selectDepartamento">Seleccione departamento</Translate>
                </option>
                {departamentos.map(dept => (
                  <option key={dept.coddepto} value={dept.coddepto}>
                    {dept.nomdepto}
                  </option>
                ))}
              </Input>
            </FormGroup>
          </Col>
          <Col md={3}>
            <FormGroup>
              <Label for="municipio">
                <Translate contentKey="divipol.filters.municipio">Municipio</Translate>
              </Label>
              <Input
                type="select"
                id="municipio"
                value={filters.municipio || ''}
                onChange={handleMunicipioChange}
                disabled={!filters.departamento}
              >
                <option value="">
                  <Translate contentKey="divipol.filters.selectMunicipio">Seleccione municipio</Translate>
                </option>
                {municipios.map(mpio => (
                  <option key={mpio.codmipio} value={mpio.codmipio}>
                    {mpio.nommipio}
                  </option>
                ))}
              </Input>
            </FormGroup>
          </Col>
          <Col md={3}>
            <FormGroup>
              <Label for="zona">
                <Translate contentKey="divipol.filters.zona">Zona</Translate>
              </Label>
              <Input type="select" id="zona" value={filters.zona || ''} onChange={handleZonaChange} disabled={!filters.municipio}>
                <option value="">
                  <Translate contentKey="divipol.filters.selectZona">Seleccione zona</Translate>
                </option>
                {zonas.map(zona => (
                  <option key={zona.codzona} value={zona.codzona}>
                    Zona {zona.codzona}
                  </option>
                ))}
              </Input>
            </FormGroup>
          </Col>
          <Col md={2}>
            <FormGroup>
              <Label>&nbsp;</Label>
              <div>
                <Button color="secondary" onClick={handleReset} block>
                  <Translate contentKey="divipol.filters.reset">Limpiar</Translate>
                </Button>
              </div>
            </FormGroup>
          </Col>
        </Row>
      </Form>
    </div>
  );
};
