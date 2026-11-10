CREATE TABLE presenca_dispositivo (
    evento_id    UUID         NOT NULL REFERENCES evento (id),
    device_uuid  VARCHAR(64)  NOT NULL,
    usuario_id   UUID         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (evento_id, device_uuid)
);
