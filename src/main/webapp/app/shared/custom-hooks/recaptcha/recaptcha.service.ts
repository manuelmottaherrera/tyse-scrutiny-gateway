import axios from 'axios';

export const recaptchaService = {
  // Verificar el token con el backend
  verifyToken(token: string, action: string): Promise<{ success: boolean }> {
    return axios.post('/api/recaptcha/verify', { token, action }).then(response => response.data);
  },
};
