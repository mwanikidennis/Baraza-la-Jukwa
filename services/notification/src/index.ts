import Fastify from 'fastify';
import fcmPlugin from './plugins/fcm';
import mqttPlugin from './plugins/mqtt';

const server = Fastify({ logger: true });

server.register(fcmPlugin);
server.register(mqttPlugin);

server.get('/health', async () => {
  return { status: 'UP', service: 'notification-service' };
});

const start = async () => {
  try {
    const port = Number(process.env.PORT) || 3007;
    await server.listen({ port, host: '0.0.0.0' });
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
