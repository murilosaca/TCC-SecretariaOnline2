package br.ufpr.sept.so2.modules.importacao.application

import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Component
class DepoisDoCommit {
    fun executar(acao: () -> Unit) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    acao()
                }
            })
        } else {
            acao()
        }
    }
}
