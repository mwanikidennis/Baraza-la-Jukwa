// Source: Technical Build Bible Part I §1.2 (offline-first sync engine)
export enum SyncPriority {
  EMERGENCY = 0,
  SECURITY = 1,
  TRAFFIC = 2,
  CIVIC = 3,
  GENERAL = 4,
}

export const SYNC_INTERVAL_MS: Record<SyncPriority, number> = {
  [SyncPriority.EMERGENCY]: 10_000,
  [SyncPriority.SECURITY]: 60_000,
  [SyncPriority.TRAFFIC]: 300_000,
  [SyncPriority.CIVIC]: 600_000,
  [SyncPriority.GENERAL]: 3_600_000,
};

export type NetworkClass = 'WiFi' | '4G_LTE' | '3G' | '2G_EDGE' | 'offline';

export const SYNC_STRATEGY_BY_NETWORK: Record<
  NetworkClass,
  {
    syncOrder: SyncPriority[];
    mediaQuality: 'full-res' | 'compressed' | 'none';
    fallbackSms: boolean;
  }
> = {
  WiFi: {
    syncOrder: [
      SyncPriority.EMERGENCY,
      SyncPriority.SECURITY,
      SyncPriority.TRAFFIC,
      SyncPriority.CIVIC,
      SyncPriority.GENERAL,
    ],
    mediaQuality: 'full-res',
    fallbackSms: false,
  },
  '4G_LTE': {
    syncOrder: [
      SyncPriority.EMERGENCY,
      SyncPriority.SECURITY,
      SyncPriority.TRAFFIC,
      SyncPriority.CIVIC,
    ],
    mediaQuality: 'compressed',
    fallbackSms: false,
  },
  '3G': {
    syncOrder: [SyncPriority.EMERGENCY, SyncPriority.SECURITY],
    mediaQuality: 'none',
    fallbackSms: true,
  },
  '2G_EDGE': {
    syncOrder: [SyncPriority.EMERGENCY],
    mediaQuality: 'none',
    fallbackSms: true,
  },
  offline: {
    syncOrder: [],
    mediaQuality: 'none',
    fallbackSms: false,
  },
};
