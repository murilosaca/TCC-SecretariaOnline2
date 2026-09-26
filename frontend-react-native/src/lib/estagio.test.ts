import { describe, expect, it } from 'vitest';
import { dataBr, rotuloSituacaoEstagio, vigencia } from './estagio';

describe('estagio', () => {
  it('formata vigência e situação', () => {
    expect(rotuloSituacaoEstagio('ATIVO')).toBe('Ativo');
    expect(dataBr('2026-03-15')).toBe('15/03/2026');
    expect(vigencia('2026-03-01', '2026-06-30')).toBe('01/03/2026 – 30/06/2026');
  });
});
