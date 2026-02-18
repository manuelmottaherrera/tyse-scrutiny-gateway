import React from 'react';
import { Form, FormGroup, Label, Input, Row, Col, Button } from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { useAppSelector, useAppDispatch } from 'app/config/store';
import { setFilterStatus, setFilterType, setFilterSeverity, clearFilters } from '../anomaly.reducer';
import { AnomalyStatus, AnomalyType, AnomalySeverity } from 'app/shared/services/anomaly.service';

export const AnomalyFilters: React.FC = () => {
  const dispatch = useAppDispatch();
  const { filters } = useAppSelector(state => state.anomaly);

  const onStatusChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value as AnomalyStatus | '';
    dispatch(setFilterStatus(value || null));
  };

  const onTypeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value as AnomalyType | '';
    dispatch(setFilterType(value || null));
  };

  const onSeverityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value as AnomalySeverity | '';
    dispatch(setFilterSeverity(value || null));
  };

  const handleReset = () => {
    dispatch(clearFilters());
  };

  return (
    <div className="anomaly-filters mb-4 p-3">
      <Form>
        <Row>
          <Col md={3}>
            <FormGroup>
              <Label for="status">
                <Translate contentKey="anomalies.filters.status">Estado</Translate>
              </Label>
              <Input type="select" id="status" value={filters.status || ''} onChange={onStatusChange}>
                <option value="">{translate('anomalies.filters.allStatuses')}</option>
                <option value="NEW">{translate('anomalies.status.NEW')}</option>
                <option value="REVIEWED">{translate('anomalies.status.REVIEWED')}</option>
                <option value="CLAIMED">{translate('anomalies.status.CLAIMED')}</option>
                <option value="DISMISSED">{translate('anomalies.status.DISMISSED')}</option>
              </Input>
            </FormGroup>
          </Col>
          <Col md={3}>
            <FormGroup>
              <Label for="type">
                <Translate contentKey="anomalies.filters.type">Tipo</Translate>
              </Label>
              <Input type="select" id="type" value={filters.type || ''} onChange={onTypeChange}>
                <option value="">{translate('anomalies.filters.allTypes')}</option>
                <option value="PRECOUNT_DIFFERENCE">{translate('anomalies.type.PRECOUNT_DIFFERENCE')}</option>
                <option value="VOTES_EXCEED_VOTERS">{translate('anomalies.type.VOTES_EXCEED_VOTERS')}</option>
                <option value="SUM_MISMATCH">{translate('anomalies.type.SUM_MISMATCH')}</option>
              </Input>
            </FormGroup>
          </Col>
          <Col md={3}>
            <FormGroup>
              <Label for="severity">
                <Translate contentKey="anomalies.filters.severity">Severidad</Translate>
              </Label>
              <Input type="select" id="severity" value={filters.severity || ''} onChange={onSeverityChange}>
                <option value="">{translate('anomalies.filters.allSeverities')}</option>
                <option value="CRITICAL">{translate('anomalies.severity.CRITICAL')}</option>
                <option value="HIGH">{translate('anomalies.severity.HIGH')}</option>
                <option value="MEDIUM">{translate('anomalies.severity.MEDIUM')}</option>
                <option value="LOW">{translate('anomalies.severity.LOW')}</option>
              </Input>
            </FormGroup>
          </Col>
          <Col md={3}>
            <FormGroup>
              <Label>&nbsp;</Label>
              <div>
                <Button color="secondary" onClick={handleReset} block>
                  <Translate contentKey="anomalies.filters.reset">Limpiar</Translate>
                </Button>
              </div>
            </FormGroup>
          </Col>
        </Row>
      </Form>
    </div>
  );
};
