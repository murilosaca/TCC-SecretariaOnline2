import { describe, expect, it } from 'vitest';
import { useActions } from './useActions';

describe('useActions', () => {
  it('libera ação somente quando o _link existe, sem olhar authorities', () => {
    const actions = useActions({ inicio: '/inicio', solicitacoes: '/solicitacoes' });
    expect(actions.can('inicio')).toBe(true);
    expect(actions.can('cursos')).toBe(false);
    expect(actions.href('solicitacoes')).toBe('/solicitacoes');
  });
});
