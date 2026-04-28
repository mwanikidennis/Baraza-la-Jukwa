/**
 * BARAZA: Commitment Lifecycle States & FSM Transitions
 * Source: BARAZA Spec §2.2
 */

export enum CommitmentStatus {
  CAPTURED = "CAPTURED",
  CLASSIFIED = "CLASSIFIED",
  ACKNOWLEDGED = "ACKNOWLEDGED",
  SILENCE = "SILENCE",
  CLARIFICATION_REQUIRED = "CLARIFICATION_REQUIRED",
  IN_PROGRESS = "IN_PROGRESS",
  ESCALATED = "ESCALATED",
  FULFILLED = "FULFILLED",
  OVERDUE = "OVERDUE",
  VERIFIED_RESOLVED = "VERIFIED_RESOLVED",
  FAILED = "FAILED"
}

/**
 * Finite State Machine Transition Map
 */
export const COMMITMENT_TRANSITIONS: Record<CommitmentStatus, CommitmentStatus[]> = {
  [CommitmentStatus.CAPTURED]: [CommitmentStatus.CLASSIFIED],
  [CommitmentStatus.CLASSIFIED]: [
    CommitmentStatus.ACKNOWLEDGED,
    CommitmentStatus.SILENCE,
    CommitmentStatus.CLARIFICATION_REQUIRED
  ],
  [CommitmentStatus.ACKNOWLEDGED]: [CommitmentStatus.IN_PROGRESS, CommitmentStatus.ESCALATED],
  [CommitmentStatus.SILENCE]: [CommitmentStatus.ESCALATED],
  [CommitmentStatus.CLARIFICATION_REQUIRED]: [CommitmentStatus.ESCALATED, CommitmentStatus.ACKNOWLEDGED],
  [CommitmentStatus.IN_PROGRESS]: [CommitmentStatus.FULFILLED, CommitmentStatus.OVERDUE, CommitmentStatus.ESCALATED],
  [CommitmentStatus.ESCALATED]: [CommitmentStatus.IN_PROGRESS, CommitmentStatus.OVERDUE, CommitmentStatus.FAILED],
  [CommitmentStatus.FULFILLED]: [CommitmentStatus.VERIFIED_RESOLVED, CommitmentStatus.IN_PROGRESS],
  [CommitmentStatus.OVERDUE]: [CommitmentStatus.FAILED, CommitmentStatus.FULFILLED, CommitmentStatus.ESCALATED],
  [CommitmentStatus.VERIFIED_RESOLVED]: [], // Terminal state
  [CommitmentStatus.FAILED]: [] // Terminal state
};
