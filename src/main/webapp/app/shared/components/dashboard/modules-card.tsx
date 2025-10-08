import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Link } from 'react-router-dom';
import { Module } from './dashboard-grid';

interface ModuleCardProps {
  module: Module;
}

export const ModuleCard: React.FC<ModuleCardProps> = ({ module }) => {
  return (
    <Link to={module.path} className="module-card">
      <div className="card-content">
        <div className="card-icon" style={{ backgroundColor: module.color + '20', color: module.color }}>
          <FontAwesomeIcon icon={module.icon} size="2x" />
        </div>

        <div className="card-text">
          <h3 className="card-title">{module.title}</h3>
          <p className="card-description">{module.description}</p>
        </div>

        <div className="card-arrow">
          <FontAwesomeIcon icon="arrow-right" />
        </div>
      </div>
    </Link>
  );
};
