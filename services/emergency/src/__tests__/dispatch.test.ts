import { describe, it, expect } from 'vitest';
import { VALID_SOS_TYPES, EMERGENCY_MQTT_QOS, DISPATCH_TOPIC_PREFIX } from '../routes/dispatch';

describe('dispatch route constants', () => {
  describe('VALID_SOS_TYPES', () => {
    it('should contain MEDICAL', () => {
      expect(VALID_SOS_TYPES).toContain('MEDICAL');
    });

    it('should contain SECURITY', () => {
      expect(VALID_SOS_TYPES).toContain('SECURITY');
    });

    it('should contain FIRE', () => {
      expect(VALID_SOS_TYPES).toContain('FIRE');
    });

    it('should contain ACCIDENT', () => {
      expect(VALID_SOS_TYPES).toContain('ACCIDENT');
    });

    it('should have exactly 4 types', () => {
      expect(VALID_SOS_TYPES).toHaveLength(4);
    });
  });

  describe('EMERGENCY_MQTT_QOS', () => {
    it('should be 2 (EXACTLY_ONCE)', () => {
      expect(EMERGENCY_MQTT_QOS).toBe(2);
    });
  });

  describe('DISPATCH_TOPIC_PREFIX', () => {
    it('should be jukwa/emergency', () => {
      expect(DISPATCH_TOPIC_PREFIX).toBe('jukwa/emergency');
    });

    it('should produce correct topic format when combined with county', () => {
      const county = 'nairobi';
      const topic = `${DISPATCH_TOPIC_PREFIX}/${county}/dispatch`;
      expect(topic).toBe('jukwa/emergency/nairobi/dispatch');
    });

    it('should produce correct topic format for different counties', () => {
      const counties = ['mombasa', 'kisumu', 'nakuru'];
      counties.forEach((county) => {
        const topic = `${DISPATCH_TOPIC_PREFIX}/${county}/dispatch`;
        expect(topic).toBe(`jukwa/emergency/${county}/dispatch`);
        expect(topic).toMatch(/^jukwa\/emergency\/[a-z]+\/dispatch$/);
      });
    });
  });
});
