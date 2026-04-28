import Fastify from 'fastify';
import { fastifySwagger } from '@fastify/swagger';
import dbPlugin from './plugins/db';
import commitmentRoutes from './routes/commitments';

const server = Fastify({ logger: true });

server.register(dbPlugin);

server.register(fastifySwagger, {
  swagger: {
    info: {
      title: 'Jukwa Commitment Service API (BARAZA)',
      description: 'Government commitment tracking and accountability',
      version: '1.0.0',
    },
    host: 'localhost:3002',
    schemes: ['http'],
  },
});

server.register(commitmentRoutes);

server.get('/health', async () => {
  return { status: 'UP', service: 'commitment-service' };
});

const start = async () => {
  try {
    const port = Number(process.env.PORT) || 3002;
    await server.listen({ port, host: '0.0.0.0' });
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
