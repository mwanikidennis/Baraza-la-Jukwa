// Source: JUKWA_BARAZA_Module_Feature_Specification §2.2 (commitment lifecycle FSM)
export enum CommitmentStatus {
  CAPTURED = 'CAPTURED',
  CLASSIFIED = 'CLASSIFIED',
  ACKNOWLEDGED = 'ACKNOWLEDGED',
  CLARIFICATION_REQUIRED = 'CLARIFICATION_REQUIRED',
  IN_PROGRESS = 'IN_PROGRESS',
  FULFILLED = 'FULFILLED',
  SILENCE = 'SILENCE',
  ESCALATED = 'ESCALATED',
  OVERDUE = 'OVERDUE',
  VERIFIED_RESOLVED = 'VERIFIED_RESOLVED',
  FAILED = 'FAILED',
}

export const COMMITMENT_STATE_TRANSITIONS: Record<CommitmentStatus, CommitmentStatus[]> = {
  [CommitmentStatus.CAPTURED]: [CommitmentStatus.CLASSIFIED],
  [CommitmentStatus.CLASSIFIED]: [
    CommitmentStatus.ACKNOWLEDGED,
    CommitmentStatus.SILENCE,
    CommitmentStatus.CLARIFICATION_REQUIRED,
  ],
  [CommitmentStatus.ACKNOWLEDGED]: [CommitmentStatus.IN_PROGRESS],
  [CommitmentStatus.CLARIFICATION_REQUIRED]: [
    CommitmentStatus.ESCALATED,
    CommitmentStatus.ACKNOWLEDGED,
  ],
  [CommitmentStatus.IN_PROGRESS]: [CommitmentStatus.FULFILLED, CommitmentStatus.OVERDUE],
  [CommitmentStatus.SILENCE]: [CommitmentStatus.ESCALATED],
  [CommitmentStatus.ESCALATED]: [CommitmentStatus.ACKNOWLEDGED, CommitmentStatus.OVERDUE],
  [CommitmentStatus.FULFILLED]: [CommitmentStatus.VERIFIED_RESOLVED, CommitmentStatus.IN_PROGRESS],
  [CommitmentStatus.OVERDUE]: [CommitmentStatus.FAILED, CommitmentStatus.FULFILLED],
  [CommitmentStatus.VERIFIED_RESOLVED]: [],
  [CommitmentStatus.FAILED]: [],
};

export function canTransition(from: CommitmentStatus, to: CommitmentStatus): boolean {
  return COMMITMENT_STATE_TRANSITIONS[from].includes(to);
}

export enum CommitmentOriginType {
  JIM_BARAZA = 'JIM_BARAZA',
  CITIZEN_CAPTURE = 'CITIZEN_CAPTURE',
  DIGITAL_SUBMISSION = 'DIGITAL_SUBMISSION',
  OFFICIAL_ANNOUNCEMENT = 'OFFICIAL_ANNOUNCEMENT',
  JIM_TICKET_IMPORT = 'JIM_TICKET_IMPORT',
}

export enum CommitmentSector {
  HEALTH = 'health',
  INFRASTRUCTURE = 'infrastructure',
  WATER = 'water',
  SECURITY = 'security',
  AGRICULTURE = 'agriculture',
  EDUCATION = 'education',
  LAND = 'land',
  EMPLOYMENT = 'employment',
}

export enum BarazaSessionType {
  JIM_OFFICIAL = 'JIM_OFFICIAL',
  COUNTY_FORUM = 'COUNTY_FORUM',
  WARD_BARAZA = 'WARD_BARAZA',
  CITIZEN_CAPTURED = 'CITIZEN_CAPTURED',
}

export enum EvidenceType {
  CREATION_AUDIO = 'CREATION_AUDIO',
  CREATION_VIDEO = 'CREATION_VIDEO',
  PROGRESS_UPDATE = 'PROGRESS_UPDATE',
  AGENCY_REPORT = 'AGENCY_REPORT',
  CITIZEN_VERIFICATION = 'CITIZEN_VERIFICATION',
  CITIZEN_DISPUTE = 'CITIZEN_DISPUTE',
  ESCALATION_NOTICE = 'ESCALATION_NOTICE',
}

export enum VerificationVote {
  CONFIRMED = 'CONFIRMED',
  DISPUTED = 'DISPUTED',
}
