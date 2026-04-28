// Source: Technical Build Bible Part I §1.1 + Architecture Framework §5.5
export enum IncidentSeverity {
  LOW = 1,
  MEDIUM = 2,
  HIGH = 3,
  VERY_HIGH = 4,
  CRITICAL = 5,
}

export enum AnonymityMode {
  STANDARD = 'STANDARD',
  INCOGNITO = 'INCOGNITO',
  VERIFIED = 'VERIFIED',
}

export enum IncidentStatus {
  REPORTED = 'REPORTED',
  CLASSIFIED = 'CLASSIFIED',
  ROUTED = 'ROUTED',
  ACKNOWLEDGED = 'ACKNOWLEDGED',
  IN_PROGRESS = 'IN_PROGRESS',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED',
  REJECTED = 'REJECTED',
}

export enum IncidentCategory {
  // Security
  ROBBERY = 'robbery',
  ASSAULT = 'assault',
  THEFT = 'theft',
  SUSPICIOUS_ACTIVITY = 'suspicious_activity',
  GANG_ACTIVITY = 'gang_activity',
  DRUG_TRAFFICKING = 'drug_trafficking',
  DOMESTIC_VIOLENCE = 'domestic_violence',
  STALKING = 'stalking',
  // Traffic & transport
  POTHOLE = 'pothole',
  CONGESTION = 'congestion',
  ACCIDENT = 'accident',
  PUBLIC_TRANSPORT_DELAY = 'public_transport_delay',
  ROAD_CLOSURE = 'road_closure',
  UNSAFE_DRIVING = 'unsafe_driving',
  MATATU_VIOLATION = 'matatu_violation',
  // Infrastructure & water
  BROKEN_WATER_MAIN = 'broken_water_main',
  NO_ELECTRICITY = 'no_electricity',
  DAMAGED_STREETLIGHT = 'damaged_streetlight',
  BUILDING_COLLAPSE_RISK = 'building_collapse_risk',
  WASTE_MANAGEMENT = 'waste_management',
  // Health
  DRUG_SHORTAGE = 'drug_shortage',
  FACILITY_CLOSURE = 'facility_closure',
  STAFF_SHORTAGE = 'staff_shortage',
  DISEASE_OUTBREAK = 'disease_outbreak',
  // Environment & land
  ILLEGAL_DUMPING = 'illegal_dumping',
  DEFORESTATION = 'deforestation',
  LAND_GRABBING = 'land_grabbing',
  AIR_POLLUTION = 'air_pollution',
  NOISE_POLLUTION = 'noise_pollution',
  // Employment & services
  JOB_SCAM = 'job_scam',
  WAGE_THEFT = 'wage_theft',
  UNFAIR_LABOR = 'unfair_labor',
  // Agriculture
  CROP_DISEASE = 'crop_disease',
  PEST_INFESTATION = 'pest_infestation',
  LIVESTOCK_DISEASE = 'livestock_disease',
  AGRICULTURAL_INPUTS = 'agricultural_inputs',
  // Emergency
  MEDICAL_EMERGENCY = 'medical_emergency',
  FIRE = 'fire',
  // Catch-all
  OTHER = 'other',
}

// Default sync priority assignment by category (drives the offline queue).
import { SyncPriority } from './sync-priority';

export const CATEGORY_TO_PRIORITY: Record<IncidentCategory, SyncPriority> = {
  [IncidentCategory.MEDICAL_EMERGENCY]: SyncPriority.EMERGENCY,
  [IncidentCategory.FIRE]: SyncPriority.EMERGENCY,
  [IncidentCategory.ACCIDENT]: SyncPriority.EMERGENCY,
  [IncidentCategory.BUILDING_COLLAPSE_RISK]: SyncPriority.EMERGENCY,

  [IncidentCategory.ROBBERY]: SyncPriority.SECURITY,
  [IncidentCategory.ASSAULT]: SyncPriority.SECURITY,
  [IncidentCategory.GANG_ACTIVITY]: SyncPriority.SECURITY,
  [IncidentCategory.DRUG_TRAFFICKING]: SyncPriority.SECURITY,
  [IncidentCategory.DOMESTIC_VIOLENCE]: SyncPriority.SECURITY,
  [IncidentCategory.STALKING]: SyncPriority.SECURITY,
  [IncidentCategory.SUSPICIOUS_ACTIVITY]: SyncPriority.SECURITY,
  [IncidentCategory.THEFT]: SyncPriority.SECURITY,

  [IncidentCategory.POTHOLE]: SyncPriority.TRAFFIC,
  [IncidentCategory.CONGESTION]: SyncPriority.TRAFFIC,
  [IncidentCategory.PUBLIC_TRANSPORT_DELAY]: SyncPriority.TRAFFIC,
  [IncidentCategory.ROAD_CLOSURE]: SyncPriority.TRAFFIC,
  [IncidentCategory.UNSAFE_DRIVING]: SyncPriority.TRAFFIC,
  [IncidentCategory.MATATU_VIOLATION]: SyncPriority.TRAFFIC,

  [IncidentCategory.BROKEN_WATER_MAIN]: SyncPriority.CIVIC,
  [IncidentCategory.NO_ELECTRICITY]: SyncPriority.CIVIC,
  [IncidentCategory.DAMAGED_STREETLIGHT]: SyncPriority.CIVIC,
  [IncidentCategory.WASTE_MANAGEMENT]: SyncPriority.CIVIC,
  [IncidentCategory.DRUG_SHORTAGE]: SyncPriority.CIVIC,
  [IncidentCategory.FACILITY_CLOSURE]: SyncPriority.CIVIC,
  [IncidentCategory.STAFF_SHORTAGE]: SyncPriority.CIVIC,
  [IncidentCategory.DISEASE_OUTBREAK]: SyncPriority.SECURITY,
  [IncidentCategory.ILLEGAL_DUMPING]: SyncPriority.CIVIC,
  [IncidentCategory.DEFORESTATION]: SyncPriority.CIVIC,
  [IncidentCategory.LAND_GRABBING]: SyncPriority.CIVIC,
  [IncidentCategory.AIR_POLLUTION]: SyncPriority.CIVIC,
  [IncidentCategory.NOISE_POLLUTION]: SyncPriority.CIVIC,
  [IncidentCategory.JOB_SCAM]: SyncPriority.CIVIC,
  [IncidentCategory.WAGE_THEFT]: SyncPriority.CIVIC,
  [IncidentCategory.UNFAIR_LABOR]: SyncPriority.CIVIC,
  [IncidentCategory.CROP_DISEASE]: SyncPriority.CIVIC,
  [IncidentCategory.PEST_INFESTATION]: SyncPriority.CIVIC,
  [IncidentCategory.LIVESTOCK_DISEASE]: SyncPriority.CIVIC,
  [IncidentCategory.AGRICULTURAL_INPUTS]: SyncPriority.CIVIC,

  [IncidentCategory.OTHER]: SyncPriority.GENERAL,
};
