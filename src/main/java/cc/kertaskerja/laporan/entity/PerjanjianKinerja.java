package cc.kertaskerja.laporan.entity;

import cc.kertaskerja.laporan.common.BaseAuditable;
import cc.kertaskerja.laporan.dto.PegawaiInfo;
import cc.kertaskerja.laporan.helper.PegawaiInfoConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perjanjian_kinerja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Builder
public class PerjanjianKinerja extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========= Data Pegawai =========
    @Column(name = "nama_pegawai", length = 100, nullable = false)
    private String namaPegawai;

    @Column(name = "id_rencana_kinerja", length = 100, nullable = false, unique = true)
    private String idRencanaKinerja;

    @Column(name = "nama_rencana_kinerja", length = 255, nullable = false)
    private String namaRencanaKinerja;

    // ========= Program / Kegiatan =========
    @Column(name = "kode_program", length = 100, nullable = false)
    private String kodeProgram;

    @Column(name = "program", length = 100, nullable = false)
    private String program;

    @Column(name = "kode_kegiatan", length = 100, nullable = false)
    private String kodeKegiatan;

    @Column(name = "kegiatan", length = 100, nullable = false)
    private String kegiatan;

    @Column(name = "kode_sub_kegiatan", length = 100, nullable = false)
    private String kodeSubKegiatan;

    @Column(name = "sub_kegiatan", length = 100, nullable = false)
    private String subKegiatan;

    // ========= Anggaran & Indikator =========
    @Column(name = "pagu_anggaran")
    private Integer paguAnggaran;

    @Column(name = "indikator", length = 255, nullable = false)
    private String indikator;

    @Column(name = "target", length = 20, nullable = false)
    private String target;

    @Column(name = "satuan", length = 20, nullable = false)
    private String satuan;

    @Column(name = "status_rencana_kinerja", length = 50)
    private String statusRencanaKinerja;

    // ========= Data Atasan =========
    @Column(name = "nama_atasan", length = 100, nullable = false)
    private String namaAtasan;

    @Column(name = "id_rencana_kinerja_atasan", length = 100, nullable = false, unique = true)
    private String idRencanaKinerjaAtasan;

    @Column(name = "nama_rencana_kinerja_atasan", length = 255, nullable = false)
    private String namaRencanaKinerjaAtasan;

    @Column(name = "kode_program_atasan", length = 100, nullable = false)
    private String kodeProgramAtasan;

    @Column(name = "program_atasan", length = 50, nullable = false)
    private String programAtasan;

    @Column(name = "kode_kegiatan_atasan", length = 100, nullable = false)
    private String kodeKegiatanAtasan;

    @Column(name = "kegiatan_atasan", length = 100, nullable = false)
    private String kegiatanAtasan;

    @Column(name = "kode_sub_kegiatan_atasan", length = 100, nullable = false)
    private String kodeSubKegiatanAtasan;

    @Column(name = "sub_kegiatan_atasan", length = 100, nullable = false)
    private String subKegiatanAtasan;

    @Column(name = "pagu_anggaran_atasan")
    private Integer paguAnggaranAtasan;

    @Column(name = "indikator_atasan", length = 255, nullable = false)
    private String indikatorAtasan;

    @Column(name = "target_atasan", length = 20, nullable = false)
    private String targetAtasan;

    @Column(name = "satuan_atasan", length = 20, nullable = false)
    private String satuanAtasan;

    @Column(name = "status_rencana_kinerja_atasan", length = 50)
    private String statusRencanaKinerjaAtasan;

    // ========= Status & Verifikator =========
    @Column(name = "status", length = 50)
    private String status;

    @Convert(converter = PegawaiInfoConverter.class)
    @Column(name = "verifikator", columnDefinition = "jsonb")
    @org.hibernate.annotations.ColumnTransformer(
            read = "verifikator::text",
            write = "?::jsonb"
    )
    private PegawaiInfo verifikator;
}
