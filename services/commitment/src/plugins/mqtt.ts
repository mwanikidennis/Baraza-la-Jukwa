import fp from 'fastify-plugin';
import mqtt, { MqttClient } from 'mqtt';
import dotenv from 'dotenv';

dotenv.config();

export default fp(async (fastify) => {
  const url = process.env.MQTT_URL || 'mqtt://localhost:1883';
  const client = mqtt.connect(url);

  client.on('connect', () => {
    fastify.log.info('Commitment Service connected to MQTT broker');
  });

  client.on('error', (err) => {
    fastify.log.error({ err }, 'MQTT connection error');
  });

  fastify.decorate('mqtt', client);

  fastify.addHook('onClose', async () => {
    client.end();
  });
});

declare module 'fastify' {
  export interface FastifyInstance {
    mqtt: MqttClient;
  }
}
