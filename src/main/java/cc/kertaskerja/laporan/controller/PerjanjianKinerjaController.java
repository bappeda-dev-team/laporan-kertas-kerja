package cc.kertaskerja.laporan.controller;

import cc.kertaskerja.laporan.dto.ApiResponse;
import cc.kertaskerja.laporan.dto.PegawaiInfo;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.*;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.entity.Verifikator;
import cc.kertaskerja.laporan.exception.BadRequestException;
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
@SuppressWarnings("unused")
public class PerjanjianKinerjaController {

    private final PerjanjianKinerjaService pkService;

    @GetMapping("/get-all-rekin/{kodeOpd}/{tahun}")
    @Operation(summary = "Menampilkan semua data rencana kinerja by OPD")
    public ResponseEntity<ApiResponse<List<RencanaKinerjaResDTO>>> findAllRencanaKinerja(@RequestHeader("X-Session-Id") String sessionId,
                                                                                        @PathVariable String kodeOpd,
                                                                                        @PathVariable String tahun,
                                                                                        @RequestParam(required = false) String levelPegawai) {
        List<RencanaKinerjaResDTO> result = pkService.getAllRencanaKinerjaOpd(sessionId, kodeOpd, tahun, levelPegawai);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data successfully"));
    }

    @GetMapping("/rekin-atasan/{idRekin}")
    @Operation(summary = "Menampilkan semua list rencana kinerja atasan")
    public ResponseEntity<ApiResponse<List<RencanaKinerjaAtasanResDTO>>> findAllRekinAtasan(@RequestHeader("X-Session-Id") String sessionId,
                                                                                            @PathVariable String idRekin) {
        List<RencanaKinerjaAtasanResDTO> result = pkService.findAllRencanaKinerjaAtasanByIdRekinPegawai(sessionId,idRekin);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data successfully"));
    }

//    @GetMapping("/rencana-kinerja/{nip}/{tahun}")
//    @Operation(summary = "Menampilkan rencana kinerja detail buat dicetak")
//    public ResponseEntity<ApiResponse<RencanaKinerjaResDTO>> getDetailRekin(@RequestHeader("X-Session-Id") String sessionId,
//                                                                            @PathVariable String nip,
//                                                                            @PathVariable String tahun) {
//        RencanaKinerjaResDTO dto = pkService.pkRencanaKinerja(sessionId, nip, tahun);
//        ApiResponse<RencanaKinerjaResDTO> response = ApiResponse.success(dto, "Retrieved 1 data successfully");
//
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/list-atasan/{encNip}")
    @Operation(summary = "List atasan by encrypted NIP bawahan")
    public ResponseEntity<ApiResponse<List<PegawaiInfo>>> getAllAtasanByNip(@PathVariable String encNip) {
        if (encNip == null || encNip.isBlank()) {
          throw new BadRequestException("NIP Tidak ditemukan");
        }
        List<PegawaiInfo> listAtasan = pkService.listAtasan(encNip);
        return ResponseEntity.ok(ApiResponse.success(listAtasan, "List Atasan"));
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

//    @GetMapping("/verifikator/list/{nip}")
//    @Operation(summary = "Menampilkan data atasan berdasarkan NIP pegawai")
//    public ResponseEntity<ApiResponse<List<VerifikatorResDTO>>> getAllAtasanByNIP(@PathVariable String nip) {
//        List<VerifikatorResDTO> result = pkService.findAllVerifikatorByPegawai(nip);
//
//        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved " + result.size() + " data successfully"));
//    }

    @PostMapping
    @Operation(summary = "Hubungkan rencana kinerja pegawai dengan atasan")
    public ResponseEntity<ApiResponse<?>> savePK(@RequestHeader("X-Session-Id") String sessionId,
                                                 @Valid @RequestBody RencanaKinerjaAtasanReqDTO reqDTO,
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

        RencanaKinerjaAtasan saved = pkService.savePK(sessionId, reqDTO);

        return ResponseEntity.ok(ApiResponse.created(saved));
    }

    @GetMapping("/show/{pkId}")
    @Operation(summary = "Show hubungan rekin bawahan dan atasan by id")
    public ResponseEntity<ApiResponse<?>> showPK(@PathVariable String pkId) {
        if (!pkService.existingRekinAtasan(pkId)) {
            var errResp = ApiResponse.builder()
                    .success(false)
                    .statusCode(400)
                    .message("Bad Request")
                    .errors(List.of("Rekin terhubung tidak ditemukan"))
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.badRequest().body(errResp);
        }

        RencanaKinerjaAtasan result = pkService.findById(pkId);

        return ResponseEntity.ok(ApiResponse.success(result, "Retrieved data successfully"));
    }

    @PatchMapping("/update/{pkId}")
    @Operation(summary = "Update subkegiatan dan detail rekin terhubung")
    public ResponseEntity<ApiResponse<?>> updatePK(
            @PathVariable String pkId,
            @Valid @RequestBody RencanaKinerjaAtasanReqDTO reqDTO,
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

        if (!pkService.existingRekinAtasan(pkId)) {
            var errResp = ApiResponse.builder()
                    .success(false)
                    .statusCode(400)
                    .message("Bad Request")
                    .errors(List.of("Rekin terhubung tidak ditemukan"))
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.badRequest().body(errResp);
        }

        RencanaKinerjaAtasan updated = pkService.updatePK(reqDTO, pkId);

        return ResponseEntity.ok(ApiResponse.updated(updated));
    }

    @DeleteMapping("/batal/{pkId}")
    @Operation(summary = "Batalkan hubungan rekin bawahan dan atasan by id")
    public ResponseEntity<ApiResponse<?>> deletePK(@PathVariable String pkId) {
        if (!pkService.existingRekinAtasan(pkId)) {
            var errResp = ApiResponse.builder()
                    .success(false)
                    .statusCode(400)
                    .message("Bad Request")
                    .errors(List.of("Rekin terhubung tidak ditemukan"))
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        pkService.batalkan(pkId);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Rekin Terhubung %s dibatalkan", pkId),
                "Berhasil dibatalkan"));
    }
}





