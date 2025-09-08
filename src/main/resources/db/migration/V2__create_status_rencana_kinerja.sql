CREATE TABLE IF NOT EXISTS status_rencana_kinerja
(
    id               BIGSERIAL,
    kode_opd         VARCHAR(100),
    nama_opd         VARCHAR(255) NOT NULL,
    nip              VARCHAR(255) NOT NULL,
    nama_atasan      VARCHAR(255) NOT NULL,
    nip_atasan       VARCHAR(255) NOT NULL,
    level_pegawai    INTEGER,
    status           VARCHAR(50)  NOT NULL,
    tahun_verifikasi INTEGER      NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP
)