FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json tsconfig.json ./
RUN npm install --package-lock-only && npm ci
COPY src ./src
RUN npm run build

FROM node:20-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production
ENV PORT=3002
COPY package.json ./
RUN npm install --package-lock-only && npm ci --omit=dev && npm cache clean --force
COPY --from=builder /app/dist ./dist
RUN apk add --no-cache wget && \
    chown -R node:node /app
USER node
EXPOSE 3002
HEALTHCHECK --interval=15s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:3002/health || exit 1
CMD ["node", "dist/index.js"]
