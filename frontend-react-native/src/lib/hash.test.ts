import { describe, expect, it } from 'vitest';
import { truncarHash } from './hash';

describe('truncarHash', () => {
  it('abrevia hash longo', () => {
    expect(truncarHash('abcd1234efgh5678ijkl')).toBe('abcd1234…ijkl');
    expect(truncarHash('curto')).toBe('curto');
    expect(truncarHash(null)).toBe('—');
  });
});
