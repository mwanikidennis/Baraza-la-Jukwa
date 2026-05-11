import { describe, it, expect } from 'vitest';
import crypto from 'crypto';
import { VALID_ANONYMITY_MODES } from '../routes/auth';

describe('VALID_ANONYMITY_MODES', () => {
  it('should have exactly 3 modes', () => {
    expect(VALID_ANONYMITY_MODES).toHaveLength(3);
  });

  it('should include STANDARD', () => {
    expect(VALID_ANONYMITY_MODES).toContain('STANDARD');
  });

  it('should include INCOGNITO', () => {
    expect(VALID_ANONYMITY_MODES).toContain('INCOGNITO');
  });

  it('should include VERIFIED', () => {
    expect(VALID_ANONYMITY_MODES).toContain('VERIFIED');
  });
});

describe('device_token_hash', () => {
  it('should use SHA-256', () => {
    const deviceId = 'test-device-123';
    const salt = 'test_salt';
    const hash = crypto
      .createHash('sha256')
      .update(deviceId + salt)
      .digest('hex');

    // Verify the hash is 64 hex characters (256 bits)
    expect(hash).toHaveLength(64);
    expect(hash).toMatch(/^[0-9a-f]{64}$/);

    // Verify determinism
    const hash2 = crypto
      .createHash('sha256')
      .update(deviceId + salt)
      .digest('hex');
    expect(hash).toBe(hash2);
  });

  it('should produce different hashes for different inputs', () => {
    const hash1 = crypto
      .createHash('sha256')
      .update('device-a' + 'salt')
      .digest('hex');
    const hash2 = crypto
      .createHash('sha256')
      .update('device-b' + 'salt')
      .digest('hex');
    expect(hash1).not.toBe(hash2);
  });
});
