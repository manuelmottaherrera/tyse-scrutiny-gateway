import './theme-toggle.scss';
import React from 'react';
import { useTheme, Theme } from '../../context/theme-contex/theme-context';

const ThemeToggle: React.FC = () => {
  const { theme, setTheme, currentResolvedTheme } = useTheme();

  const buttons: { key: Theme; label: string; icon: string }[] = [
    { key: 'light', label: 'Claro', icon: '☀️' },
    { key: 'dark', label: 'Oscuro', icon: '🌙' },
    { key: 'system', label: 'Sistema', icon: currentResolvedTheme === 'dark' ? '🌙' : '☀️' },
  ];

  return (
    <div className="theme-toggle">
      <div className="btn-group" role="group" aria-label="Selector de tema">
        {buttons.map(({ key, label, icon }) => (
          <button
            key={key}
            type="button"
            className={`btn btn-sm ${theme === key ? 'btn-primary' : 'btn-outline-primary'}`}
            onClick={() => setTheme(key)}
            title={`Tema ${label.toLowerCase()}`}
          >
            <span className="theme-icon">{icon}</span>
            <span className="d-none d-md-inline ms-1">{label}</span>
          </button>
        ))}
      </div>
    </div>
  );
};

export default ThemeToggle;
