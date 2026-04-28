import { FastifyInstance } from 'fastify';

export default async function (fastify: FastifyInstance) {
  // Create commitment (BARAZA)
  fastify.post('/commitments', async (request, reply) => {
    const { 
      origin_type,
      sector,
      promise_summary,
      affected_ward_id,
      responsible_agency_id,
      sla_deadline
    } = request.body as any;

    const query = `
      INSERT INTO commitments (
        origin_type, sector, promise_summary, affected_ward_id, responsible_agency_id, sla_deadline
      ) VALUES ($1, $2, $3, $4, $5, $6)
      RETURNING *;
    `;

    try {
      const result = await fastify.db.query(query, [
        origin_type, sector, promise_summary, affected_ward_id, responsible_agency_id, sla_deadline
      ]);
      return result.rows[0];
    } catch (err) {
      fastify.log.error(err);
      return reply.status(500).send({ error: 'Database error' });
    }
  });

  // Get commitments
  fastify.get('/commitments', async (request, reply) => {
    const { ward_id, agency_id } = request.query as any;
    let query = 'SELECT * FROM commitments WHERE 1=1';
    const params = [];

    if (ward_id) {
      params.push(ward_id);
      query += ` AND affected_ward_id = $${params.length}`;
    }
    if (agency_id) {
      params.push(agency_id);
      query += ` AND responsible_agency_id = $${params.length}`;
    }

    query += ' ORDER BY created_at DESC';
    
    const result = await fastify.db.query(query, params);
    return result.rows;
  });
}
