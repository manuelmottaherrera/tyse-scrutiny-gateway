import React from 'react';
import { ModuleCard } from './modules-card';
import { moduleIcons } from './icons';
import './dashboard-grid.scss';

export interface Module {
  id: string;
  title: string;
  description: string;
  icon: any;
  path: string;
  role: string[];
  color?: string;
}

export const DashboardGrid: React.FC = () => {
  const modules: Module[] = [
    {
      id: 'divipol',
      title: 'Divipol',
      description: 'Gestión de división política y configuración territorial',
      icon: moduleIcons.divipol,
      path: '/divipol',
      role: ['ADMIN', 'COORDINATOR'],
      color: '#008cba',
    },
    {
      id: 'estadisticas',
      title: 'Estadísticas',
      description: 'Reportes y análisis estadísticos electorales',
      icon: moduleIcons.estadisticas,
      path: '/estadistics',
      role: ['ADMIN', 'ANALYST', 'VIEWER'],
      color: '#43ac6a',
    },
    {
      id: 'mapa-calor',
      title: 'Mapa de Calor',
      description: 'Visualización geográfica de resultados electorales',
      icon: moduleIcons.mapaCalor,
      path: '/heat-map',
      role: ['ADMIN', 'ANALYST', 'VIEWER'],
      color: '#e99002',
    },
    {
      id: 'conteo-votos',
      title: 'Conteo de Votos',
      description: 'Sistema de escrutinio y conteo en tiempo real',
      icon: moduleIcons.conteoVotos,
      path: '/vote-count',
      role: ['ADMIN', 'COORDINATOR', 'OPERATOR'],
      color: '#f04124',
    },
    {
      id: 'cargue-archivos',
      title: 'Cargue de Archivos',
      description: 'Gestión y procesamiento de archivos electorales',
      icon: moduleIcons.cargueArchivos,
      path: '/file-upload',
      role: ['ADMIN', 'OPERATOR'],
      color: '#5bc0de',
    },
    {
      id: 'jurados',
      title: 'Jurados de Votación',
      description: 'Administración del personal electoral',
      icon: moduleIcons.jurados,
      path: '/jury-management',
      role: ['ADMIN', 'COORDINATOR'],
      color: '#6f42c1',
    },
    {
      id: 'capacitaciones',
      title: 'Capacitaciones',
      description: 'Programas de formación para el personal electoral',
      icon: moduleIcons.capacitaciones,
      path: '/training',
      role: ['ADMIN', 'COORDINATOR', 'TRAINER'],
      color: '#20c997',
    },
    {
      id: 'testigos',
      title: 'Testigos Electorales',
      description: 'Gestión de observadores y testigos electorales',
      icon: moduleIcons.testigos,
      path: '/witnesses',
      role: ['ADMIN', 'COORDINATOR'],
      color: '#e83e8c',
    },
    {
      id: 'analitica',
      title: 'Analítica Electoral',
      description: 'Análisis predictivo y tendencias electorales',
      icon: moduleIcons.analitica,
      path: '/analytics',
      role: ['ADMIN', 'ANALYST'],
      color: '#6610f2',
    },
  ];

  // Filtrar módulos según el rol del usuario (debes integrar con tu sistema de auth)
  const userRoles = ['ADMIN', 'USER']; // Ejemplo - reemplazar con roles reales
  const accessibleModules = modules.filter(module => module.role.some(role => userRoles.includes(role)));

  return (
    <div className="dashboard-grid">
      <div className="dashboard-header">
        <h1 className="dashboard-title">Panel Principal</h1>
        <p className="dashboard-subtitle">Selecciona un módulo para comenzar</p>
      </div>

      <div className="modules-grid">
        {accessibleModules.map(module => (
          <ModuleCard key={module.id} module={module} />
        ))}
      </div>
    </div>
  );
};
