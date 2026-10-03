-- Fatia 30 / F7.7: diff antes/depois na trilha já append-only (V003).
-- A tabela não é recriada. UPDATE e DELETE continuam proibidos pelas rules.

ALTER TABLE audit_log ADD COLUMN payload_antes TEXT;
ALTER TABLE audit_log ADD COLUMN payload_depois TEXT;
