/**
 * JUKWA MQTT Topic Hierarchy & QoS Table
 * Sources: Master Prompt §3, Architecture §6.1
 */

export enum MqttQoS {
  AT_MOST_ONCE = 0,
  AT_LEAST_ONCE = 1,
  EXACTLY_ONCE = 2
}

export const MQTT_TOPICS = {
  // Citizen-facing alerts (QoS 1)
  ALERTS: "jukwa/alerts/{county}/{ward}/{category}",
  
  // ITS sensor telemetry - Inbound (QoS 0)
  TRAFFIC_SENSORS: "jukwa/traffic/sensors/{junction_id}",
  
  // Processed traffic alerts (QoS 1)
  TRAFFIC_ALERTS: "jukwa/traffic/alerts/{corridor}",
  
  // Individual incident status updates (QoS 1)
  INCIDENT_STATUS: "jukwa/incidents/{incident_id}/status",
  
  // Emergency dispatch - Restricted (QoS 2)
  EMERGENCY_DISPATCH: "jukwa/emergency/{county}/dispatch",
  
  // BARAZA: Ward-level commitment updates (QoS 1)
  BARAZA_COMMITMENTS: "jukwa/baraza/{ward_id}/commitments",
  
  // BARAZA: Citizen verification requests (QoS 1)
  BARAZA_VERIFICATIONS: "jukwa/baraza/{ward_id}/verifications",
  
  // BARAZA: Agency-specific updates (QoS 1)
  BARAZA_AGENCY: "jukwa/baraza/agencies/{agency_id}",
  
  // Civic participation events/polls (QoS 1)
  CIVIC_POLLS: "jukwa/civic/{ward}/polls",
  
  // Aggregate scorecard updates (QoS 1)
  NATIONAL_SCORECARDS: "jukwa/baraza/national/scorecards"
} as const;

export const TOPIC_QOS_MAP: Record<string, MqttQoS> = {
  "jukwa/alerts/": MqttQoS.AT_LEAST_ONCE,
  "jukwa/traffic/sensors/": MqttQoS.AT_MOST_ONCE,
  "jukwa/traffic/alerts/": MqttQoS.AT_LEAST_ONCE,
  "jukwa/incidents/": MqttQoS.AT_LEAST_ONCE,
  "jukwa/emergency/": MqttQoS.EXACTLY_ONCE,
  "jukwa/baraza/": MqttQoS.AT_LEAST_ONCE,
  "jukwa/civic/": MqttQoS.AT_LEAST_ONCE
};
