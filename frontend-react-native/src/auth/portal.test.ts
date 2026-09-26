import { describe, expect, it } from 'vitest';
import { inicioDaSessao, portalEgresso, rotaBloqueada, rotaExclusivaAluno } from './portal';

const aluno = { inicio: '/inicio', formativas: '/formativas', contato: '/contato' };
const egresso = { 'egresso-inicio': '/egresso/inicio', certificados: '/certificados', contato: '/contato' };

describe('portal', () => {
  it('detecta portal do egresso só com egresso-inicio', () => {
    expect(portalEgresso(egresso)).toBe(true);
    expect(portalEgresso(aluno)).toBe(false);
  });

  it('inicioDaSessao aponta para /egresso/inicio ou /inicio', () => {
    expect(inicioDaSessao(egresso)).toBe('/egresso/inicio');
    expect(inicioDaSessao(aluno)).toBe('/inicio');
  });

  it('bloqueia rotas exclusivas de aluno no portal egresso', () => {
    expect(rotaExclusivaAluno('/formativas')).toBe(true);
    expect(rotaExclusivaAluno('/formativas/abc')).toBe(true);
    expect(rotaExclusivaAluno('/certificados')).toBe(false);
    expect(rotaBloqueada('/inicio', egresso)).toBe(true);
    expect(rotaBloqueada('/formativas', egresso)).toBe(true);
    expect(rotaBloqueada('/egresso/inicio', egresso)).toBe(false);
    expect(rotaBloqueada('/egresso/inicio', aluno)).toBe(true);
  });
});
