import React from 'react';
import { Card, CardHeader, CardBody, ListGroup, ListGroupItem, Badge, Alert } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Translate, TextFormat } from 'react-jhipster';
import { IExpiringRole } from 'app/shared/model/dashboard/expiring-role.model';
import { APP_LOCAL_DATETIME_FORMAT } from 'app/config/constants';
import './expiring-roles-widget.scss';

interface ExpiringRolesWidgetProps {
  expiringRoles: IExpiringRole[];
  loading?: boolean;
}

const getAlertColor = (daysUntilExpiration: number): string => {
  if (daysUntilExpiration <= 1) return 'danger';
  if (daysUntilExpiration <= 3) return 'warning';
  return 'info';
};

const getAlertIcon = (daysUntilExpiration: number) => {
  if (daysUntilExpiration <= 1) return 'exclamation-triangle';
  if (daysUntilExpiration <= 3) return 'exclamation-circle';
  return 'info-circle';
};

export const ExpiringRolesWidget: React.FC<ExpiringRolesWidgetProps> = ({ expiringRoles, loading = false }) => {
  return (
    <Card className="expiring-roles-widget">
      <CardHeader>
        <h5 className="mb-0">
          <FontAwesomeIcon icon="exclamation-triangle" className="me-2" />
          <Translate contentKey="dashboard.expiringRoles.title">Roles Próximos a Expirar</Translate>
        </h5>
      </CardHeader>
      <CardBody className="p-0">
        {loading ? (
          <div className="text-center p-4">
            <div className="spinner-border spinner-border-sm text-primary" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
          </div>
        ) : expiringRoles && expiringRoles.length > 0 ? (
          <ListGroup flush>
            {expiringRoles.map((role, idx) => (
              <ListGroupItem
                key={idx}
                className={`expiring-role-item border-start border-4 border-${getAlertColor(role.daysUntilExpiration)}`}
              >
                <div className="d-flex align-items-start">
                  <div className="role-icon me-3">
                    <FontAwesomeIcon
                      icon={getAlertIcon(role.daysUntilExpiration)}
                      className={`text-${getAlertColor(role.daysUntilExpiration)}`}
                      size="lg"
                    />
                  </div>
                  <div className="role-details flex-grow-1">
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <div>
                        <h6 className="mb-0">{role.userName || role.userLogin}</h6>
                        <small className="text-muted">@{role.userLogin}</small>
                      </div>
                      <Badge color={getAlertColor(role.daysUntilExpiration)} pill>
                        {role.daysUntilExpiration === 0 ? (
                          <Translate contentKey="dashboard.expiringRoles.today">Hoy</Translate>
                        ) : role.daysUntilExpiration === 1 ? (
                          <Translate contentKey="dashboard.expiringRoles.tomorrow">Mañana</Translate>
                        ) : (
                          <Translate contentKey="dashboard.expiringRoles.days" interpolate={{ days: role.daysUntilExpiration }}>
                            {role.daysUntilExpiration} días
                          </Translate>
                        )}
                      </Badge>
                    </div>
                    <div className="role-authority mb-1">
                      <strong>{role.authorityName}</strong>
                    </div>
                    <div className="role-expiration">
                      <small className="text-muted">
                        <Translate contentKey="dashboard.expiringRoles.expiresOn">Expira el</Translate>:{' '}
                        <TextFormat type="date" value={role.expiresAt} format={APP_LOCAL_DATETIME_FORMAT} />
                      </small>
                    </div>
                  </div>
                </div>
              </ListGroupItem>
            ))}
          </ListGroup>
        ) : (
          <Alert color="success" className="m-3">
            <FontAwesomeIcon icon="check-circle" className="me-2" />
            <Translate contentKey="dashboard.expiringRoles.empty">No hay roles próximos a expirar</Translate>
          </Alert>
        )}
      </CardBody>
    </Card>
  );
};

export default ExpiringRolesWidget;
