import Fastify from 'fastify';
import { fastifySwagger } from '@fastify/swagger';
import dbPlugin from './plugins/db';
import mqttPlugin from './plugins/mqtt';
import incidentRoutes from './routes/incidents';

const server = Fastify({
  logger: {
    transport: {
      target: 'pino-pretty'
    }
  }
});

server.register(dbPlugin);
server.register(mqttPlugin);

server.register(fastifySwagger, {
  swagger: {
    info: {
      title: 'Jukwa Incident Service API',
      description: 'Core incident management and spatial querying',
      version: '1.0.0',
    },
    host: 'localhost:3001',
    schemes: ['http'],
    consumes: ['application/json'],
    produces: ['application/json'],
  },
});

server.register(incidentRoutes);

server.get('/health', async () => {
  return { status: 'UP', service: 'incident-service' };
});

const start = async () => {
  try {
    const port = Number(process.env.PORT) || 3001;
    await server.listen({ port, host: '0.0.0.0' });
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
