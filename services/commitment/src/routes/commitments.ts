import { FastifyInstance, FastifyRequest, FastifyReply } from 'fastify';

const COMMITMENT_STATUSES = [
  'CAPTURED',
  'CLASSIFIED',
  'ACKNOWLEDGED',
  'SILENCE',
  'CLARIFICATION_REQUIRED',
  'IN_PROGRESS',
  'ESCALATED',
  'FULFILLED',
  'OVERDUE',
  'VERIFIED_RESOLVED',
  'FAILED',
] as const;

type CommitmentStatus = (typeof COMMITMENT_STATUSES)[number];

const ORIGIN_TYPES = [
  'BARAZA_SESSION',
  'CITIZEN_REPORT',
  'JIM_TICKET',
  'MEDIA_REPORT',
] as const;

type OriginType = (typeof ORIGIN_TYPES)[number];

const SECTORS = [
  'HEALTH',
  'SECURITY',
  'WATER',
  'INFRASTRUCTURE',
  'LAND',
  'EDUCATION',
  'AGRICULTURE',
  'EMPLOYMENT',
  'ENVIRONMENT',
  'TRANSPORT',
  'HOUSING',
  'FINANCE',
] as const;

type Sector = (typeof SECTORS)[number];

const COMMITMENT_TRANSITIONS: Record<CommitmentStatus, CommitmentStatus[]> = {
  CAPTURED: ['CLASSIFIED'],
  CLASSIFIED: ['ACKNOWLEDGED', 'SILENCE', 'CLARIFICATION_REQUIRED'],
  ACKNOWLEDGED: ['IN_PROGRESS', 'ESCALATED'],
  SILENCE: ['ESCALATED'],
  CLARIFICATION_REQUIRED: ['ESCALATED', 'ACKNOWLEDGED'],
  IN_PROGRESS: ['FULFILLED', 'OVERDUE', 'ESCALATED'],
  ESCALATED: ['IN_PROGRESS', 'OVERDUE', 'FAILED'],
  FULFILLED: ['VERIFIED_RESOLVED', 'IN_PROGRESS'],
  OVERDUE: ['FAILED', 'FULFILLED', 'ESCALATED'],
  VERIFIED_RESOLVED: [],
  FAILED: [],
};

const createCommitmentSchema = {
  type: 'object',
  required: [
    'origin_type',
    'sector',
    'promise_summary',
    'affected_ward_id',
    'responsible_agency_id',
  ],
  properties: {
    origin_type: { type: 'string', enum: [...ORIGIN_TYPES] },
    sector: { type: 'string', enum: [...SECTORS] },
    promise_summary: { type: 'string', minLength: 1 },
    affected_ward_id: { type: 'string', format: 'uuid' },
    responsible_agency_id: { type: 'string', format: 'uuid' },
    sla_deadline: { type: 'string', format: 'date-time' },
  },
};

const listCommitmentsSchema = {
  querystring: {
    type: 'object',
    properties: {
      ward_id: { type: 'string', format: 'uuid' },
      agency_id: { type: 'string', format: 'uuid' },
    },
  },
};

const getCommitmentByIdSchema = {
  params: {
    type: 'object',
    required: ['id'],
    properties: {
      id: { type: 'string', format: 'uuid' },
    },
  },
};

const updateCommitmentStatusSchema = {
  params: {
    type: 'object',
    required: ['id'],
    properties: {
      id: { type: 'string', format: 'uuid' },
    },
  },
  body: {
    type: 'object',
    required: ['status'],
    properties: {
      status: { type: 'string', enum: [...COMMITMENT_STATUSES] },
    },
  },
};

interface CreateCommitmentBody {
  origin_type: OriginType;
  sector: Sector;
  promise_summary: string;
  affected_ward_id: string;
  responsible_agency_id: string;
  sla_deadline?: string;
}

interface ListCommitmentsQuery {
  ward_id?: string;
  agency_id?: string;
}

interface CommitmentParams {
  id: string;
}

interface UpdateStatusBody {
  status: CommitmentStatus;
}

