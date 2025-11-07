import React from 'react';
import { Card, CardBody } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IconProp } from '@fortawesome/fontawesome-svg-core';
import './metric-card.scss';

interface MetricCardProps {
  title: string | React.ReactNode;
  value: number;
  icon: IconProp;
  color?: 'primary' | 'success' | 'info' | 'warning' | 'danger';
  loading?: boolean;
}

export const MetricCard: React.FC<MetricCardProps> = ({ title, value, icon, color = 'primary', loading = false }) => {
  return (
    <Card className={`metric-card border-${color}`}>
      <CardBody>
        <div className="metric-card-content">
          <div className="metric-icon">
            <FontAwesomeIcon icon={icon} size="2x" className={`text-${color}`} />
          </div>
          <div className="metric-details">
            <h3 className="metric-title">{title}</h3>
            {loading ? (
              <div className="spinner-border spinner-border-sm text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
            ) : (
              <div className="metric-value">{value?.toLocaleString() || 0}</div>
            )}
          </div>
        </div>
      </CardBody>
    </Card>
  );
};

export default MetricCard;
