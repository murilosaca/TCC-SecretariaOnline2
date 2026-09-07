package br.ufpr.sept.so2.shared.domain

abstract class AggregateRoot {
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    protected fun registerEvent(event: DomainEvent) {
        domainEvents += event
    }

    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()

    fun clearEvents(): List<DomainEvent> {
        val snapshot = domainEvents.toList()
        domainEvents.clear()
        return snapshot
    }
}
