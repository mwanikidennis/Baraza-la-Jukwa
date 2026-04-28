// Source: Master Prompt §3 + Architecture Framework §6.1
// QoS: 0 = at most once (lossy telemetry), 1 = at least once (alerts), 2 = exactly once (emergency)

export const MQTT_QOS = {
  SENSOR_TELEMETRY: 0,
  CITIZEN_ALERT: 1,
  EMERGENCY_DISPATCH: 2,
} as const;

export const MQTT_TOPICS = {
  alerts: (county: string, ward: string, category: string) =>
    `jukwa/alerts/${county}/${ward}/${category}`,
  trafficSensor: (junctionId: string) => `jukwa/traffic/sensors/${junctionId}`,
  trafficAlerts: (corridor: string) => `jukwa/traffic/alerts/${corridor}`,
  incidentStatus: (incidentId: string) => `jukwa/incidents/${incidentId}/status`,
  emergencyDispatch: (county: string) => `jukwa/emergency/${county}/dispatch`,
  barazaCommitments: (wardId: string) => `jukwa/baraza/${wardId}/commitments`,
  barazaVerifications: (wardId: string) => `jukwa/baraza/${wardId}/verifications`,
  barazaAgency: (agencyId: string) => `jukwa/baraza/agencies/${agencyId}`,
  barazaEscalations: 'jukwa/baraza/escalations',
  civicPolls: (ward: string) => `jukwa/civic/${ward}/polls`,
  nationalScorecards: 'jukwa/baraza/national/scorecards',
} as const;

// Static patterns (for ACL matching, with + wildcards)
export const MQTT_TOPIC_PATTERNS = {
  alerts: 'jukwa/alerts/+/+/+',
  trafficSensors: 'jukwa/traffic/sensors/+',
  trafficAlerts: 'jukwa/traffic/alerts/+',
  incidentStatus: 'jukwa/incidents/+/status',
  emergencyDispatch: 'jukwa/emergency/+/dispatch',
  barazaCommitments: 'jukwa/baraza/+/commitments',
  barazaVerifications: 'jukwa/baraza/+/verifications',
  barazaAgency: 'jukwa/baraza/agencies/+',
  barazaEscalations: 'jukwa/baraza/escalations',
  civicPolls: 'jukwa/civic/+/polls',
  nationalScorecards: 'jukwa/baraza/national/scorecards',
} as const;
