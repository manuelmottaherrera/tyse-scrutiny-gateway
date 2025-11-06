import React from 'react';
import { Container, Row, Col, Alert, Button } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
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
            <div className="header-content">
              <div className="header-text">
                <h1>
                  <Translate contentKey="divipol.title">DIVIPOL - Political Division</Translate>
                </h1>
                <p className="text-muted">
                  <Translate contentKey="divipol.subtitle">Management and query of Colombia&apos;s electoral political division</Translate>
                </p>
              </div>
              <div className="header-actions">
                <Button color="primary" href="http://186.31.4.135/Malla" target="_blank" rel="noopener noreferrer" className="malla-button">
                  <FontAwesomeIcon icon="table-cells" className="me-2" />
                  <Translate contentKey="divipol.mallaButton">Electoral Grid</Translate>
                </Button>
              </div>
            </div>
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
