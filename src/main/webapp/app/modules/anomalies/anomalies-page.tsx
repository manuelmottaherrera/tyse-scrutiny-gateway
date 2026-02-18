import React, { useEffect } from 'react';
import { Container, Row, Col, Alert } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { useAppSelector, useAppDispatch } from 'app/config/store';
import { fetchAnomalies, fetchAnomalyStats } from './anomaly.reducer';
import { AnomalyStatsCards } from './components/anomaly-stats-cards';
import { AnomalyFilters } from './components/anomaly-filters';
import { AnomalyTable } from './components/anomaly-table';
import './anomalies.scss';

export const AnomaliesPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const { loading, error, filters, stats, loadingStats } = useAppSelector(state => state.anomaly);

  useEffect(() => {
    dispatch(fetchAnomalies());
    dispatch(fetchAnomalyStats(filters.electionProcessId));
  }, [dispatch, filters.electionProcessId]);

  // Recargar anomalías cuando cambian los filtros
  useEffect(() => {
    dispatch(fetchAnomalies());
  }, [dispatch, filters.status, filters.type, filters.severity]);

  return (
    <Container fluid className="anomalies-page p-4">
      <Row>
        <Col>
          <div className="page-header mb-4">
            <div className="header-content">
              <div className="header-text">
                <h1>
                  <Translate contentKey="anomalies.title">Anomalías Electorales</Translate>
                </h1>
                <p className="text-muted">
                  <Translate contentKey="anomalies.subtitle">Gestión y seguimiento de anomalías detectadas en el escrutinio</Translate>
                </p>
              </div>
            </div>
          </div>
        </Col>
      </Row>

      {error && (
        <Row>
          <Col>
            <Alert color="danger" fade={false}>
              <Translate contentKey="anomalies.error">Error al cargar anomalías</Translate>: {error}
            </Alert>
          </Col>
        </Row>
      )}

      <Row>
        <Col>
          <AnomalyStatsCards stats={stats} loading={loadingStats} />
        </Col>
      </Row>

      <Row>
        <Col>
          <AnomalyFilters />
        </Col>
      </Row>

      <Row>
        <Col>
          <AnomalyTable loading={loading} />
        </Col>
      </Row>
    </Container>
  );
};

export default AnomaliesPage;
