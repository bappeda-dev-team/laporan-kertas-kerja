package cc.kertaskerja.laporan.repository;

import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RencanaKinerjaAtasanRepository extends JpaRepository<RencanaKinerjaAtasan, Long> {
    @Query(value = "SELECT * FROM rencana_kinerja_atasan " +
          "WHERE nip_bawahan = :nipBawahan",
          nativeQuery = true)
    List<RencanaKinerjaAtasan> findByNipBawahan(@Param("nipBawahan") String nipBawahan);

}
