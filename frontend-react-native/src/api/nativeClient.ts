export const NATIVE_CLIENT_HEADER = 'X-SO2-Client';
export const NATIVE_CLIENT_VALUE = 'native';

export function loginBody(identificador: string, senha: string): { identificador: string; senha: string } {
  return { identificador, senha };
}
