import { FastifyInstance } from 'fastify';

export default async function (fastify: FastifyInstance) {
  // Create incident
  fastify.post('/incidents', async (request, reply) => {
    const { 
      reporter_id, 
      category, 
      severity, 
      latitude, 
      longitude, 
      description, 
      media_urls, 
      anonymity_mode 
    } = request.body as any;

    const query = `
      INSERT INTO incidents (
        reporter_id, 
        incident_category, 
        severity_score, 
        location, 
        description, 
        media_urls, 
        anonymity_mode
      ) VALUES ($1, $2, $3, ST_SetSRID(ST_MakePoint($4, $5), 4326), $6, $7, $8)
      RETURNING *;
    `;

    const values = [
      reporter_id, 
      category, 
      severity, 
      longitude, 
      latitude, 
      description, 
      media_urls, 
      anonymity_mode
    ];

    try {
      const result = await fastify.db.query(query, values);
      return result.rows[0];
    } catch (err) {
      fastify.log.error(err);
      return reply.status(500).send({ error: 'Database error' });
    }
  });

  // Get all incidents (paginated)
  fastify.get('/incidents', async (request, reply) => {
    const { limit = 20, offset = 0 } = request.query as any;
    const result = await fastify.db.query(
      'SELECT *, ST_AsGeoJSON(location)::json as location_json FROM incidents ORDER BY reported_at DESC LIMIT $1 OFFSET $2',
      [limit, offset]
    );
    return result.rows;
  });

  // Get incident by ID
  fastify.get('/incidents/:id', async (request, reply) => {
    const { id } = request.params as any;
    const result = await fastify.db.query(
      'SELECT *, ST_AsGeoJSON(location)::json as location_json FROM incidents WHERE incident_id = $1',
      [id]
    );
    if (result.rows.length === 0) return reply.status(404).send({ error: 'Not found' });
    return result.rows[0];
  });

  // Update status
  fastify.patch('/incidents/:id/status', async (request, reply) => {
    const { id } = request.params as any;
    const { status } = request.body as any;
    const result = await fastify.db.query(
      'UPDATE incidents SET status = $1, acknowledged_at = CASE WHEN $1 = \'ACKNOWLEDGED\' THEN NOW() ELSE acknowledged_at END WHERE incident_id = $2 RETURNING *',
      [status, id]
    );
    if (result.rows.length === 0) return reply.status(404).send({ error: 'Not found' });
    return result.rows[0];
  });

  // PostGIS: Find incidents within radius (meters)
  fastify.get('/incidents/near', async (request, reply) => {
    const { lat, lon, radius = 5000 } = request.query as any;
    const query = `
      SELECT *, ST_Distance(location, ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography) as distance
      FROM incidents
      WHERE ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography, $3)
      ORDER BY distance ASC;
    `;
    const result = await fastify.db.query(query, [lon, lat, radius]);
    return result.rows;
  });

  // PostGIS: Find incidents within ward
  fastify.get('/incidents/ward/:ward_id', async (request, reply) => {
    const { ward_id } = request.params as any;
    const query = `
      SELECT i.* 
      FROM incidents i
      JOIN wards w ON ST_Within(i.location, w.boundary)
      WHERE w.ward_id = $1
      ORDER BY i.reported_at DESC;
    `;
    const result = await fastify.db.query(query, [ward_id]);
    return result.rows;
  });
}
