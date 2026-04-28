import fp from 'fastify-plugin';
import mqtt from 'mqtt';

export default fp(async (fastify) => {
  const client = mqtt.connect(process.env.MQTT_URL || 'mqtt://mosquitto:1883');

  client.on('connect', () => {
    fastify.log.info('Notification Service connected to MQTT');
    // Listen for high-priority alerts to trigger push
    client.subscribe('jukwa/alerts/#');
    client.subscribe('jukwa/emergency/#');
  });

  client.on('message', async (topic, message) => {
    const payload = JSON.parse(message.toString());
    fastify.log.info(`MQTT alert on ${topic}, triggering push...`);
    
    // In a real scenario, we would map the topic to device tokens
    // For now, we mock a send to a generic 'topic' token
    await fastify.fcm.sendPush(
      'broadcast_topic', 
      'Jukwa Alert', 
      payload.description || 'Important update in your area',
      payload
    );
  });
});
