/**
 * JUKWA Sync Priority & Network Strategy
 * Source: Tech Bible Part I §1.2, Master Prompt §3
 */

export enum SyncPriority {
  EMERGENCY = "EMERGENCY", // immediate
  SECURITY = "SECURITY",   // 60s
  TRAFFIC = "TRAFFIC",     // 5min
  CIVIC = "CIVIC",         // 30min
  GENERAL = "GENERAL"      // WiFi or 24hr
}

export const SYNC_INTERVALS_MS = {
  [SyncPriority.EMERGENCY]: 0,
  [SyncPriority.SECURITY]: 60 * 1000,
  [SyncPriority.TRAFFIC]: 5 * 60 * 1000,
  [SyncPriority.CIVIC]: 30 * 60 * 1000,
  [SyncPriority.GENERAL]: 24 * 60 * 60 * 1000
} as const;

/**
 * Network-aware sync strategy
 */
export const NETWORK_SYNC_STRATEGY = {
  WIFI: {
    syncAll: true,
    mediaRes: "FULL"
  },
  CELL_4G_LTE: {
    priorities: [SyncPriority.EMERGENCY, SyncPriority.SECURITY, SyncPriority.TRAFFIC, SyncPriority.CIVIC],
    mediaRes: "COMPRESSED"
  },
  CELL_3G_2G: {
    priorities: [SyncPriority.EMERGENCY],
    mediaRes: "TEXT_ONLY",
    fallback: "SMS"
  }
} as const;
