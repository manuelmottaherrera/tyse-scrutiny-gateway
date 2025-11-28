import React from 'react';
import { ModuleCard } from './modules-card';
import { moduleIcons } from './icons';
import './dashboard-grid.scss';
import { Translate } from 'react-jhipster';
import { useAppSelector } from 'app/config/store';

export interface Module {
  id: string;
  title: string;
  description: string;
  icon: any;
  path: string;
  requiredPermission: string; // Permission name (e.g., 'divipol.read')
  color?: string;
  enabled: boolean;
  disabledReasonKey?: string; // Clave i18n para la razón de deshabilitación
  comingSoon?: boolean;
  hidden: boolean;
}

export const DashboardGrid: React.FC = () => {
  // Get user permissions from Redux store
  const userPermissions: string[] = useAppSelector(state => state.authentication.account?.permissions || []);

  const modules: Module[] = [
    {
      id: 'divipol',
      title: 'Divipol',
      description: 'Management of political division and territorial configuration',
      icon: moduleIcons.divipol,
      path: '/divipol',
      requiredPermission: 'divipol.read',
      color: '#008cba',
      enabled: true,
      disabledReasonKey: '',
      comingSoon: false,
      hidden: false,
    },
    {
      id: 'statistics',
      title: 'Statistics',
      description: 'Electoral statistical reports and analyses',
      icon: moduleIcons.statistics,
      path: 'https://estadisticaselectorales.registraduria.gov.co/stage-one',
      requiredPermission: 'statistics.read',
      color: '#43ac6a',
      enabled: true,
      disabledReasonKey: '',
      comingSoon: false,
      hidden: false,
    },
    {
      id: 'heatMap',
      title: 'Heat Map',
      description: 'Geographic visualization of election results',
      icon: moduleIcons.heatMap,
      path: '/heat-map',
      requiredPermission: 'heatmap.read',
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
      path: 'http://186.31.4.135/CuentaVotos/',
      requiredPermission: 'votecount.read',
      color: '#f04124',
      enabled: true,
      disabledReasonKey: '',
      comingSoon: false,
      hidden: false,
    },
    {
      id: 'fileUpload',
      title: 'File Upload',
      description: 'Management and processing of electoral files',
      icon: moduleIcons.fileUpload,
      path: '/file-upload',
      requiredPermission: 'fileupload.read',
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
      requiredPermission: 'votingjuries.read',
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
      path: 'http://jurna.top/',
      requiredPermission: 'trainings.read',
      color: '#20c997',
      enabled: true,
      disabledReasonKey: '',
      comingSoon: false,
      hidden: false,
    },
    {
      id: 'witnesses',
      title: 'Testigos Electorales',
      description: '',
      icon: moduleIcons.witnesses,
      path: '/witnesses',
      requiredPermission: 'witnesses.read',
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
      requiredPermission: 'analytics.read',
      color: '#6610f2',
      enabled: false,
      disabledReasonKey: 'comingSoon',
      comingSoon: true,
      hidden: false,
    },
  ];

  // Filter modules based on user permissions
  const accessibleModules = modules
    .filter(module => userPermissions.includes(module.requiredPermission))
    .sort((a, b) => {
      // Sort: enabled first, then disabled
      if (a.enabled === b.enabled) return 0;
      return a.enabled ? -1 : 1;
    });

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
