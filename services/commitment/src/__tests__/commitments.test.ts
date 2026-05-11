import { describe, it, expect } from 'vitest';

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

const ORIGIN_TYPES = [
  'BARAZA_SESSION',
  'CITIZEN_REPORT',
  'JIM_TICKET',
  'MEDIA_REPORT',
] as const;

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

describe('Commitment Statuses', () => {
  it('should define all 11 commitment statuses', () => {
    expect(COMMITMENT_STATUSES).toHaveLength(11);
    expect(COMMITMENT_STATUSES).toContain('CAPTURED');
    expect(COMMITMENT_STATUSES).toContain('CLASSIFIED');
    expect(COMMITMENT_STATUSES).toContain('ACKNOWLEDGED');
    expect(COMMITMENT_STATUSES).toContain('SILENCE');
    expect(COMMITMENT_STATUSES).toContain('CLARIFICATION_REQUIRED');
    expect(COMMITMENT_STATUSES).toContain('IN_PROGRESS');
    expect(COMMITMENT_STATUSES).toContain('ESCALATED');
    expect(COMMITMENT_STATUSES).toContain('FULFILLED');
    expect(COMMITMENT_STATUSES).toContain('OVERDUE');
    expect(COMMITMENT_STATUSES).toContain('VERIFIED_RESOLVED');
    expect(COMMITMENT_STATUSES).toContain('FAILED');
  });
});

describe('Commitment Transitions', () => {
  it('should have valid transitions for all statuses', () => {
    for (const status of COMMITMENT_STATUSES) {
      const transitions = COMMITMENT_TRANSITIONS[status];
      expect(Array.isArray(transitions)).toBe(true);
      for (const target of transitions) {
        expect(COMMITMENT_STATUSES).toContain(target);
      }
    }
  });

  it('should allow CAPTURED to transition to CLASSIFIED', () => {
    expect(COMMITMENT_TRANSITIONS.CAPTURED).toContain('CLASSIFIED');
  });

  it('should have VERIFIED_RESOLVED as a terminal state with empty transitions array', () => {
    expect(COMMITMENT_TRANSITIONS.VERIFIED_RESOLVED).toEqual([]);
  });
});

describe('Origin Types', () => {
  it('should define all 4 origin types', () => {
    expect(ORIGIN_TYPES).toHaveLength(4);
    expect(ORIGIN_TYPES).toContain('BARAZA_SESSION');
    expect(ORIGIN_TYPES).toContain('CITIZEN_REPORT');
    expect(ORIGIN_TYPES).toContain('JIM_TICKET');
    expect(ORIGIN_TYPES).toContain('MEDIA_REPORT');
  });
});

describe('Sectors', () => {
  it('should define all 12 sectors', () => {
    expect(SECTORS).toHaveLength(12);
    expect(SECTORS).toContain('HEALTH');
    expect(SECTORS).toContain('SECURITY');
    expect(SECTORS).toContain('WATER');
    expect(SECTORS).toContain('INFRASTRUCTURE');
    expect(SECTORS).toContain('LAND');
    expect(SECTORS).toContain('EDUCATION');
    expect(SECTORS).toContain('AGRICULTURE');
    expect(SECTORS).toContain('EMPLOYMENT');
    expect(SECTORS).toContain('ENVIRONMENT');
    expect(SECTORS).toContain('TRANSPORT');
    expect(SECTORS).toContain('HOUSING');
    expect(SECTORS).toContain('FINANCE');
  });
});
