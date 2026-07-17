# Etapa 1 — build da aplicação Angular
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install

COPY . .
RUN npm run build:prod

# Etapa 2 — imagem final, apenas os arquivos estáticos servidos via Nginx
FROM nginx:1.27-alpine

COPY --from=build /app/dist/bytebank-digital-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
