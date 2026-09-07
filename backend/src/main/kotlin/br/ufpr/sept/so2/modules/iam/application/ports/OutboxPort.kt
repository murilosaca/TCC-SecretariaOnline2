package br.ufpr.sept.so2.modules.iam.application.ports

interface OutboxPort {
    fun enqueue(tipo: String, payload: String)
}
