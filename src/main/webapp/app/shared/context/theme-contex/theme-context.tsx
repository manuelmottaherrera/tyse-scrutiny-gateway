import React, { createContext, useState, useEffect, useContext, ReactNode, useCallback } from 'react';

export type Theme = 'light' | 'dark' | 'system';

interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  currentResolvedTheme: 'light' | 'dark';
  isInitialized: boolean;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

interface ThemeProviderProps {
  children: ReactNode;
}

export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
  const [theme, setTheme] = useState<Theme>('system');
  const [currentResolvedTheme, setCurrentResolvedTheme] = useState<'light' | 'dark'>('light');
  const [isInitialized, setIsInitialized] = useState(false);

  const getInitialTheme = useCallback((): Theme => {
    if (typeof window === 'undefined') return 'system';

    try {
      const saved = localStorage.getItem('theme-preference');
      if (saved === 'light' || saved === 'dark' || saved === 'system') {
        return saved;
      }
    } catch (error) {
      console.warn('Error reading theme from localStorage:', error);
    }

    return 'system';
  }, []);

  const getSystemTheme = useCallback((): 'light' | 'dark' => {
    if (typeof window === 'undefined' || !window.matchMedia) {
      return 'light';
    }
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }, []);

  const applyTheme = useCallback(
    (themeToApply: Theme) => {
      const root = document.documentElement;
      const resolvedTheme = themeToApply === 'system' ? getSystemTheme() : themeToApply;

      setCurrentResolvedTheme(resolvedTheme);

      root.removeAttribute('data-theme');
      root.classList.remove('theme-light', 'theme-dark');

      if (themeToApply === 'system') {
        root.classList.add(`theme-${resolvedTheme}`);
      } else {
        root.setAttribute('data-theme', themeToApply);
        root.classList.add(`theme-${themeToApply}`);
      }

      const metaThemeColor = document.querySelector('meta[name="theme-color"]');
      if (metaThemeColor) {
        metaThemeColor.setAttribute('content', resolvedTheme === 'dark' ? '#212529' : '#008cba');
      }
    },
    [getSystemTheme],
  );

  useEffect(() => {
    const initialTheme = getInitialTheme();
    setTheme(initialTheme);
    applyTheme(initialTheme);
    setIsInitialized(true);
  }, [getInitialTheme, applyTheme]);

  useEffect(() => {
    if (!isInitialized) return;

    applyTheme(theme);

    try {
      localStorage.setItem('theme-preference', theme);
    } catch (error) {
      console.warn('Error saving theme to localStorage:', error);
    }
  }, [theme, isInitialized, applyTheme]);

  useEffect(() => {
    if (!isInitialized) return;

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

    const handleSystemThemeChange = (e: MediaQueryListEvent) => {
      if (theme === 'system') {
        applyTheme('system');
      }
    };

    mediaQuery.addEventListener('change', handleSystemThemeChange);

    return () => {
      mediaQuery.removeEventListener('change', handleSystemThemeChange);
    };
  }, [theme, isInitialized, applyTheme]);

  return (
    <ThemeContext.Provider
      value={{
        theme,
        setTheme,
        currentResolvedTheme,
        isInitialized,
      }}
    >
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = (): ThemeContextType => {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};
