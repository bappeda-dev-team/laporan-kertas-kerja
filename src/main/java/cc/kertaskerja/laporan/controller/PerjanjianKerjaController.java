package cc.kertaskerja.laporan.controller;

import cc.kertaskerja.laporan.dto.ApiResponse;
import cc.kertaskerja.laporan.dto.LaporanPerjanjianKerjaDTO;
import cc.kertaskerja.laporan.service.PerjanjianKerja.PerjanjianKerjaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/perjanjian-kerja")
@RequiredArgsConstructor
@Tag(name = "Laporan Perjanjian Kerja")
public class PerjanjianKerjaController {

    private final PerjanjianKerjaService pkService;

    @GetMapping("/get-detail/{idRekin}")
    @Operation(summary = "Ambil satu data laporan perjanjian kerja berdasarkan ID Rencana Kinerja")
    public ResponseEntity<ApiResponse<LaporanPerjanjianKerjaDTO>> getByIdRekin(@PathVariable String idRekin) {
        LaporanPerjanjianKerjaDTO dto = pkService.findOnePK(idRekin);
        ApiResponse<LaporanPerjanjianKerjaDTO> response = ApiResponse.success(dto, "Retrieved 1 data successfully");

        return ResponseEntity.ok(response);
    }
}
