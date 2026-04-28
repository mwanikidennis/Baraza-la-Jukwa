import { FastifyInstance } from 'fastify';

export default async function (fastify: FastifyInstance) {
  
  // High-Priority SOS Trigger
  // Source: Architecture §5.4
  // Priority: IMMEDIATE (bypasses rate limiting at gateway)
  fastify.post('/sos', async (request, reply) => {
    const { 
      citizen_id, 
      latitude, 
      longitude, 
      type, // 'MEDICAL', 'SECURITY', 'FIRE', 'ACCIDENT'
      media_stream_url 
    } = request.body as any;

    const incident_id = crypto.randomUUID();

    // 1. Log to DB immediately
    const dbQuery = `
      INSERT INTO incidents (
        incident_id, reporter_id, incident_category, severity_score, location, status
      ) VALUES ($1, $2, $3, 5, ST_SetSRID(ST_MakePoint($4, $5), 4326), 'EMERGENCY')
      RETURNING *;
    `;

    try {
      await fastify.db.query(dbQuery, [incident_id, citizen_id, type, longitude, latitude]);

      // 2. Dispatch to MQTT (QoS 2 - EXACTLY_ONCE for Emergency)
      const dispatchTopic = `jukwa/emergency/${type.toLowerCase()}/dispatch`;
      const payload = JSON.stringify({
        incident_id,
        location: { lat: latitude, lon: longitude },
        media_stream_url,
        timestamp: new Date()
      });

      fastify.mqtt.publish(dispatchTopic, payload, { qos: 2 });

      // 3. Start SLA Countdown (120 seconds)
      // In a real system, this would trigger a background task/cron/worker
      fastify.log.info(`Emergency dispatch triggered for incident ${incident_id}. SLA: 120s`);

      return {
        status: 'DISPATCHED',
        incident_id,
        tracking_url: `/api/v1/incidents/${incident_id}/status`
      };
    } catch (err) {
      fastify.log.error(err);
      return reply.status(500).send({ error: 'Critical dispatch failure' });
    }
  });
}

import crypto from 'crypto';
