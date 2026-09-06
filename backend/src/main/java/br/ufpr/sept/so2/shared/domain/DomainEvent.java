package br.ufpr.sept.so2.shared.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();

    OffsetDateTime occurredAt();

    String type();
}
