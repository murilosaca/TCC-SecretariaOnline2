-- Fatia 15: PDF deixa bytea e passa a object key no MinIO/S3 (RNF-POR-03).
-- Bytes legados migram no ApplicationReady (ByteaParaStorageMigrator); depois pdf/conteudo ficam nulos.

ALTER TABLE certificado
    ADD COLUMN storage_key VARCHAR(512);

ALTER TABLE certificado
    ALTER COLUMN pdf DROP NOT NULL;

ALTER TABLE estagio_documento
    ADD COLUMN storage_key VARCHAR(512);

ALTER TABLE tcc
    ADD COLUMN storage_key VARCHAR(512);

CREATE INDEX idx_certificado_storage_key ON certificado (storage_key);
CREATE INDEX idx_estagio_documento_storage_key ON estagio_documento (storage_key);
CREATE INDEX idx_tcc_storage_key ON tcc (storage_key);
