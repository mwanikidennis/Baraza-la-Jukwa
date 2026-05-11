import { FastifyInstance } from 'fastify';
import crypto from 'crypto';

export const VALID_ANONYMITY_MODES = ['STANDARD', 'INCOGNITO', 'VERIFIED'] as const;

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export default async function (fastify: FastifyInstance) {
  // Register or Get Pseudonym
  fastify.post('/register', {
    schema: {
      body: {
        type: 'object',
        required: ['device_id'],
        properties: {
          device_id: { type: 'string', minLength: 1 },
          ward_id: { type: 'integer' },
        },
      },
    },
  }, async (request, reply) => {
    const { device_id, ward_id } = request.body as { device_id: string; ward_id?: number };

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
      fastify.log.error({ err }, 'Registration failed');
      return reply.status(500).send({ error: 'Registration failed' });
    }
  });

  // Get profile
  fastify.get('/me/:citizen_id', {
    schema: {
      params: {
        type: 'object',
        required: ['citizen_id'],
        properties: {
          citizen_id: { type: 'string', pattern: UUID_REGEX.source },
        },
      },
    },
  }, async (request, reply) => {
    const { citizen_id } = request.params as { citizen_id: string };
    const result = await fastify.db.query('SELECT * FROM citizens WHERE citizen_id = $1', [citizen_id]);
    if (result.rows.length === 0) return reply.status(404).send({ error: 'Citizen not found' });
    return result.rows[0];
  });

  // Update anonymity preference
  fastify.patch('/me/:citizen_id/preferences', {
    schema: {
      params: {
        type: 'object',
        required: ['citizen_id'],
        properties: {
          citizen_id: { type: 'string', pattern: UUID_REGEX.source },
        },
      },
      body: {
        type: 'object',
        required: ['anonymity_preference'],
        properties: {
          anonymity_preference: { type: 'string', enum: [...VALID_ANONYMITY_MODES] },
        },
      },
    },
  }, async (request, reply) => {
    const { citizen_id } = request.params as { citizen_id: string };
    const { anonymity_preference } = request.body as { anonymity_preference: string };
    const result = await fastify.db.query(
      'UPDATE citizens SET anonymity_preference = $1 WHERE citizen_id = $2 RETURNING *',
      [anonymity_preference, citizen_id]
    );
    return result.rows[0];
  });
}
