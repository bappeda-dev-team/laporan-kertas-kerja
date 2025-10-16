package cc.kertaskerja.laporan.repository;

import cc.kertaskerja.laporan.entity.Verifikator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VerifikatorRepository extends JpaRepository<Verifikator, Long> {
    List<Verifikator> findByNipAndTahunVerifikasi(String nip, String tahunVerifikasi);

    @Query("SELECT v FROM Verifikator v WHERE v.nip = :nip")
    List<Verifikator> findVerifikatorByNip(@Param("nip") String nip);

}
