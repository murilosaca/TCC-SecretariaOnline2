import { describe, expect, it } from 'vitest';
import { NAV_ITENS, navItensVisiveis } from './navMenu';

describe('navMenu', () => {
  it('não usa whitelist P0: inclui formativas/estágios/tccs/certificados/egresso', () => {
    const rels = NAV_ITENS.map((item) => item.rel);
    expect(rels).toContain('formativas');
    expect(rels).toContain('estagios');
    expect(rels).toContain('tccs');
    expect(rels).toContain('certificados');
    expect(rels).toContain('egresso-inicio');
    expect(rels).not.toContain('deliberar');
    expect(rels).not.toContain('comissoes-caaf');
    expect(rels).not.toContain('alunos');
    expect(rels).not.toContain('comunicacao');
  });

  it('renderiza só itens cujo _link existe (UI cega a perfil)', () => {
    const visiveis = navItensVisiveis({
      inicio: '/inicio',
      formativas: '/formativas',
      certificados: '/certificados',
      contato: '/contato',
    });
    expect(visiveis.map((item) => item.rel)).toEqual(['inicio', 'formativas', 'certificados', 'contato']);
    expect(visiveis.find((item) => item.rel === 'estagios')).toBeUndefined();
  });

  it('menu do egresso mostra início do portal e certificados', () => {
    const visiveis = navItensVisiveis({
      'egresso-inicio': '/egresso/inicio',
      certificados: '/certificados',
      contato: '/contato',
    });
    expect(visiveis.map((item) => item.rel)).toEqual(['egresso-inicio', 'certificados', 'contato']);
    expect(visiveis[0]?.href).toBe('/egresso/inicio');
  });
});
