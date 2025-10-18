import React from 'react';
import { ModuleCard } from './modules-card';
import { moduleIcons } from './icons';
import './dashboard-grid.scss';
import { Translate } from 'react-jhipster';

export interface Module {
  id: string;
  title: string;
  description: string;
  icon: any;
  path: string;
  role: string[];
  color?: string;
  enabled: boolean;
  disabledReasonKey?: string; // Clave i18n para la razón de deshabilitación
  comingSoon?: boolean;
  hidden: boolean;
}

export const DashboardGrid: React.FC = () => {
  const modules: Module[] = [
    {
      id: 'divipol',
      title: 'Divipol',
      description: 'Management of political division and territorial configuration',
      icon: moduleIcons.divipol,
      path: '/divipol',
      role: ['ADMIN', 'COORDINATOR'],
      color: '#008cba',
      enabled: true,
      disabledReasonKey: 'inDevelopment',
      comingSoon: true,
      hidden: false,
    },
    {
      id: 'statistics',
      title: 'Statistics',
      description: 'Electoral statistical reports and analyses',
      icon: moduleIcons.statistics,
      path: '/estadistics',
      role: ['ADMIN', 'ANALYST', 'VIEWER'],
      color: '#43ac6a',
      enabled: false,
      disabledReasonKey: 'comingSoon',
      comingSoon: true,
      hidden: false,
    },
    {
      id: 'heatMap',
      title: 'Heat Map',
      description: 'Geographic visualization of election results',
      icon: moduleIcons.heatMap,
      path: '/heat-map',
      role: ['ADMIN', 'ANALYST', 'VIEWER'],
      color: '#e99002',
      enabled: false,
      disabledReasonKey: 'comingSoon',
      comingSoon: true,
      hidden: false,
    },
    {
      id: 'voteCount',
      title: 'Vote Count',
      description: 'Real-time counting and scrutiny system',
      icon: moduleIcons.voteCount,
      path: '/vote-count',
      role: ['ADMIN', 'COORDINATOR', 'OPERATOR'],
      color: '#f04124',
      enabled: false,
      disabledReasonKey: 'comingSoon',
      comingSoon: true,
      hidden: false,
    },
    {
      id: 'fileUpload',
      title: 'File Upload',
      description: 'Management and processing of electoral files',
      icon: moduleIcons.fileUpload,
      path: '/file-upload',
      role: ['ADMIN', 'OPERATOR'],
      color: '#5bc0de',
      enabled: false,
      disabledReasonKey: 'comingSoon',
      comingSoon: true,
      hidden: false,
    },
    {
      id: 'votingJuries',
      title: 'Voting Juries',
      description: '',
      icon: moduleIcons.votingJuries,
      path: '/voting-juries',
      role: ['ADMIN', 'COORDINATOR'],
      color: '#6f42c1',
      enabled: false,
      disabledReasonKey: 'comingSoon',
      comingSoon: true,
      hidden: false,
    },
    {
      id: 'trainings',
      title: 'Trainings',
      description: 'Training programs for electoral staff',
      icon: moduleIcons.trainings,
      path: '/training',
      role: ['ADMIN', 'COORDINATOR', 'TRAINER'],
      color: '#20c997',
      enabled: false,
      disabledReasonKey: 'comingSoon',
      comingSoon: true,
      hidden: false,
    },
    {
      id: 'witnesses',
      title: 'Testigos Electorales',
      description: '',
      icon: moduleIcons.witnesses,
      path: '/witnesses',
      role: ['ADMIN', 'COORDINATOR'],
      color: '#e83e8c',
      enabled: false,
      disabledReasonKey: 'comingSoon',
      comingSoon: true,
      hidden: false,
    },
    {
      id: 'analytics',
      title: 'Analítica Electoral',
      description: 'Predictive analysis and electoral trends',
      icon: moduleIcons.analytics,
      path: '/analytics',
      role: ['ADMIN', 'ANALYST'],
      color: '#6610f2',
      enabled: false,
      disabledReasonKey: 'comingSoon',
      comingSoon: true,
      hidden: false,
    },
  ];

  // Filtrar módulos según el rol del usuario (debes integrar con tu sistema de auth)
  const userRoles = ['ADMIN', 'USER']; // Ejemplo - reemplazar con roles reales
  const accessibleModules = modules.filter(module => module.role.some(role => userRoles.includes(role)));

  return (
    <div className="dashboard-grid">
      <div className="dashboard-header">
        <h1 className="dashboard-title">
          <Translate contentKey="dashboardGrid.title">Main Panel</Translate>
        </h1>
        <p className="dashboard-subtitle">
          <Translate contentKey="dashboardGrid.description">Select a module to begin</Translate>
        </p>
      </div>

      <div className="modules-grid">
        {accessibleModules.map(module => (
          <ModuleCard key={module.id} module={module} />
        ))}
      </div>
    </div>
  );
};
