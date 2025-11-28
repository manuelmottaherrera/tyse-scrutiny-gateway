import React from 'react';
import { MemoryRouter } from 'react-router';
import { render } from '@testing-library/react';
import { TranslatorContext } from 'react-jhipster';

import { ModuleCard, ModuleCardCore, ModuleCardIcon, ModuleCardSubIcon } from './modules-card';
import { Module } from './dashboard-grid';
import { faMapLocationDot, faChartBar, faArrowRight, faLock } from '@fortawesome/free-solid-svg-icons';

describe('ModuleCard component', () => {
  beforeAll(() => {
    TranslatorContext.registerTranslations('en', {});
  });

  const createModule = (overrides: Partial<Module> = {}): Module => ({
    id: 'test-module',
    title: 'Test Module',
    description: 'Test description',
    icon: faMapLocationDot,
    path: '/test-path',
    requiredPermission: 'test.read',
    color: '#008cba',
    enabled: true,
    disabledReasonKey: '',
    comingSoon: false,
    hidden: false,
    ...overrides,
  });

  describe('ModuleCard', () => {
    it('should render enabled module as a clickable Link for internal paths', () => {
      const module = createModule({ path: '/divipol', enabled: true });

      const { container } = render(
        <MemoryRouter>
          <ModuleCard module={module} />
        </MemoryRouter>,
      );

      // Should render as <a> tag (Link component renders as <a>)
      const link = container.querySelector('a.module-card');
      expect(link).toBeTruthy();
      expect(link?.getAttribute('href')).toBe('/divipol');
      expect(link?.classList.contains('disabled')).toBe(false);
    });

    it('should render external links with target="_blank"', () => {
      const module = createModule({
        path: 'https://external-site.com',
        enabled: true,
      });

      const { container } = render(
        <MemoryRouter>
          <ModuleCard module={module} />
        </MemoryRouter>,
      );

      const link = container.querySelector('a.module-card');
      expect(link).toBeTruthy();
      expect(link?.getAttribute('href')).toBe('https://external-site.com');
      expect(link?.getAttribute('target')).toBe('_blank');
      expect(link?.getAttribute('rel')).toBe('noopener noreferrer');
    });

    it('should render disabled module as a div with disabled class', () => {
      const module = createModule({ enabled: false });

      const { container } = render(
        <MemoryRouter>
          <ModuleCard module={module} />
        </MemoryRouter>,
      );

      // Should render as <div> not <a>
      const card = container.querySelector('div.module-card.disabled');
      expect(card).toBeTruthy();

      // Should not be a link
      const link = container.querySelector('a.module-card');
      expect(link).toBeFalsy();
    });

    it('should display Coming Soon badge for comingSoon modules', () => {
      const module = createModule({
        enabled: false,
        comingSoon: true,
        disabledReasonKey: 'comingSoon',
      });

      const { container } = render(
        <MemoryRouter>
          <ModuleCard module={module} />
        </MemoryRouter>,
      );

      // Check for the Coming Soon badge
      const badge = container.querySelector('.badge-coming-soon');
      expect(badge).toBeTruthy();
      expect(container.innerHTML).toContain('Coming Soon');
    });

    it('should render lock icon for disabled modules', () => {
      const module = createModule({ enabled: false });

      const { container } = render(
        <MemoryRouter>
          <ModuleCard module={module} />
        </MemoryRouter>,
      );

      // Should have lock icon (fa-lock)
      const lockIcon = container.querySelector('[data-icon="lock"]');
      expect(lockIcon).toBeTruthy();
    });

    it('should render arrow-right icon for enabled modules', () => {
      const module = createModule({ enabled: true });

      const { container } = render(
        <MemoryRouter>
          <ModuleCard module={module} />
        </MemoryRouter>,
      );

      // Should have arrow-right icon
      const arrowIcon = container.querySelector('[data-icon="arrow-right"]');
      expect(arrowIcon).toBeTruthy();
    });

    it('should apply module color to icon', () => {
      const module = createModule({ color: '#ff5733' });

      const { container } = render(
        <MemoryRouter>
          <ModuleCard module={module} />
        </MemoryRouter>,
      );

      const iconContainer = container.querySelector('.card-icon');
      expect(iconContainer).toBeTruthy();
      // Browser normalizes hex to rgb format: #ff5733 -> rgb(255, 87, 51)
      expect(iconContainer?.getAttribute('style')).toContain('rgb(255, 87, 51)');
    });

    it('should render module title and description', () => {
      const module = createModule({
        title: 'My Custom Title',
        description: 'My custom description',
      });

      const { container } = render(
        <MemoryRouter>
          <ModuleCard module={module} />
        </MemoryRouter>,
      );

      expect(container.innerHTML).toContain('My Custom Title');
      expect(container.innerHTML).toContain('My custom description');
    });
  });

  describe('ModuleCardIcon', () => {
    it('should render with custom color', () => {
      const { container } = render(<ModuleCardIcon icon={faMapLocationDot} color="#123456" />);

      const iconContainer = container.querySelector('.card-icon');
      // Browser normalizes hex to rgb format: #123456 -> rgb(18, 52, 86)
      expect(iconContainer?.getAttribute('style')).toContain('rgb(18, 52, 86)');
    });

    it('should render with default color when not specified', () => {
      const { container } = render(<ModuleCardIcon icon={faMapLocationDot} />);

      const iconContainer = container.querySelector('.card-icon');
      // Browser normalizes hex to rgb format: #000000 -> rgb(0, 0, 0)
      expect(iconContainer?.getAttribute('style')).toContain('rgb(0, 0, 0)');
    });

    it('should render with reduced opacity when specified', () => {
      const { container } = render(<ModuleCardIcon icon={faMapLocationDot} opacity={0.5} />);

      const icon = container.querySelector('.svg-inline--fa');
      expect(icon?.getAttribute('opacity')).toBe('0.5');
    });
  });

  describe('ModuleCardCore', () => {
    it('should render title and description', () => {
      const { container } = render(<ModuleCardCore id="test" title="Test Title" description="Test Description" />);

      expect(container.innerHTML).toContain('Test Title');
      expect(container.innerHTML).toContain('Test Description');
    });

    it('should show Coming Soon badge when comingSoon is true', () => {
      const { container } = render(<ModuleCardCore id="test" title="Test" description="Desc" comingSoon={true} />);

      const badge = container.querySelector('.badge-coming-soon');
      expect(badge).toBeTruthy();
    });

    it('should not show Coming Soon badge when comingSoon is false', () => {
      const { container } = render(<ModuleCardCore id="test" title="Test" description="Desc" comingSoon={false} />);

      const badge = container.querySelector('.badge-coming-soon');
      expect(badge).toBeFalsy();
    });

    it('should show disabled reason when provided', () => {
      const { container } = render(<ModuleCardCore id="test" title="Test" description="Desc" disabledReasonKey="maintenance" />);

      const disabledReason = container.querySelector('.disabled-reason');
      expect(disabledReason).toBeTruthy();
    });
  });

  describe('ModuleCardSubIcon', () => {
    it('should render arrow-right icon when enabled', () => {
      const { container } = render(<ModuleCardSubIcon enabled={true} />);

      const icon = container.querySelector('[data-icon="arrow-right"]');
      expect(icon).toBeTruthy();
    });

    it('should render lock icon when disabled', () => {
      const { container } = render(<ModuleCardSubIcon enabled={false} />);

      const icon = container.querySelector('[data-icon="lock"]');
      expect(icon).toBeTruthy();
    });
  });
});
