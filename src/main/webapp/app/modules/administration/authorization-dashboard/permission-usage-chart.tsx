import React from 'react';
import { Card, CardHeader, CardBody } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Translate } from 'react-jhipster';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { IPermissionUsage } from 'app/shared/model/dashboard/permission-usage.model';
import './permission-usage-chart.scss';

interface PermissionUsageChartProps {
  data: IPermissionUsage[];
  loading?: boolean;
}

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d', '#ffc658', '#ff7c7c', '#8dd1e1', '#d0ed57'];

const renderLabel = (entry: any) => {
  const name = entry.name || '';
  const value = Number(entry.value) || 0;
  return `${name}: ${value}`;
};

export const PermissionUsageChart: React.FC<PermissionUsageChartProps> = ({ data, loading = false }) => {
  const chartData = data.map(item => ({
    name: item.permissionName || `${item.resource}.${item.action}`,
    value: item.usageCount || 0,
  }));

  return (
    <Card className="permission-usage-chart">
      <CardHeader>
        <h5 className="mb-0">
          <FontAwesomeIcon icon="key" className="me-2" />
          <Translate contentKey="dashboard.permissionUsage.title">Uso de Permisos</Translate>
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
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderLabel}
                outerRadius={80}
                fill={COLORS[4]}
                dataKey="value"
              >
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: 'var(--bs-card-bg)',
                  border: '1px solid var(--bs-table-border-color)',
                  color: 'var(--bs-body-color)',
                }}
              />
              <Legend wrapperStyle={{ color: 'var(--bs-body-color)' }} />
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <div className="text-center p-4 text-muted">
            <FontAwesomeIcon icon="info-circle" className="me-2" />
            <Translate contentKey="dashboard.permissionUsage.empty">No hay datos disponibles</Translate>
          </div>
        )}
      </CardBody>
    </Card>
  );
};

export default PermissionUsageChart;
