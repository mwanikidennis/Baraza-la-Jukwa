// Source: JUKWA Build Kickstart and Master Prompt §3 (monorepo structure)
export const SERVICE_PORTS = {
  incident: 3001,
  commitment: 3002,
  traffic: 3003,
  emergency: 3004,
  civic: 3005,
  identity: 3006,
  notification: 3007,
  payment: 3008,
  accountability: 3009,
  aiClassifier: 3010,
  media: 3011,
  ussd: 3012,
  whatsapp: 3013,
} as const;

export type ServiceName = keyof typeof SERVICE_PORTS;
