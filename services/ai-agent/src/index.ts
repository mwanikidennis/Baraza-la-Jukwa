import Fastify from 'fastify';
import triageRoutes from './routes/triage';

const server = Fastify({ logger: true });

server.register(triageRoutes);

server.get('/health', async () => {
  return { status: 'UP', service: 'ai-agent-service' };
});

const start = async () => {
  try {
    const port = Number(process.env.PORT) || 3010;
    await server.listen({ port, host: '0.0.0.0' });
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
