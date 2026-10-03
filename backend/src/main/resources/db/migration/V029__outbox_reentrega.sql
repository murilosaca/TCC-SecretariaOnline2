-- Fatia 31: quem reentregou o evento. A tabela outbox_event (V003/V010) não é recriada.
-- Status continua texto livre: PENDING, PROCESSING, SENT, FAILED e DEAD (tentativas esgotadas).

ALTER TABLE outbox_event
    ADD COLUMN retried_by UUID;
