/**
 * Branding constants for the application.
 *
 * Centralized configuration for application name, description, and team information.
 * Update these values to change the commercial branding across the entire application.
 */
export const BRANDING = {
  /**
   * Main application name
   * "Detinio" combines "DEmocracia" + "EscruTINIO"
   */
  APP_NAME: 'Detinio',

  /**
   * Full application description
   */
  APP_DESCRIPTION: 'Sistema de escrutinio electoral',

  /**
   * Team/company name
   */
  TEAM_NAME: 'Tecnología y Servicios Electorales',

  /**
   * Application title for browser tabs and meta tags
   */
  APP_TITLE: 'Detinio',
} as const;

export type Branding = typeof BRANDING;
