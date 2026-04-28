import fp from 'fastify-plugin';
import mqtt, { MqttClient } from 'mqtt';
import dotenv from 'dotenv';

dotenv.config();

export default fp(async (fastify) => {
  const url = process.env.MQTT_URL || 'mqtt://localhost:1883';
  const client = mqtt.connect(url);

  client.on('connect', () => {
    fastify.log.info('Connected to Mosquitto MQTT Broker');
    // Subscribe to traffic sensors
    client.subscribe('jukwa/traffic/sensors/#', (err) => {
      if (err) fastify.log.error('Failed to subscribe to traffic sensors', err);
    });
  });

  client.on('message', async (topic, message) => {
    if (topic.startsWith('jukwa/traffic/sensors/')) {
      const payload = JSON.parse(message.toString());
      // Logic to store in MongoDB
      try {
        await fastify.mongo.collection('traffic_telemetry').insertOne({
          sensor_id: topic.split('/').pop(),
          timestamp: new Date(),
          location: payload.location, // GeoJSON Point
          metrics: payload.metrics,
          raw: payload
        });
      } catch (err) {
        fastify.log.error('Failed to store telemetry', err);
      }
    }
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
