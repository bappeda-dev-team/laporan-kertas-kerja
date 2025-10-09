-- Lepas constraint UNIQUE dari id_rencana_kinerja
ALTER TABLE rencana_kinerja_atasan
DROP CONSTRAINT IF EXISTS rencana_kinerja_atasan_id_rencana_kinerja_key;

-- Ubah semua kolom dari NOT NULL menjadi NULLABLE
ALTER TABLE rencana_kinerja_atasan
ALTER COLUMN nama DROP NOT NULL,
ALTER COLUMN nip DROP NOT NULL,
ALTER COLUMN level_pegawai DROP NOT NULL,
ALTER COLUMN id_rencana_kinerja DROP NOT NULL,
ALTER COLUMN nama_rencana_kinerja DROP NOT NULL,
ALTER COLUMN id_rencana_kinerja_bawahan DROP NOT NULL,
ALTER COLUMN nip_bawahan DROP NOT NULL,
ALTER COLUMN kode_program DROP NOT NULL,
ALTER COLUMN program DROP NOT NULL,
ALTER COLUMN kode_kegiatan DROP NOT NULL,
ALTER COLUMN kegiatan DROP NOT NULL,
ALTER COLUMN kode_sub_kegiatan DROP NOT NULL,
ALTER COLUMN sub_kegiatan DROP NOT NULL,
ALTER COLUMN indikator DROP NOT NULL,
ALTER COLUMN target DROP NOT NULL,
ALTER COLUMN satuan DROP NOT NULL;
