package cc.kertaskerja.laporan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rencana_kinerja_atasan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Builder
public class RencanaKinerjaAtasan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nama", length = 100, nullable = false)
    private String nama;

    @Column(name = "nip", length = 18, nullable = false)
    private String nip;

    @Column(name = "level_pegawai", nullable = false)
    private Integer levelPegawai;

    @Column(name = "id_rencana_kinerja", length = 100, nullable = false, unique = true)
    private String idRencanaKinerja;

    @Column(name = "nama_rencana_kinerja", nullable = false)
    private String namaRencanaKinerja;

    @Column(name = "id_rencana_kinerja_bawahan")
    private String idRencanaKinerjaBawahan;

    @Column(name = "nama_rencana_kinerja_bawahan", nullable = false)
    private String namaRencanaKinerjaBawahan;

    @Column(name = "nip_bawahan")
    private String nipBawahan;

    @Column(name = "nama_bawahan")
    private String namaBawahan;

    @Column(name = "kode_program", length = 100, nullable = false)
    private String kodeProgram;

    @Column(name = "program", nullable = false)
    private String program;

    @Column(name = "kode_kegiatan", length = 100, nullable = false)
    private String kodeKegiatan;

    @Column(name = "kegiatan", length = 100, nullable = false)
    private String kegiatan;

    @Column(name = "kode_sub_kegiatan", length = 100, nullable = false)
    private String kodeSubKegiatan;

    @Column(name = "sub_kegiatan", length = 100, nullable = false)
    private String subKegiatan;

    @Column(name = "pagu_anggaran")
    private Integer paguAnggaran;

    @Column(name = "indikator", nullable = false)
    private String indikator;

    @Column(name = "target", length = 20, nullable = false)
    private String target;

    @Column(name = "satuan", length = 20, nullable = false)
    private String satuan;

    @Column(name = "status_rencana_kinerja", length = 50)
    private String statusRencanaKinerja;

    @Column(name = "kode_opd")
    private String kodeOpd;

    @Column(name = "tahun")
    private Integer tahun;
}
