import fp from 'fastify-plugin';
import mqtt from 'mqtt';
import dotenv from 'dotenv';

dotenv.config();

export default fp(async (fastify) => {
  const brokerUrl = process.env.MQTT_URL || 'mqtt://localhost:1883';
  const client = mqtt.connect(brokerUrl);

  client.on('connect', () => {
    fastify.log.info(`Connected to MQTT broker at ${brokerUrl}`);
  });

  client.on('error', (err) => {
    fastify.log.error({ err }, 'MQTT connection error');
  });

  fastify.decorate('mqtt', client);

  fastify.addHook('onClose', async (instance) => {
    instance.mqtt.end();
  });
});

declare module 'fastify' {
  export interface FastifyInstance {
    mqtt: mqtt.MqttClient;
  }
}
