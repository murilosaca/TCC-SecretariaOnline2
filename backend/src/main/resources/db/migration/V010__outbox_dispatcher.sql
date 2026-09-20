-- Dispatcher at-least-once (RNF-DES-05 / RNF-CON-01). Não redesenha outbox_event.
-- Status continua PENDING | PROCESSING | SENT | FAILED. last_error sem e-mail em claro.

ALTER TABLE outbox_event
    ADD COLUMN last_error VARCHAR(500),
    ADD COLUMN processed_at TIMESTAMPTZ;
