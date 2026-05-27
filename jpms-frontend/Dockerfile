# =============================================================
# Stage 1 — BUILD
# Node 20 Alpine: install deps and compile Angular production build
# =============================================================
FROM node:20-alpine AS builder

WORKDIR /app

# Copy lock files first — Docker layer cache means npm ci only
# re-runs when package-lock.json changes, not on every code change
COPY package.json package-lock.json ./

RUN npm ci --legacy-peer-deps

# Copy source
COPY . .

# Build Angular in production mode
# Output: dist/jpms-frontend/browser/
RUN npm run build -- --configuration production

# =============================================================
# Stage 2 — SERVE
# nginx Alpine: serve compiled static files (~25MB final image)
# =============================================================
FROM nginx:alpine

# Remove default nginx page
RUN rm -rf /usr/share/nginx/html/*

# Copy compiled Angular output from builder stage
# Angular 17+ with @angular/build outputs to dist/<project>/browser/
COPY --from=builder /app/dist/jpms-frontend/browser /usr/share/nginx/html

# Copy custom nginx config (SPA routing + API proxy)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
