import Fastify from 'fastify';
import dbPlugin from './plugins/db';
import mqttPlugin from './plugins/mqtt'; // I'll copy the mqtt plugin from traffic service
import dispatchRoutes from './routes/dispatch';

const server = Fastify({ logger: true });

server.register(dbPlugin);
server.register(mqttPlugin);
server.register(dispatchRoutes);

server.get('/health', async () => {
  return { status: 'UP', service: 'emergency-service' };
});

const start = async () => {
  try {
    const port = Number(process.env.PORT) || 3004;
    await server.listen({ port, host: '0.0.0.0' });
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
