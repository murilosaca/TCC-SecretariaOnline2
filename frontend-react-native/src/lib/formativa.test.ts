import { describe, expect, it } from 'vitest';
import { rotuloEstadoFormativa, rotuloOrigemFormativa } from './formativa';

describe('formativa', () => {
  it('rotula estados e origem', () => {
    expect(rotuloEstadoFormativa('PENDENTE_CONFIRMACAO')).toBe('Aguardando confirmação');
    expect(rotuloEstadoFormativa('AGUARDANDO_CAAF')).toBe('Aguardando CAAF');
    expect(rotuloOrigemFormativa('PRESENCA_VALIDADA')).toBe('Presença validada');
  });
});
