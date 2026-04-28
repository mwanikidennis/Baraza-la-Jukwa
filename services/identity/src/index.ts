import Fastify from 'fastify';
import dbPlugin from './plugins/db';
import authRoutes from './routes/auth';

const server = Fastify({ logger: true });

server.register(dbPlugin);
server.register(authRoutes);

server.get('/health', async () => {
  return { status: 'UP', service: 'identity-service' };
});

const start = async () => {
  try {
    const port = Number(process.env.PORT) || 3006;
    await server.listen({ port, host: '0.0.0.0' });
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
