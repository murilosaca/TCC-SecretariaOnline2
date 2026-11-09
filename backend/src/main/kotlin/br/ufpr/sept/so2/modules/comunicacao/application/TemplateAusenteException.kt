package br.ufpr.sept.so2.modules.comunicacao.application

/** O despacho marca FAILED com last_error, sem fechar SENT. */
class TemplateAusenteException(message: String) : RuntimeException(message)
