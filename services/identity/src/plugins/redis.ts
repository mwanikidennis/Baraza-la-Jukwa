import fp from 'fastify-plugin';
import { createClient } from 'redis';
import dotenv from 'dotenv';

dotenv.config();

export default fp(async (fastify) => {
  const url = process.env.REDIS_URL || 'redis://localhost:6379';
  let client;
  try {
    client = createClient({ url });
    client.on('error', (err) => fastify.log.error({ err }, 'Redis client error'));
    await client.connect();
    fastify.log.info('Identity Service connected to Redis');
  } catch (err) {
    fastify.log.warn({ err }, 'Redis unavailable -- running without cache');
    client = null;
  }

  fastify.decorate('redis', client);

  fastify.addHook('onClose', async () => {
    if (client) await client.quit();
  });
});

declare module 'fastify' {
  export interface FastifyInstance {
    redis: ReturnType<typeof createClient> | null;
  }
}
