package br.ufpr.sept.so2.shared.domain.exception

class SchemaInvalidoException(val tipo: String, message: String) : RuntimeException(message)
