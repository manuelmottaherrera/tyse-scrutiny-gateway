import React from 'react';
import { Form, FormGroup, Label, Input, Row, Col, Button } from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { useAppSelector } from 'app/config/store';
import { useDivipolFilterParams } from '../hooks/use-divipol-filter-params';
import './divipol-filters.scss';

export const DivipolFilters: React.FC = () => {
  const { departamentos, municipios, zonas, puestos, filters } = useAppSelector(state => state.divipol);
  const { handleDepartamentoChange, handleMunicipioChange, handleZonaChange, handlePuestoChange, handleReset } = useDivipolFilterParams();

  const onDepartamentoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const codDepto = e.target.value ? Number(e.target.value) : null;
    handleDepartamentoChange(codDepto);
  };

  const onMunicipioChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const codMpio = e.target.value ? Number(e.target.value) : null;
    handleMunicipioChange(codMpio);
  };

  const onZonaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const codZona = e.target.value ? Number(e.target.value) : null;
    handleZonaChange(codZona);
  };

  const onPuestoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    handlePuestoChange(e.target.value || null);
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
              <Input type="select" id="departamento" value={filters.departamento || ''} onChange={onDepartamentoChange}>
                <option value="">{translate('divipol.filters.selectDepartamento')}</option>
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
                onChange={onMunicipioChange}
                disabled={!filters.departamento}
              >
                <option value="">{translate('divipol.filters.selectMunicipio')}</option>
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
              <Input type="select" id="zona" value={filters.zona || ''} onChange={onZonaChange} disabled={!filters.municipio}>
                <option value="">{translate('divipol.filters.selectZona')}</option>
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
