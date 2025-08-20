package cc.kertaskerja.laporan.controller;

import cc.kertaskerja.laporan.dto.ApiResponse;
import cc.kertaskerja.laporan.dto.LaporanPerjanjianKinerjaDTO;
import cc.kertaskerja.laporan.dto.PerjanjianKinerjaReqDTO;
import cc.kertaskerja.laporan.dto.RencanaKinerjaAtasanDTO;
import cc.kertaskerja.laporan.entity.PerjanjianKinerja;
import cc.kertaskerja.laporan.service.PerjanjianKinerja.PerjanjianKinerjaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/perjanjian-kinerja")
@RequiredArgsConstructor
@Tag(name = "Laporan Perjanjian Kinerja")
public class PerjanjianKinerjaController {

    private final PerjanjianKinerjaService pkService;

    @GetMapping("/get-detail/{kodeOpd}/{tahun}")
    @Operation(summary = "Ambil satu data laporan perjanjian kerja berdasarkan ID Rencana Kinerja")
    public ResponseEntity<ApiResponse<LaporanPerjanjianKinerjaDTO>> getByIdRekin(@PathVariable String kodeOpd,
                                                                               @PathVariable String tahun) {
        LaporanPerjanjianKinerjaDTO dto = pkService.findOnePK(kodeOpd, tahun);
        ApiResponse<LaporanPerjanjianKinerjaDTO> response = ApiResponse.success(dto, "Retrieved 1 data successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rencana-kinerja-atasan/{idRekin}")
    @Operation(summary = "Menampilkan semua data rencana kinerja atasan berdasarkan id rencana kinerja pegawai")
    public ResponseEntity<ApiResponse<List<RencanaKinerjaAtasanDTO>>> findAllRekinAtasan(@PathVariable String idRekin) {
        List<RencanaKinerjaAtasanDTO> result = pkService.findAllRekinAtasan(idRekin);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data successfully"));
    }

    @PostMapping
    @Operation(summary = "Hubungkan rencana kinerja pegawai dengan atasan")
    public ResponseEntity<ApiResponse<?>> savePK(@Valid @RequestBody PerjanjianKinerjaReqDTO reqDTO,
                                                 BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList();

            ApiResponse<List<String>> errorResponse = ApiResponse.<List<String>>builder()
                    .success(false)
                    .statusCode(400)
                    .message("Validation failed")
                    .errors(errorMessages)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }

        PerjanjianKinerja saved = pkService.savePK(reqDTO);

        return ResponseEntity.ok(ApiResponse.created(saved));
    }
}





