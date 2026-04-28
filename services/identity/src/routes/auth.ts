import { FastifyInstance } from 'fastify';
import crypto from 'crypto';

export default async function (fastify: FastifyInstance) {
  // Register or Get Pseudonym
  fastify.post('/register', async (request, reply) => {
    const { device_id, ward_id } = request.body as any;

    // Create a deterministic hash of the device ID (never store raw device ID)
    const device_token_hash = crypto
      .createHash('sha256')
      .update(device_id + (process.env.SALT || 'jukwaa_salt'))
      .digest('hex');

    const checkQuery = 'SELECT * FROM citizens WHERE device_token_hash = $1';
    const existing = await fastify.db.query(checkQuery, [device_token_hash]);

    if (existing.rows.length > 0) {
      return existing.rows[0];
    }

    const insertQuery = `
      INSERT INTO citizens (device_token_hash, ward_id)
      VALUES ($1, $2)
      RETURNING *;
    `;

    try {
      const result = await fastify.db.query(insertQuery, [device_token_hash, ward_id]);
      return result.rows[0];
    } catch (err) {
      fastify.log.error(err);
      return reply.status(500).send({ error: 'Registration failed' });
    }
  });

  // Get profile
  fastify.get('/me/:citizen_id', async (request, reply) => {
    const { citizen_id } = request.params as any;
    const result = await fastify.db.query('SELECT * FROM citizens WHERE citizen_id = $1', [citizen_id]);
    if (result.rows.length === 0) return reply.status(404).send({ error: 'Citizen not found' });
    return result.rows[0];
  });

  // Update anonymity preference
  fastify.patch('/me/:citizen_id/preferences', async (request, reply) => {
    const { citizen_id } = request.params as any;
    const { anonymity_preference } = request.body as any;
    const result = await fastify.db.query(
      'UPDATE citizens SET anonymity_preference = $1 WHERE citizen_id = $2 RETURNING *',
      [anonymity_preference, citizen_id]
    );
    return result.rows[0];
  });
}
