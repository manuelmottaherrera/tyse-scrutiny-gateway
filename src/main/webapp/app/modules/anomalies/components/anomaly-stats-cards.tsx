import React from 'react';
import { Card, CardBody, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { AnomalyStats } from 'app/shared/services/anomaly.service';

interface AnomalyStatsCardsProps {
  stats: AnomalyStats | null;
  loading: boolean;
}

export const AnomalyStatsCards: React.FC<AnomalyStatsCardsProps> = ({ stats, loading }) => {
  if (loading) {
    return (
      <div className="text-center p-4">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Cargando...</span>
        </div>
      </div>
    );
  }

  if (!stats) {
    return null;
  }

  const cards = [
    {
      title: <Translate contentKey="anomalies.stats.total">Total Anomalías</Translate>,
      value: stats.total,
      color: '#5d4bb7',
    },
    {
      title: <Translate contentKey="anomalies.stats.new">Por Revisar</Translate>,
      value: stats.new,
      color: '#0d6efd',
    },
    {
      title: <Translate contentKey="anomalies.stats.highSeverity">Alta Severidad</Translate>,
      value: stats.highSeverity,
      color: '#dc3545',
    },
    {
      title: <Translate contentKey="anomalies.stats.claimed">Reclamaciones</Translate>,
      value: stats.claimed,
      color: '#6f42c1',
    },
  ];

  return (
    <div className="anomaly-stats-cards">
      <Row>
        {cards.map((card, index) => (
          <Col key={index} md={3} sm={6} className="mb-4">
            <Card className="stats-card" style={{ borderTop: `4px solid ${card.color}` }}>
              <CardBody>
                <h6 className="stats-card-title">{card.title}</h6>
                <h2 className="stats-card-value">{card.value?.toLocaleString('es-CO') || 0}</h2>
              </CardBody>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
};
