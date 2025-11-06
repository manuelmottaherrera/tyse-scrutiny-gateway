import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Link } from 'react-router-dom';
import { Module } from './dashboard-grid';
import { Translate } from 'react-jhipster';

interface ModuleCardProps {
  module: Module;
}

export const ModuleCardIcon: React.FC<{ icon: any; color?: string; opacity?: number }> = ({ icon, color, opacity }) => {
  return (
    <div className="card-icon" style={{ backgroundColor: color ? color + '20' : '#00000020', color: color || '#000' }}>
      <FontAwesomeIcon icon={icon} size="2x" opacity={opacity ? opacity : 1} />
    </div>
  );
};

export const ModuleCardCore: React.FC<{
  id: string;
  title: string;
  description: string;
  comingSoon?: boolean;
  disabledReasonKey?: string;
}> = ({ id, title, description, comingSoon, disabledReasonKey }) => {
  return (
    <div className="card-text">
      <h3 className="card-title">
        <Translate contentKey={`dashboardGrid.items.${id}.title`}>{title}</Translate>
        {comingSoon && (
          <span className="badge bg-secondary ms-2">
            <Translate contentKey="dashboardGrid.general.comingSoon">Coming Soon</Translate>
          </span>
        )}
      </h3>
      <p className="card-description">
        <Translate contentKey={`dashboardGrid.items.${id}.description`}>{description}</Translate>
      </p>
      {disabledReasonKey && (
        <p className="disabled-reason">
          <Translate contentKey={`dashboardGrid.general.disabledReasons.${disabledReasonKey}`}>{disabledReasonKey}</Translate>
        </p>
      )}
    </div>
  );
};

export const ModuleCardSubIcon: React.FC<{ enabled: boolean }> = ({ enabled }) => {
  return (
    <div className="card-arrow">
      <FontAwesomeIcon icon={enabled ? 'arrow-right' : 'lock'} />
    </div>
  );
};

export const ModuleCard: React.FC<ModuleCardProps> = ({ module }) => {
  if (!module.enabled) {
    return (
      <div className="module-card disabled">
        <div className="card-content">
          <ModuleCardIcon icon={module.icon} color={module.color} opacity={0.5} />
          <ModuleCardCore
            id={module.id}
            title={module.title}
            description={module.description}
            comingSoon={module.comingSoon}
            disabledReasonKey={module.disabledReasonKey}
          />
          <ModuleCardSubIcon enabled={module.enabled} />
        </div>
      </div>
    );
  }

  // Detectar si es un enlace externo (no comienza con '/')
  const isExternalLink = !module.path.startsWith('/');

  if (isExternalLink) {
    return (
      <a href={module.path} className="module-card" hidden={module.hidden} target="_blank" rel="noopener noreferrer">
        <div className="card-content">
          <ModuleCardIcon icon={module.icon} color={module.color} />
          <ModuleCardCore id={module.id} title={module.title} description={module.description} />
          <ModuleCardSubIcon enabled={module.enabled} />
        </div>
      </a>
    );
  }

  return (
    <Link to={module.path} className="module-card" hidden={module.hidden}>
      <div className="card-content">
        <ModuleCardIcon icon={module.icon} color={module.color} />
        <ModuleCardCore id={module.id} title={module.title} description={module.description} />
        <ModuleCardSubIcon enabled={module.enabled} />
      </div>
    </Link>
  );
};
