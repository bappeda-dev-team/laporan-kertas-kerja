package cc.kertaskerja.laporan.entity;

import cc.kertaskerja.laporan.common.BaseAuditable;
import cc.kertaskerja.laporan.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status_rencana_kinerja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Builder
public class Verifikator extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kode_opd")
    private String kodeOpd;

    @Column(name = "nama_opd")
    private String namaOpd;

    @Column(name = "nama_atasan")
    private String namaAtasan;

    @Column(name = "nip")
    private String nip;

    @Column(name = "nama")
    private String nama;

    @Column(name = "nip_atasan")
    private String nipAtasan;

    @Column(name = "level_pegawai")
    private Integer levelPegawai;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private StatusEnum status;

    @Column(name = "tahun_verifikasi")
    private Integer tahunVerifikasi;
}
