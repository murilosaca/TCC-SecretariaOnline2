import { describe, expect, it } from 'vitest';
import { entregaProxima, rotuloEstadoTcc } from './tcc';

describe('tcc', () => {
  it('rotula estado e detecta prazo próximo', () => {
    expect(rotuloEstadoTcc('EM_ELABORACAO')).toBe('Em elaboração');
    expect(entregaProxima('2026-09-30', new Date(2026, 8, 26))).toBe(true);
    expect(entregaProxima('2026-12-31', new Date(2026, 8, 26))).toBe(false);
  });
});
