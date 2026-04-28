/**
 * BARAZA: Default Agency SLA Windows (Days)
 * Source: BARAZA Spec §2.3
 */

export const DEFAULT_SLA_DAYS = {
  EMERGENCY: 7,      // drug shortages, broken water mains, security threats
  SERVICE: 30,       // staffing, equipment, process improvements
  INFRASTRUCTURE: 90, // road repairs, building construction, facility upgrades
  POLICY: 180        // regulatory changes, program expansions, budget allocations
} as const;

/**
 * Sector to SLA Type Mapping
 */
export const SECTOR_SLA_MAPPING: Record<string, keyof typeof DEFAULT_SLA_DAYS> = {
  "HEALTH": "EMERGENCY",
  "SECURITY": "EMERGENCY",
  "WATER": "EMERGENCY",
  "INFRASTRUCTURE": "INFRASTRUCTURE",
  "LAND": "POLICY",
  "EDUCATION": "SERVICE",
  "AGRICULTURE": "SERVICE",
  "EMPLOYMENT": "SERVICE"
};
