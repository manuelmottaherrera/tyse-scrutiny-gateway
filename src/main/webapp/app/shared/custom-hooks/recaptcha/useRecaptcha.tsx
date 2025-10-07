import { useGoogleReCaptcha } from 'react-google-recaptcha-v3';
import { useCallback, useState } from 'react';
import { recaptchaService } from './recaptcha.service';

export const useRecaptcha = (action?: string) => {
  const { executeRecaptcha } = useGoogleReCaptcha();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const verifyRecaptcha = useCallback(async (): Promise<boolean> => {
    try {
      setIsLoading(true);
      const tokenGenerated = await executeRecaptcha(action || 'default');
      if (!tokenGenerated) {
        setError('reCaptcha not yet generated, please try again later.');
        return false;
      }
      const response = await recaptchaService.verifyToken(tokenGenerated, action || 'default');
      if (!response.success) {
        setError('Invalid reCaptcha, please try again later.');
        return false;
      }
      setError(null);
      return true;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'reCAPTCHA error';
      setError(errorMessage);
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [executeRecaptcha]);

  return {
    verifyRecaptcha,
    isLoadingRecaptcha: isLoading,
    errorRecaptcha: error,
  };
};

export default useRecaptcha;
