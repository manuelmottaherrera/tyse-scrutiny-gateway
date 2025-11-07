import React from 'react';
import { Card, CardHeader, CardBody } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Translate } from 'react-jhipster';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { IAuthorityUsage } from 'app/shared/model/dashboard/authority-usage.model';
import './top-authorities-chart.scss';

interface TopAuthoritiesChartProps {
  data: IAuthorityUsage[];
  loading?: boolean;
}

export const TopAuthoritiesChart: React.FC<TopAuthoritiesChartProps> = ({ data, loading = false }) => {
  const chartData = data.map(item => ({
    name: item.authorityName || item.authorityCode,
    activos: item.activeCount || 0,
    expirados: item.expiredCount || 0,
    total: item.userCount || 0,
  }));

  return (
    <Card className="top-authorities-chart">
      <CardHeader>
        <h5 className="mb-0">
          <FontAwesomeIcon icon="shield-alt" className="me-2" />
          <Translate contentKey="dashboard.topAuthorities.title">Roles Más Asignados</Translate>
        </h5>
      </CardHeader>
      <CardBody>
        {loading ? (
          <div className="text-center p-4">
            <div className="spinner-border spinner-border-sm text-primary" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
          </div>
        ) : chartData && chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--bs-table-border-color)" />
              <XAxis dataKey="name" tick={{ fill: 'var(--bs-body-color)' }} />
              <YAxis tick={{ fill: 'var(--bs-body-color)' }} />
              <Tooltip
                contentStyle={{
                  backgroundColor: 'var(--bs-card-bg)',
                  border: '1px solid var(--bs-table-border-color)',
                  color: 'var(--bs-body-color)',
                }}
              />
              <Legend wrapperStyle={{ color: 'var(--bs-body-color)' }} />
              <Bar dataKey="activos" fill="#28a745" name="Activos" />
              <Bar dataKey="expirados" fill="#6c757d" name="Expirados" />
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <div className="text-center p-4 text-muted">
            <FontAwesomeIcon icon="info-circle" className="me-2" />
            <Translate contentKey="dashboard.topAuthorities.empty">No hay datos disponibles</Translate>
          </div>
        )}
      </CardBody>
    </Card>
  );
};

export default TopAuthoritiesChart;
