import React from 'react';
import { Container, Row, Col, Alert } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { useAppSelector } from 'app/config/store';
import { DivipolStatsCards } from './components/divipol-stats-cards';
import { DivipolFilters } from './components/divipol-filters';
import { DivipolTable } from './components/divipol-table';
import { DivipolExport } from './components/divipol-export';
import './divipol.scss';

export const DivipolPage: React.FC = () => {
  const { stats, loading, error } = useAppSelector(state => state.divipol);

  return (
    <Container fluid className="divipol-page p-4">
      <Row>
        <Col>
          <div className="page-header mb-4">
            <h1>
              <Translate contentKey="divipol.title">DIVIPOL - División Política</Translate>
            </h1>
            <p className="text-muted">
              <Translate contentKey="divipol.subtitle">Gestión y consulta de la división política electoral de Colombia</Translate>
            </p>
          </div>
        </Col>
      </Row>

      {error && (
        <Row>
          <Col>
            <Alert color="danger">
              <Translate contentKey="divipol.error">Error al cargar datos</Translate>: {error}
            </Alert>
          </Col>
        </Row>
      )}

      <Row>
        <Col>
          <DivipolFilters />
        </Col>
      </Row>

      <Row>
        <Col>
          <DivipolStatsCards stats={stats} loading={loading} />
        </Col>
      </Row>

      <Row>
        <Col>
          <DivipolTable />
        </Col>
      </Row>

      <Row>
        <Col>
          <DivipolExport />
        </Col>
      </Row>
    </Container>
  );
};

export default DivipolPage;
