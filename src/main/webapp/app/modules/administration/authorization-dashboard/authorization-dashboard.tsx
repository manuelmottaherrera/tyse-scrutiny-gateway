import React, { useEffect, useState } from 'react';
import { Row, Col, Alert } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { getDashboardMetrics } from 'app/shared/services/dashboard.service';
import { IDashboardMetrics } from 'app/shared/model/dashboard/dashboard-metrics.model';
import MetricCard from './metric-card';
import RecentActivityWidget from './recent-activity-widget';
import ExpiringRolesWidget from './expiring-roles-widget';
import TopAuthoritiesChart from './top-authorities-chart';
import PermissionUsageChart from './permission-usage-chart';
import './authorization-dashboard.scss';

export const AuthorizationDashboard = () => {
  const [metrics, setMetrics] = useState<IDashboardMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDashboardMetrics();
  }, []);

  const loadDashboardMetrics = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getDashboardMetrics();
      setMetrics(response.data);
    } catch (err) {
      console.error('Error loading dashboard metrics:', err);
      setError('Error loading dashboard metrics');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="authorization-dashboard">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          <FontAwesomeIcon icon="chart-line" className="me-2" />
          <Translate contentKey="dashboard.title">Authorization Dashboard</Translate>
        </h2>
        <button className="btn btn-outline-primary btn-sm" onClick={loadDashboardMetrics} disabled={loading}>
          <FontAwesomeIcon icon="sync" spin={loading} className="me-1" />
          <Translate contentKey="dashboard.refresh">Refresh</Translate>
        </button>
      </div>

      {error && (
        <Alert color="danger" className="mb-4" fade={false}>
          <FontAwesomeIcon icon="exclamation-triangle" className="me-2" />
          <Translate contentKey="dashboard.error">{error}</Translate>
        </Alert>
      )}

      {/* Metric Cards */}
      <Row className="mb-4">
        <Col md={3}>
          <MetricCard
            title={<Translate contentKey="dashboard.metrics.totalAuthorities">Total Authorities</Translate>}
            value={metrics?.totalAuthorities || 0}
            icon="shield"
            color="primary"
            loading={loading}
          />
        </Col>
        <Col md={3}>
          <MetricCard
            title={<Translate contentKey="dashboard.metrics.totalPermissions">Total Permissions</Translate>}
            value={metrics?.totalPermissions || 0}
            icon="key"
            color="info"
            loading={loading}
          />
        </Col>
        <Col md={3}>
          <MetricCard
            title={<Translate contentKey="dashboard.metrics.activeUsers">Active Users</Translate>}
            value={metrics?.activeUsers || 0}
            icon="users"
            color="success"
            loading={loading}
          />
        </Col>
        <Col md={3}>
          <MetricCard
            title={<Translate contentKey="dashboard.metrics.expiredRoles">Expired Roles</Translate>}
            value={metrics?.expiredRoles || 0}
            icon="exclamation-triangle"
            color="warning"
            loading={loading}
          />
        </Col>
      </Row>

      {/* Row 1: Recent Activity and Expiring Roles */}
      <Row className="mb-4">
        <Col lg={6}>
          <RecentActivityWidget activities={metrics?.recentActivity || []} loading={loading} />
        </Col>
        <Col lg={6}>
          <ExpiringRolesWidget expiringRoles={metrics?.expiringRoles || []} loading={loading} />
        </Col>
      </Row>

      {/* Row 2: Charts */}
      <Row>
        <Col lg={6}>
          <TopAuthoritiesChart data={metrics?.topAuthorities || []} loading={loading} />
        </Col>
        <Col lg={6}>
          <PermissionUsageChart data={metrics?.permissionUsage || []} loading={loading} />
        </Col>
      </Row>
    </div>
  );
};

export default AuthorizationDashboard;
