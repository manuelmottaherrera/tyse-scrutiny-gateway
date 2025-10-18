import React from 'react';
import { render, screen, act, waitFor } from '@testing-library/react';
import { ThemeProvider, useTheme } from './theme-context';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: (key: string) => store[key] || null,
    setItem(key: string, value: string) {
      store[key] = value.toString();
    },
    removeItem(key: string) {
      delete store[key];
    },
    clear() {
      store = {};
    },
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

const TestComponent = () => {
  const { theme, setTheme, currentResolvedTheme, isInitialized } = useTheme();

  return (
    <div>
      <div data-testid="theme">{theme}</div>
      <div data-testid="resolved-theme">{currentResolvedTheme}</div>
      <div data-testid="is-initialized">{isInitialized ? 'true' : 'false'}</div>
      <button onClick={() => setTheme('light')}>Set Light</button>
      <button onClick={() => setTheme('dark')}>Set Dark</button>
      <button onClick={() => setTheme('system')}>Set System</button>
    </div>
  );
};

describe('ThemeContext', () => {
  beforeEach(() => {
    localStorageMock.clear();
    document.documentElement.removeAttribute('data-theme');
    document.documentElement.classList.remove('theme-light', 'theme-dark');
  });

  it('should throw error when useTheme is used outside ThemeProvider', () => {
    const consoleError = jest.spyOn(console, 'error').mockImplementation(() => {});
    expect(() => render(<TestComponent />)).toThrow('useTheme must be used within a ThemeProvider');
    consoleError.mockRestore();
  });

  it('should initialize with system theme by default', () => {
    act(() => {
      render(
        <ThemeProvider>
          <TestComponent />
        </ThemeProvider>,
      );
    });

    waitFor(() => {
      expect(screen.getByTestId('theme')).toHaveTextContent('system');
      expect(screen.getByTestId('is-initialized')).toHaveTextContent('true');
    });
  });

  it('should change theme to light', () => {
    act(() => {
      render(
        <ThemeProvider>
          <TestComponent />
        </ThemeProvider>,
      );
    });

    const lightButton = screen.getByText('Set Light');
    act(() => {
      lightButton.click();
    });

    waitFor(() => {
      expect(screen.getByTestId('theme')).toHaveTextContent('light');
      expect(screen.getByTestId('resolved-theme')).toHaveTextContent('light');
    });
  });

  it('should change theme to dark', () => {
    act(() => {
      render(
        <ThemeProvider>
          <TestComponent />
        </ThemeProvider>,
      );
    });

    const darkButton = screen.getByText('Set Dark');
    act(() => {
      darkButton.click();
    });

    waitFor(() => {
      expect(screen.getByTestId('theme')).toHaveTextContent('dark');
      expect(screen.getByTestId('resolved-theme')).toHaveTextContent('dark');
    });
  });

  it('should save theme preference to localStorage', () => {
    act(() => {
      render(
        <ThemeProvider>
          <TestComponent />
        </ThemeProvider>,
      );
    });

    const lightButton = screen.getByText('Set Light');
    act(() => {
      lightButton.click();
    });

    waitFor(() => {
      expect(localStorageMock.getItem('theme-preference')).toBe('light');
    });
  });

  it('should load theme preference from localStorage', () => {
    localStorageMock.setItem('theme-preference', 'dark');

    act(() => {
      render(
        <ThemeProvider>
          <TestComponent />
        </ThemeProvider>,
      );
    });

    waitFor(() => {
      expect(screen.getByTestId('theme')).toHaveTextContent('dark');
    });
  });

  it('should apply correct CSS classes when theme changes', () => {
    act(() => {
      render(
        <ThemeProvider>
          <TestComponent />
        </ThemeProvider>,
      );
    });

    const darkButton = screen.getByText('Set Dark');
    act(() => {
      darkButton.click();
    });

    waitFor(() => {
      expect(document.documentElement.classList.contains('theme-dark')).toBe(true);
      expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    });
  });
});
