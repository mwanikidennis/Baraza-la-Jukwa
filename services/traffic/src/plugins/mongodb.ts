import fp from 'fastify-plugin';
import { MongoClient, Db } from 'mongodb';
import dotenv from 'dotenv';

dotenv.config();

export default fp(async (fastify) => {
  const url = process.env.MONGODB_URI || 'mongodb://localhost:27017';
  const dbName = 'jukwaa_telemetry';

  const client = new MongoClient(url);

  try {
    await client.connect();
    fastify.log.info('Connected to MongoDB');
    const db = client.db(dbName);
    fastify.decorate('mongo', db);
  } catch (err) {
    fastify.log.error('Failed to connect to MongoDB', err);
  }

  fastify.addHook('onClose', async () => {
    await client.close();
  });
});

declare module 'fastify' {
  export interface FastifyInstance {
    mongo: Db;
  }
}
