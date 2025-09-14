package cc.kertaskerja.laporan.controller;

import cc.kertaskerja.laporan.dto.ApiResponse;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaAtasanReqDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaAtasanResDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaResDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.VerifikatorReqDTO;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.entity.Verifikator;
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
@Tag(name = "Laporan Perjanjian Kinerja", description = "API for laporan perjanjian kinerja")
public class PerjanjianKinerjaController {

    private final PerjanjianKinerjaService pkService;

    @GetMapping("/get-all-rekin/{kodeOpd}/{tahun}")
    @Operation(summary = "Menampilkan semua data rencana kinerja by OPD")
    public ResponseEntity<ApiResponse<List<RencanaKinerjaResDTO>>> findAllRencanaKinerja(
            @RequestHeader("X-Session-Id") String sessionId,
            @PathVariable String kodeOpd,
            @PathVariable String tahun) {
        List<RencanaKinerjaResDTO> result = pkService.findAllRencanaKinerja(sessionId, kodeOpd, tahun);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data successfully"));
    }

    @GetMapping("/rekin-atasan/{idRekin}")
    @Operation(summary = "Menampilkan semua list rencana kinerja atasan")
    public ResponseEntity<ApiResponse<List<RencanaKinerjaAtasanResDTO>>> findAllRekinAtasan(
            @RequestHeader("X-Session-Id") String sessionId,
            @PathVariable String idRekin
    ) {
        List<RencanaKinerjaAtasanResDTO> result = pkService.findAllRencanaKinerjaAtasanByIdRekinPegawai(sessionId,idRekin);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data successfully"));
    }

    @GetMapping("/rencana-kinerja/{nip}/{tahun}")
    @Operation(summary = "Menampilkan rencana kinerja detail buat dicetak")
    public ResponseEntity<ApiResponse<RencanaKinerjaResDTO>> getDetailRekin(
            @RequestHeader("X-Session-Id") String sessionId,
            @PathVariable String nip,
            @PathVariable String tahun) {
        RencanaKinerjaResDTO dto = pkService.pkRencanaKinerja(sessionId, nip, tahun);
        ApiResponse<RencanaKinerjaResDTO> response = ApiResponse.success(dto, "Retrieved 1 data successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verifikator")
    @Operation(summary = "Masukkan data atasan / verifikator")
    public ResponseEntity<ApiResponse<?>> saveVerifikator(@Valid @RequestBody VerifikatorReqDTO reqDTO,
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

        Verifikator saved = pkService.verification(reqDTO);

        return ResponseEntity.ok(ApiResponse.created(saved));
    }

    @PostMapping
    @Operation(summary = "Hubungkan rencana kinerja pegawai dengan atasan")
    public ResponseEntity<ApiResponse<?>> savePK(@Valid @RequestBody RencanaKinerjaAtasanReqDTO reqDTO,
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

        RencanaKinerjaAtasan saved = pkService.savePK(reqDTO);

        return ResponseEntity.ok(ApiResponse.created(saved));
    }
}





