package br.ufpr.sept.so2.shared.domain.exception

class RateLimitExcedidoException(val retryAfterSeconds: Int) :
    RuntimeException("Muitas tentativas. Aguarde antes de tentar novamente.")
