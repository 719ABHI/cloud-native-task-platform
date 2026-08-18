# ==========================
# Stage 1 - Build React App
# ==========================

FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

# Build-time variable
ARG VITE_API_URL

# Convert ARG into ENV so Vite can read it
ENV VITE_API_URL=$VITE_API_URL

# Build React application
RUN npm run build

# ==========================
# Stage 2 - Runtime
# ==========================

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

# Start the Nginx web server.
# "daemon off;" keeps Nginx running in the foreground.
# Docker requires the main process to stay in the foreground,
# otherwise the container exits immediately.
CMD ["nginx","-g","daemon off;"]