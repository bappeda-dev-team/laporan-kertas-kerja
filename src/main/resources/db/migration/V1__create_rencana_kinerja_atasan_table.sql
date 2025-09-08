CREATE TABLE IF NOT EXISTS rencana_kinerja_atasan
(
    id                         BIGSERIAL PRIMARY KEY,
    nama                       VARCHAR(100) NOT NULL,
    nip                        VARCHAR(255) NOT NULL,
    level_pegawai              INTEGER      NOT NULL,
    id_rencana_kinerja         VARCHAR(100) NOT NULL UNIQUE,
    nama_rencana_kinerja       VARCHAR(255) NOT NULL,
    id_rencana_kinerja_bawahan VARCHAR(100) NOT NULL,
    nip_bawahan                VARCHAR(255) NOT NULL,
    kode_program               VARCHAR(100) NOT NULL,
    program                    VARCHAR(255) NOT NULL,
    kode_kegiatan              VARCHAR(100) NOT NULL,
    kegiatan                   VARCHAR(100) NOT NULL,
    kode_sub_kegiatan          VARCHAR(100) NOT NULL,
    sub_kegiatan               VARCHAR(100) NOT NULL,
    pagu_anggaran              INTEGER,
    indikator                  VARCHAR(255) NOT NULL,
    target                     VARCHAR(20)  NOT NULL,
    satuan                     VARCHAR(20)  NOT NULL,
    status_rencana_kinerja     VARCHAR(50),
    created_at                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP
)