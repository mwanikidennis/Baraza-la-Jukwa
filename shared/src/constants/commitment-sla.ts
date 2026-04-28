// Source: JUKWA_BARAZA_Module_Feature_Specification §2.3 (default SLA windows by sector)
import { CommitmentSector } from './commitment-status';

export const DEFAULT_SLA_DAYS: Record<CommitmentSector, number> = {
  [CommitmentSector.HEALTH]: 7,
  [CommitmentSector.SECURITY]: 7,
  [CommitmentSector.WATER]: 30,
  [CommitmentSector.INFRASTRUCTURE]: 90,
  [CommitmentSector.EDUCATION]: 30,
  [CommitmentSector.AGRICULTURE]: 30,
  [CommitmentSector.LAND]: 180,
  [CommitmentSector.EMPLOYMENT]: 30,
};

export const SILENCE_THRESHOLD_HOURS = 72;
export const ESCALATION_THRESHOLD_DAYS = 14;