export default async function (fastify: FastifyInstance) {
  // Create commitment
  fastify.post<{ Body: CreateCommitmentBody }>(
    '/commitments',
    { schema: { body: createCommitmentSchema } },
    async (request, reply) => {
      const {
        origin_type,
        sector,
        promise_summary,
        affected_ward_id,
        responsible_agency_id,
        sla_deadline,
      } = request.body;

      const query = `
        INSERT INTO commitments (
          origin_type, sector, promise_summary, affected_ward_id, responsible_agency_id, sla_deadline, status
        ) VALUES ($1, $2, $3, $4, $5, $6, 'CAPTURED')
        RETURNING *;
      `;

      try {
        const result = await fastify.db.query(query, [
          origin_type,
          sector,
          promise_summary,
          affected_ward_id,
          responsible_agency_id,
          sla_deadline || null,
        ]);

        const commitment = result.rows[0];

        // Publish to ward topic
        const wardTopic = `jukwa/baraza/${affected_ward_id}/commitments`;
        fastify.mqtt.publish(
          wardTopic,
          JSON.stringify({ event: 'commitment_created', data: commitment }),
          { qos: 1 },
        );

        // Publish to agency topic
        const agencyTopic = `jukwa/baraza/agencies/${responsible_agency_id}`;
        fastify.mqtt.publish(
          agencyTopic,
          JSON.stringify({ event: 'commitment_created', data: commitment }),
          { qos: 1 },
        );

        return reply.status(201).send(commitment);
      } catch (err) {
        fastify.log.error({ err }, 'Failed to create commitment');
        return reply.status(500).send({ error: 'Database error' });
      }
    },
  );

  // List commitments with optional filters
  fastify.get<{ Querystring: ListCommitmentsQuery }>(
    '/commitments',
    { schema: listCommitmentsSchema },
    async (request, reply) => {
      const { ward_id, agency_id } = request.query;
      let query = 'SELECT * FROM commitments WHERE 1=1';
      const params: string[] = [];

      if (ward_id) {
        params.push(ward_id);
        query += ` AND affected_ward_id = $${params.length}`;
      }
      if (agency_id) {
        params.push(agency_id);
        query += ` AND responsible_agency_id = $${params.length}`;
      }

      query += ' ORDER BY created_at DESC';

      try {
        const result = await fastify.db.query(query, params);
        return result.rows;
      } catch (err) {
        fastify.log.error({ err }, 'Failed to list commitments');
        return reply.status(500).send({ error: 'Database error' });
      }
    },
  );

  // Get commitment by ID
  fastify.get<{ Params: CommitmentParams }>(
    '/commitments/:id',
    { schema: getCommitmentByIdSchema },
    async (request, reply) => {
      const { id } = request.params;

      try {
        const result = await fastify.db.query(
          'SELECT * FROM commitments WHERE id = $1',
          [id],
        );

        if (result.rows.length === 0) {
          return reply.status(404).send({ error: 'Commitment not found' });
        }

        return result.rows[0];
      } catch (err) {
        fastify.log.error({ err }, 'Failed to get commitment');
        return reply.status(500).send({ error: 'Database error' });
      }
    },
  );

  // Update commitment status (FSM-validated)
  fastify.patch<{ Params: CommitmentParams; Body: UpdateStatusBody }>(
    '/commitments/:id/status',
    { schema: updateCommitmentStatusSchema },
    async (request, reply) => {
      const { id } = request.params;
      const { status: newStatus } = request.body;

      try {
        // Fetch current commitment
        const fetchResult = await fastify.db.query(
          'SELECT * FROM commitments WHERE id = $1',
          [id],
        );

        if (fetchResult.rows.length === 0) {
          return reply.status(404).send({ error: 'Commitment not found' });
        }

        const currentStatus = fetchResult.rows[0].status as CommitmentStatus;
        const allowedTransitions = COMMITMENT_TRANSITIONS[currentStatus];

        if (!allowedTransitions.includes(newStatus)) {
          return reply.status(422).send({
            error: `Invalid transition from ${currentStatus} to ${newStatus}`,
            allowed_transitions: allowedTransitions,
          });
        }

        const updateResult = await fastify.db.query(
          'UPDATE commitments SET status = $1, updated_at = NOW() WHERE id = $2 RETURNING *',
          [newStatus, id],
        );

        return updateResult.rows[0];
      } catch (err) {
        fastify.log.error({ err }, 'Failed to update commitment status');
        return reply.status(500).send({ error: 'Database error' });
      }
    },
  );
}
