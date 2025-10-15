import './theme-toggle.scss';
import React, { useState } from 'react';
import { Dropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useTheme, Theme } from '../../context/theme-contex/theme-context';
import { Translate } from 'react-jhipster';

const ThemeToggle: React.FC = () => {
  const { theme, setTheme, currentResolvedTheme } = useTheme();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const toggle = () => setDropdownOpen(prevState => !prevState);

  const themeOptions: { key: Theme; translationKey: string; icon: any }[] = [
    { key: 'light', translationKey: 'global.theme.light', icon: 'sun' },
    { key: 'dark', translationKey: 'global.theme.dark', icon: 'moon' },
    { key: 'system', translationKey: 'global.theme.system', icon: 'desktop' },
  ];

  const getCurrentIcon = () => {
    if (theme === 'system') {
      return currentResolvedTheme === 'dark' ? 'moon' : 'sun';
    }
    return themeOptions.find(opt => opt.key === theme)?.icon || 'desktop';
  };

  return (
    <div className="theme-toggle">
      <Dropdown isOpen={dropdownOpen} toggle={toggle} direction="down" nav inNavbar>
        <DropdownToggle
          nav
          caret
          className="theme-toggle-button d-flex align-items-center"
          title="Selector de tema"
          aria-label="Selector de tema"
        >
          <FontAwesomeIcon icon={getCurrentIcon()} />
          <span>
            <Translate contentKey="global.theme.title">Theme</Translate>
          </span>
        </DropdownToggle>
        <DropdownMenu end className="theme-dropdown-menu">
          {themeOptions.map(({ key, translationKey, icon }) => (
            <DropdownItem key={key} onClick={() => setTheme(key)} active={theme === key} className="theme-dropdown-item">
              <FontAwesomeIcon icon={icon} className="me-2" />
              <Translate contentKey={translationKey}>{key}</Translate>
              {theme === key && <span className="ms-auto check-icon">✓</span>}
            </DropdownItem>
          ))}
        </DropdownMenu>
      </Dropdown>
    </div>
  );
};

export default ThemeToggle;
