import React from 'react';
import { Card, CardBody, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { DivipolStats } from 'app/shared/services/divipol.service';
import './divipol-stats-cards.scss';

interface DivipolStatsCardsProps {
  stats: DivipolStats | null;
  loading: boolean;
}

export const DivipolStatsCards: React.FC<DivipolStatsCardsProps> = ({ stats, loading }) => {
  if (loading) {
    return (
      <div className="text-center p-4">
        <div className="spinner-border" role="status">
          <span className="sr-only">Cargando...</span>
        </div>
      </div>
    );
  }

  if (!stats) {
    return null;
  }

  const cards = [
    {
      title: <Translate contentKey="divipol.stats.departamentos">Departamentos</Translate>,
      total: stats.totalDepartamentos,
      color: '#008cba',
    },
    {
      title: <Translate contentKey="divipol.stats.municipios">Municipios</Translate>,
      total: stats.totalMunicipios,
      color: '#43ac6a',
    },
    {
      title: <Translate contentKey="divipol.stats.zonas">Zonas</Translate>,
      total: stats.totalZonas,
      color: '#e99002',
    },
    {
      title: <Translate contentKey="divipol.stats.mesas">Mesas</Translate>,
      total: stats.totalMesas,
      color: '#f04124',
    },
  ];

  return (
    <div className="divipol-stats-cards">
      <Row>
        {cards.map((card, index) => (
          <Col key={index} md={3} sm={6} className="mb-4">
            <Card className="stats-card" style={{ borderTop: `4px solid ${card.color}` }}>
              <CardBody>
                <h6 className="stats-card-title">{card.title}</h6>
                <h2 className="stats-card-value">{card.total?.toLocaleString('es-CO') || 0}</h2>
              </CardBody>
            </Card>
          </Col>
        ))}
      </Row>
      <Row>
        <Col md={12} className="mb-4">
          <Card className="stats-card" style={{ borderTop: '4px solid #5d4bb7' }}>
            <CardBody>
              <h6 className="stats-card-title">
                <Translate contentKey="divipol.stats.potencialElectoral">Potencial Electoral</Translate>
              </h6>
              <Row>
                <Col md={4}>
                  <div className="text-center">
                    <small className="text-muted d-block mb-2">
                      <Translate contentKey="divipol.stats.total">Total</Translate>
                    </small>
                    <h3 className="stats-card-value mb-0">{stats.potencialTotal?.toLocaleString('es-CO') || 0}</h3>
                  </div>
                </Col>
                <Col md={4}>
                  <div className="text-center">
                    <small className="text-muted d-block mb-2">
                      <Translate contentKey="divipol.stats.women">Mujeres</Translate>
                    </small>
                    <h3 className="stats-card-value mb-0">{stats.potencialFemenino?.toLocaleString('es-CO') || 0}</h3>
                  </div>
                </Col>
                <Col md={4}>
                  <div className="text-center">
                    <small className="text-muted d-block mb-2">
                      <Translate contentKey="divipol.stats.men">Hombres</Translate>
                    </small>
                    <h3 className="stats-card-value mb-0">{stats.potencialMasculino?.toLocaleString('es-CO') || 0}</h3>
                  </div>
                </Col>
              </Row>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </div>
  );
};
