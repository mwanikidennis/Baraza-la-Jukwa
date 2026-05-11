import { FastifyInstance } from 'fastify';

export const VALID_CATEGORIES = [
  'robbery', 'assault', 'theft', 'suspicious_activity', 'gang_activity',
  'drug_trafficking', 'domestic_violence', 'stalking',
  'pothole', 'congestion', 'accident', 'public_transport_delay',
  'road_closure', 'unsafe_driving', 'matatu_violation',
  'broken_water_main', 'no_electricity', 'damaged_streetlight',
  'building_collapse_risk', 'waste_management',
  'drug_shortage', 'facility_closure', 'staff_shortage', 'disease_outbreak',
  'illegal_dumping', 'deforestation', 'land_grabbing',
  'air_pollution', 'noise_pollution',
  'job_scam', 'wage_theft', 'unfair_labor',
  'crop_disease', 'pest_infestation', 'livestock_disease', 'agricultural_inputs',
  'medical_emergency', 'fire',
  'other',
];

export const VALID_ANONYMITY_MODES = ['STANDARD', 'INCOGNITO', 'VERIFIED'];

export const VALID_STATUSES = [
  'SUBMITTED', 'CLASSIFIED', 'ROUTED', 'ACKNOWLEDGED',
  'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED', 'EMERGENCY',
];

const createIncidentSchema = {
  body: {
    type: 'object',
    required: ['category', 'severity', 'latitude', 'longitude', 'anonymity_mode'],
    properties: {
      reporter_id: { type: 'string', format: 'uuid' },
      category: { type: 'string', enum: VALID_CATEGORIES },
      severity: { type: 'integer', minimum: 1, maximum: 5 },
      latitude: { type: 'number', minimum: -90, maximum: 90 },
      longitude: { type: 'number', minimum: -180, maximum: 180 },
      description: { type: 'string', maxLength: 5000 },
      media_urls: { type: 'array', items: { type: 'string' }, maxItems: 10 },
      anonymity_mode: { type: 'string', enum: VALID_ANONYMITY_MODES },
    },
  },
};

const updateStatusSchema = {
  body: {
    type: 'object',
    required: ['status'],
    properties: {
      status: { type: 'string', enum: VALID_STATUSES },
    },
  },
};

const nearQuerySchema = {
  querystring: {
    type: 'object',
    required: ['lat', 'lon'],
    properties: {
      lat: { type: 'number', minimum: -90, maximum: 90 },
      lon: { type: 'number', minimum: -180, maximum: 180 },
      radius: { type: 'integer', minimum: 1, maximum: 50000, default: 5000 },
    },
  },
};

const listQuerySchema = {
  querystring: {
    type: 'object',
    properties: {
      limit: { type: 'integer', minimum: 1, maximum: 100, default: 20 },
      offset: { type: 'integer', minimum: 0, default: 0 },
    },
  },
};

export default async function (fastify: FastifyInstance) {
  fastify.post('/incidents', { schema: createIncidentSchema }, async (request, reply) => {
    const {
      reporter_id,
      category,
      severity,
      latitude,
      longitude,
      description,
      media_urls,
      anonymity_mode,
    } = request.body as {
      reporter_id?: string;
      category: string;
      severity: number;
      latitude: number;
      longitude: number;
      description?: string;
      media_urls?: string[];
      anonymity_mode: string;
    };

    const query = `
      INSERT INTO incidents (
        reporter_id, incident_category, severity_score, location,
        description, media_urls, anonymity_mode
      ) VALUES ($1, $2, $3, ST_SetSRID(ST_MakePoint($4, $5), 4326), $6, $7, $8)
      RETURNING *, ST_AsGeoJSON(location)::json as location_json;
    `;

    try {
      const result = await fastify.db.query(query, [
        reporter_id ?? null,
        category,
        severity,
        longitude,
        latitude,
        description ?? '',
        media_urls ?? [],
        anonymity_mode,
      ]);
      const incident = result.rows[0];

      // Publish to MQTT for real-time distribution
      const topic = `jukwa/incidents/${incident.incident_id}/status`;
      fastify.mqtt.publish(
        topic,
        JSON.stringify({
          incident_id: incident.incident_id,
          category: incident.incident_category,
          severity: incident.severity_score,
          status: incident.status,
          location: incident.location_json,
          reported_at: incident.reported_at,
        }),
        { qos: 1 }
      );

      return incident;
    } catch (err) {
      fastify.log.error(err);
      return reply.status(500).send({ error: 'Database error' });
    }
  });

  fastify.get('/incidents', { schema: listQuerySchema }, async (request) => {
    const { limit, offset } = request.query as { limit: number; offset: number };
    const result = await fastify.db.query(
      'SELECT *, ST_AsGeoJSON(location)::json as location_json FROM incidents ORDER BY reported_at DESC LIMIT $1 OFFSET $2',
      [limit, offset]
    );
    return result.rows;
  });

  fastify.get('/incidents/near', { schema: nearQuerySchema }, async (request) => {
    const { lat, lon, radius } = request.query as { lat: number; lon: number; radius: number };
    const query = `
      SELECT *, ST_Distance(location, ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography) as distance
      FROM incidents
      WHERE ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography, $3)
      ORDER BY distance ASC;
    `;
    const result = await fastify.db.query(query, [lon, lat, radius]);
    return result.rows;
  });

  fastify.get('/incidents/ward/:ward_id', async (request, reply) => {
    const { ward_id } = request.params as { ward_id: string };
    const wardId = parseInt(ward_id, 10);
    if (isNaN(wardId)) return reply.status(400).send({ error: 'Invalid ward_id' });

    const query = `
      SELECT i.*, ST_AsGeoJSON(i.location)::json as location_json
      FROM incidents i
      JOIN wards w ON ST_Within(i.location, w.boundary)
      WHERE w.ward_id = $1
      ORDER BY i.reported_at DESC;
    `;
    const result = await fastify.db.query(query, [wardId]);
    return result.rows;
  });

  fastify.get('/incidents/:id', async (request, reply) => {
    const { id } = request.params as { id: string };
    const result = await fastify.db.query(
      'SELECT *, ST_AsGeoJSON(location)::json as location_json FROM incidents WHERE incident_id = $1',
      [id]
    );
    if (result.rows.length === 0) return reply.status(404).send({ error: 'Not found' });
    return result.rows[0];
  });

  fastify.patch('/incidents/:id/status', { schema: updateStatusSchema }, async (request, reply) => {
    const { id } = request.params as { id: string };
    const { status } = request.body as { status: string };
    const result = await fastify.db.query(
      `UPDATE incidents SET status = $1,
        acknowledged_at = CASE WHEN $1 = 'ACKNOWLEDGED' THEN NOW() ELSE acknowledged_at END,
        resolved_at = CASE WHEN $1 = 'RESOLVED' THEN NOW() ELSE resolved_at END
       WHERE incident_id = $2 RETURNING *, ST_AsGeoJSON(location)::json as location_json`,
      [status, id]
    );
    if (result.rows.length === 0) return reply.status(404).send({ error: 'Not found' });
    return result.rows[0];
  });
}
