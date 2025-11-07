import React from 'react';
import { Card, CardHeader, CardBody, ListGroup, ListGroupItem, Badge } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Translate, TextFormat } from 'react-jhipster';
import { IRecentActivity } from 'app/shared/model/dashboard/recent-activity.model';
import { APP_LOCAL_DATETIME_FORMAT } from 'app/config/constants';
import './recent-activity-widget.scss';

interface RecentActivityWidgetProps {
  activities: IRecentActivity[];
  loading?: boolean;
}

const getActionBadgeColor = (action: string): string => {
  switch (action) {
    case 'CREATED':
      return 'success';
    case 'UPDATED':
      return 'info';
    case 'DELETED':
      return 'danger';
    case 'ACTIVATED':
      return 'success';
    case 'DEACTIVATED':
      return 'warning';
    default:
      return 'secondary';
  }
};

const getActionIcon = (action: string) => {
  switch (action) {
    case 'CREATED':
      return 'plus-circle';
    case 'UPDATED':
      return 'edit';
    case 'DELETED':
      return 'trash';
    case 'ACTIVATED':
      return 'check-circle';
    case 'DEACTIVATED':
      return 'times-circle';
    default:
      return 'circle';
  }
};

export const RecentActivityWidget: React.FC<RecentActivityWidgetProps> = ({ activities, loading = false }) => {
  return (
    <Card className="recent-activity-widget">
      <CardHeader>
        <h5 className="mb-0">
          <FontAwesomeIcon icon="clock" className="me-2" />
          <Translate contentKey="dashboard.recentActivity.title">Actividad Reciente</Translate>
        </h5>
      </CardHeader>
      <CardBody className="p-0">
        {loading ? (
          <div className="text-center p-4">
            <div className="spinner-border spinner-border-sm text-primary" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
          </div>
        ) : activities && activities.length > 0 ? (
          <ListGroup flush>
            {activities.map((activity, idx) => (
              <ListGroupItem key={idx} className="activity-item">
                <div className="d-flex align-items-start">
                  <div className="activity-icon me-3">
                    <FontAwesomeIcon icon={getActionIcon(activity.action)} className={`text-${getActionBadgeColor(activity.action)}`} />
                  </div>
                  <div className="activity-details flex-grow-1">
                    <div className="d-flex justify-content-between align-items-start mb-1">
                      <div>
                        <strong>{activity.changedBy}</strong>{' '}
                        <Badge color={getActionBadgeColor(activity.action)} pill>
                          <Translate contentKey={`dashboard.auditAction.${activity.action}`}>{activity.action}</Translate>
                        </Badge>
                      </div>
                      <small className="text-muted">
                        <TextFormat type="date" value={activity.changedDate} format={APP_LOCAL_DATETIME_FORMAT} />
                      </small>
                    </div>
                    <div className="activity-description">
                      <span className="text-muted">{activity.authorityName}</span>
                    </div>
                  </div>
                </div>
              </ListGroupItem>
            ))}
          </ListGroup>
        ) : (
          <div className="text-center p-4 text-muted">
            <FontAwesomeIcon icon="info-circle" className="me-2" />
            <Translate contentKey="dashboard.recentActivity.empty">No hay actividad reciente</Translate>
          </div>
        )}
      </CardBody>
    </Card>
  );
};

export default RecentActivityWidget;
