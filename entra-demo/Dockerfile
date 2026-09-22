# ===== Etapa 1: Build =====
FROM node:22-alpine AS build
WORKDIR /app

# Copia los archivos de dependencias primero
COPY package*.json ./
RUN npm ci

# Copia el resto y construye
COPY . .
RUN npm run build

# ===== Etapa 2: Runtime con nginx =====
FROM nginx:alpine

# Copia los archivos estáticos generados
COPY --from=build /app/dist /usr/share/nginx/html

# Copia configuración personalizada de nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]