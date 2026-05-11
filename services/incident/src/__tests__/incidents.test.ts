import { describe, it, expect } from 'vitest';
import {
  VALID_CATEGORIES,
  VALID_ANONYMITY_MODES,
  VALID_STATUSES,
} from '../routes/incidents';

describe('Incident Service Validation Constants', () => {
  it('should have all required incident categories', () => {
    expect(VALID_CATEGORIES).toContain('robbery');
    expect(VALID_CATEGORIES).toContain('pothole');
    expect(VALID_CATEGORIES).toContain('medical_emergency');
    expect(VALID_CATEGORIES).toContain('fire');
    expect(VALID_CATEGORIES).toContain('other');
  });

  it('should have all three anonymity modes', () => {
    expect(VALID_ANONYMITY_MODES).toEqual(['STANDARD', 'INCOGNITO', 'VERIFIED']);
  });

  it('should have all valid statuses', () => {
    expect(VALID_STATUSES).toContain('SUBMITTED');
    expect(VALID_STATUSES).toContain('ACKNOWLEDGED');
    expect(VALID_STATUSES).toContain('IN_PROGRESS');
    expect(VALID_STATUSES).toContain('RESOLVED');
    expect(VALID_STATUSES).toContain('EMERGENCY');
  });

  it('should have at least 30 incident categories', () => {
    expect(VALID_CATEGORIES.length).toBeGreaterThanOrEqual(30);
  });
});
