FROM node:25-alpine AS builder
WORKDIR /app
COPY package.json tsconfig.json ./
RUN npm install --package-lock-only && npm ci
COPY src ./src
RUN npm run build

FROM node:25-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production
ENV PORT=3001
COPY package.json ./
RUN npm install --package-lock-only && npm ci --omit=dev && npm cache clean --force
COPY --from=builder /app/dist ./dist
RUN apk add --no-cache wget && \
    chown -R node:node /app
USER node
EXPOSE 3001
HEALTHCHECK --interval=15s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:3001/health || exit 1
CMD ["node", "dist/index.js"]
