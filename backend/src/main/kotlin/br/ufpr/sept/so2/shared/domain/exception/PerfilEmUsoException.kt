package br.ufpr.sept.so2.shared.domain.exception

class PerfilEmUsoException(val usuarios: Int) :
    RuntimeException("$usuarios usuários ativos com este perfil")
