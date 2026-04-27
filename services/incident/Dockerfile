FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json tsconfig.json ./
COPY src ./src
RUN npm ci && npm run build

FROM node:20-alpine
WORKDIR /app
COPY --from=builder /app/dist ./dist
COPY package.json ./
RUN npm ci --production
EXPOSE 3000
CMD ["node", "dist/index.js"]
