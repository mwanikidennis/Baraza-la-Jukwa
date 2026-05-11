import crypto from 'crypto';
import { FastifyInstance } from 'fastify';

export const VALID_SOS_TYPES = ['MEDICAL', 'SECURITY', 'FIRE', 'ACCIDENT'] as const;

export const EMERGENCY_MQTT_QOS = 2; // EXACTLY_ONCE

export const DISPATCH_TOPIC_PREFIX = 'jukwa/emergency';

const SosBodySchema = {
  type: 'object',
  required: ['latitude', 'longitude', 'type', 'county'],
  properties: {
    citizen_id: {
      type: 'string',
      format: 'uuid',
    },
    latitude: {
      type: 'number',
      minimum: -90,
      maximum: 90,
    },
    longitude: {
      type: 'number',
      minimum: -180,
      maximum: 180,
    },
    type: {
      type: 'string',
      enum: ['MEDICAL', 'SECURITY', 'FIRE', 'ACCIDENT'],
    },
    county: {
      type: 'string',
    },
    media_stream_url: {
      type: 'string',
    },
  },
};

export default async function (fastify: FastifyInstance) {

  // High-Priority SOS Trigger
  // Source: Architecture §5.4
  // Priority: IMMEDIATE (bypasses rate limiting at gateway)
  fastify.post('/sos', { schema: { body: SosBodySchema } }, async (request, reply) => {
    const {
      citizen_id,
      latitude,
      longitude,
      type,
      county,
      media_stream_url,
    } = request.body as {
      citizen_id?: string;
      latitude: number;
      longitude: number;
      type: string;
      county: string;
      media_stream_url?: string;
    };

    const incident_id = crypto.randomUUID();

    // 1. Log to DB immediately
    const dbQuery = `
      INSERT INTO incidents (
        incident_id, reporter_id, incident_category, severity_score, location, status
      ) VALUES ($1, $2, $3, 5, ST_SetSRID(ST_MakePoint($4, $5), 4326), 'EMERGENCY')
      RETURNING *;
    `;

    try {
      await fastify.db.query(dbQuery, [incident_id, citizen_id ?? null, type, longitude, latitude]);

      // 2. Dispatch to MQTT (QoS 2 - EXACTLY_ONCE for Emergency)
      const dispatchTopic = `${DISPATCH_TOPIC_PREFIX}/${county}/dispatch`;
      const payload = JSON.stringify({
        incident_id,
        location: { lat: latitude, lon: longitude },
        type,
        county,
        media_stream_url,
        timestamp: new Date().toISOString(),
      });

      fastify.mqtt.publish(dispatchTopic, payload, { qos: EMERGENCY_MQTT_QOS });

      // 3. Start SLA Countdown (120 seconds)
      // In a real system, this would trigger a background task/cron/worker
      fastify.log.info({ incident_id }, 'Emergency dispatch triggered. SLA: 120s');

      return {
        status: 'DISPATCHED',
        incident_id,
        tracking_url: `/api/v1/incidents/${incident_id}/status`,
      };
    } catch (err) {
      fastify.log.error({ err }, 'Critical dispatch failure');
      return reply.status(500).send({ error: 'Critical dispatch failure' });
    }
  });
}
