import { describe, expect, it } from 'vitest';
import { loginBody, NATIVE_CLIENT_HEADER, NATIVE_CLIENT_VALUE } from './nativeClient';

describe('payload de login nativo', () => {
  it('envia identificador e senha, nunca login', () => {
    const body = loginBody('aluno.dev@ufpr.br', 'TroqueEstaSenha1!');
    expect(body).toEqual({ identificador: 'aluno.dev@ufpr.br', senha: 'TroqueEstaSenha1!' });
    expect(body).not.toHaveProperty('login');
    expect(NATIVE_CLIENT_HEADER).toBe('X-SO2-Client');
    expect(NATIVE_CLIENT_VALUE).toBe('native');
  });
});
