package cc.kertaskerja.laporan.controller;

import cc.kertaskerja.laporan.dto.cetak.RencanaKinerjaHierarchyResponse;
import cc.kertaskerja.laporan.service.cetak.RencanaKinerjaAtasanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cetak")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class CetakController {
    private final RencanaKinerjaAtasanService service;

    @GetMapping("/perjanjian-kinerja/{encryptedNip}/{tahun}")
    public ResponseEntity<RencanaKinerjaHierarchyResponse> cetakPk(
            @RequestHeader("X-Session-Id") String sessionId,
            @PathVariable String encryptedNip,
            @PathVariable String tahun) {
        var result = service.getHierarchyByNipAndTahun(encryptedNip, tahun, sessionId);
        return ResponseEntity.ok(result);
    }
}
