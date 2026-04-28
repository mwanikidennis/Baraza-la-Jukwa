import Fastify from 'fastify';
import mongoPlugin from './plugins/mongodb';
import mqttPlugin from './plugins/mqtt';

const server = Fastify({ logger: true });

server.register(mongoPlugin);
server.register(mqttPlugin);

server.get('/health', async () => {
  return { status: 'UP', service: 'traffic-service' };
});

// Route to query telemetry
server.get('/telemetry/:sensor_id', async (request, reply) => {
  const { sensor_id } = request.params as any;
  const docs = await server.mongo
    .collection('traffic_telemetry')
    .find({ sensor_id })
    .sort({ timestamp: -1 })
    .limit(10)
    .toArray();
  return docs;
});

const start = async () => {
  try {
    const port = Number(process.env.PORT) || 3003;
    await server.listen({ port, host: '0.0.0.0' });
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
