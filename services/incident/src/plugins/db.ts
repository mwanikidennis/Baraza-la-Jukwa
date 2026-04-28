import fp from 'fastify-plugin';
import { Pool } from 'pg';
import dotenv from 'dotenv';

dotenv.config();

export default fp(async (fastify) => {
  const pool = new Pool({
    connectionString: process.env.DATABASE_URL || 'postgresql://jukwaa:jukwaa_secret@localhost:5432/jukwaa',
  });

  // Test connection
  try {
    const client = await pool.connect();
    fastify.log.info('Connected to PostgreSQL/PostGIS');
    client.release();
  } catch (err) {
    fastify.log.error('Failed to connect to database', err);
  }

  fastify.decorate('db', pool);

  fastify.addHook('onClose', async (instance) => {
    await instance.db.end();
  });
});

declare module 'fastify' {
  export interface FastifyInstance {
    db: Pool;
  }
}
