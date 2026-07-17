export const environment = {
  production: true,
  // Em produção o Nginx faz proxy_pass de /api para o backend (ver nginx.conf),
  // então usamos um caminho relativo em vez de um host fixo.
  apiUrl: '/api'
};
