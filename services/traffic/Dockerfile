FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json tsconfig.json ./
RUN npm install
COPY src ./src
RUN npm run build

FROM node:20-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production
ENV PORT=3003
COPY package.json ./
RUN npm install --omit=dev
COPY --from=builder /app/dist ./dist
RUN apk add --no-cache wget && \
    chown -R node:node /app
USER node
EXPOSE 3003
HEALTHCHECK --interval=15s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:3003/health || exit 1
CMD ["node", "dist/index.js"]
