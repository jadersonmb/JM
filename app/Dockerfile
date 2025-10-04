# syntax=docker/dockerfile:1.6

FROM node:20-alpine AS base
WORKDIR /app
COPY package*.json ./
RUN npm ci

FROM base AS development
COPY . .
ENV HOST=0.0.0.0 \
    PORT=5173 \
    VITE_API_BASE_URL=http://localhost:8080 \
    CHOKIDAR_USEPOLLING=true
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5173"]

FROM base AS build
COPY . .
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL \
    NODE_ENV=production
RUN npm run build

FROM nginx:1.27-alpine AS production
RUN apk add --no-cache curl
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=5 CMD curl -f http://localhost/health || exit 1
CMD ["nginx", "-g", "daemon off;"]
