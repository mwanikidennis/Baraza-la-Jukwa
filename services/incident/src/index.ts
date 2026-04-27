import Fastify from 'fastify';
import { fastifySwagger } from '@fastify/swagger';

const server = Fastify({ logger: true });

// Register Swagger plugin
server.register(fastifySwagger, {
  routePrefix: '/documentation',
  exposeRoute: true,
  swagger: {
    info: {
      title: 'Incident Service API',
      description: 'Fastify API for incident handling',
      version: '1.0.0',
    },
    host: 'localhost',
    schemes: ['http'],
  },
});

// Health check
server.get('/health', async (request, reply) => {
  return { status: 'ok' };
});

const start = async () => {
  try {
    await server.listen({ port: 3000, host: '0.0.0.0' });
    server.log.info(`Server listening on ${server.server.address()?.toString()}`);
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
