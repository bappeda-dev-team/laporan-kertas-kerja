//package cc.kertaskerja.laporan.service.PerjanjianKinerja;
//
//import cc.kertaskerja.laporan.dto.LaporanPerjanjianKinerjaDTO;
//import cc.kertaskerja.laporan.entity.PerjanjianKinerja;
//import cc.kertaskerja.laporan.exception.ResourceNotFoundException;
//import cc.kertaskerja.laporan.repository.PerjanjianKinerjaRepository;
//import cc.kertaskerja.laporan.service.global.ManajemenRisikoFraudService;
//import cc.kertaskerja.laporan.service.global.RencanaKinerjaService;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.StreamSupport;
//
//@Service
//@RequiredArgsConstructor
//public class PerjanjianKinerjaServiceImpl {
//
//    private final RencanaKinerjaService rekinService;
//    private final PerjanjianKinerjaRepository pkRepository;
//    private final ManajemenRisikoFraudService manriskFraudService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    private RencanaKinerjaAtasanDTO buildRencanaKinerjaAtasanDTO(JsonNode node) {
//        return RencanaKinerjaAtasanDTO.builder()
//                .id_rencana_kinerja(node.path("id").asText())
//                .nama_rencana_kinerja(node.path("nama_rencana_kinerja").asText())
//                .tahun(node.path("tahun").asText())
//                .status_rencana_kinerja(node.path("status_rencana_kinerja").asText())
//                .catatan(node.path("catatan").asText())
//                .kode_opd(node.path("kode_opd").asText())
//                .nip(node.path("nip").asText())
//                .nama_pegawai(node.path("nama_pegawai").asText())
//                .build();
//    }
//
//    private LaporanPerjanjianKinerjaDTO buildDTOFromRkNode(JsonNode rkNode) {
//        return LaporanPerjanjianKinerjaDTO.builder()
//                .idRencanaKinerja(rkNode.path("id_rencana_kinerja").asText())
//                .namaRencanaKinerja(rkNode.path("nama_rencana_kinerja").asText())
//                .tahun(rkNode.path("tahun").asText())
//                .statusRencanaKinerja(rkNode.path("status_rencana_kinerja").asText())
//                .nip(rkNode.path("pegawai_id").asText())
//                .namaPegawai(rkNode.path("nama_pegawai").asText())
//                .operasionalDaerah(
//                        LaporanPerjanjianKinerjaDTO.OperasionalDaerahDTO.builder()
//                                .kodeOpd(rkNode.path("operasional_daerah").path("kode_opd").asText())
//                                .namaOpd(rkNode.path("operasional_daerah").path("nama_opd").asText())
//                                .build()
//                )
//                .indikator(
//                        StreamSupport.stream(rkNode.path("indikator").spliterator(), false)
//                                .map(indikatorNode ->
//                                        LaporanPerjanjianKinerjaDTO.IndikatorDTO.builder()
//                                                .idIndikator(indikatorNode.path("id_indikator").asText())
//                                                .namaIndikator(indikatorNode.path("nama_indikator").asText())
//                                                .targets(
//                                                        StreamSupport.stream(indikatorNode.path("targets").spliterator(), false)
//                                                                .map(targetNode -> LaporanPerjanjianKinerjaDTO.TargetDTO.builder()
//                                                                        .idTarget(targetNode.path("id_target").asText())
//                                                                        .indikatorId(targetNode.path("indikator_id").asText())
//                                                                        .target(targetNode.path("target").asText())
//                                                                        .satuan(targetNode.path("satuan").asText())
//                                                                        .build()
//                                                                )
//                                                                .toList()
//                                                )
//                                                .build()
//                                )
//                                .toList()
//                )
//                .build();
//    }
//
//
//    @Override
//    public LaporanPerjanjianKinerjaDTO findOnePK(String kodeOpd, String tahun) {
//        Map<String, Object> rekinDetail = rekinService.getRencanaKinerja(kodeOpd, tahun);
//        Object rkObj = rekinDetail.get("rencana_kinerja");
//
//        System.out.println("rkObj = " + rkObj);
//
//        return null;
//
////        if (rkObj == null) {
////            throw new ResourceNotFoundException("Data rencana kinerja is not found");
////        }
////
////        JsonNode rkNode = objectMapper.convertValue(rkObj, JsonNode.class);
////        return buildDTOFromRkNode(rkNode);
//    }
//
//    @Override
//    public List<RencanaKinerjaAtasanDTO> findAllRekinAtasan(String idRekin) {
//        if (idRekin == null || idRekin.isEmpty()) {
//            throw new ResourceNotFoundException("Harap masukkan id rencana kinerja");
//        }
//
//        Map<String, Object> rekinAtasan = rekinService.getAllRencanaKinerjaAtasan(idRekin);
//        Object dataObj = rekinAtasan.get("data");
//
//        if (dataObj == null) {
//            throw new ResourceNotFoundException("Data rencana kinerja atasan tidak ditemukan");
//        }
//
//        JsonNode dataNode = objectMapper.convertValue(dataObj, JsonNode.class);
//        JsonNode rekinAtasanArray = dataNode.path("rekin_atasan");
//
//        if (!rekinAtasanArray.isArray()) {
//            throw new ResourceNotFoundException("Format data rencana kinerja atasan tidak valid");
//        }
//
//        return StreamSupport.stream(rekinAtasanArray.spliterator(), false)
//                .map(this::buildRencanaKinerjaAtasanDTO)
//                .toList();
//    }
//
//    @Override
//    @Transactional
//    public PerjanjianKinerja savePK(PerjanjianKinerjaReqDTO dto) {
//        PerjanjianKinerja pk = PerjanjianKinerja.builder()
//                .namaPegawai(dto.getNama_pegawai())
//                .idRencanaKinerja(dto.getId_rencana_kinerja())
//                .namaRencanaKinerja(dto.getNama_rencana_kinerja())
//                .kodeProgram(dto.getKode_program())
//                .program(dto.getProgram())
//                .kodeKegiatan(dto.getKode_kegiatan())
//                .kegiatan(dto.getKegiatan())
//                .kodeSubKegiatan(dto.getKode_sub_kegiatan())
//                .subKegiatan(dto.getSub_kegiatan())
//                .paguAnggaran(dto.getPagu_anggaran())
//                .indikator(dto.getIndikator())
//                .target(dto.getTarget())
//                .satuan(dto.getSatuan())
//                .statusRencanaKinerja(dto.getStatus_rencana_kinerja())
//                .namaAtasan(dto.getNama_atasan())
//                .idRencanaKinerjaAtasan(dto.getId_rencana_kinerja_atasan())
//                .namaRencanaKinerjaAtasan(dto.getNama_rencana_kinerja_atasan())
//                .kodeProgramAtasan(dto.getKode_program_atasan())
//                .programAtasan(dto.getProgram_atasan())
//                .kodeKegiatanAtasan(dto.getKode_kegiatan_atasan())
//                .kegiatanAtasan(dto.getKegiatan_atasan())
//                .kodeSubKegiatanAtasan(dto.getKode_sub_kegiatan_atasan())
//                .subKegiatanAtasan(dto.getSub_kegiatan_atasan())
//                .statusRencanaKinerjaAtasan(dto.getStatus_rencana_kinerja_atasan())
//                .status("WAITING")
//                .build();
//
//        return pkRepository.save(pk);
//    }
//
////    @Override
////    @Transactional
////    public RencanaKinerjaAtasanDTO savePK(RencanaKinerjaAtasanDTO dto) {
////        PerjanjianKinerja pk = PerjanjianKinerja.builder()
////                .namaPegawai(dto.getNama_pegawai())
////                .idRencanaKinerja(dto.getId_rencana_kinerja())
////                .namaRencanaKinerja(dto.getNama_rencana_kinerja())
////                .kodeProgram(dto.getProgram)
////                .build();
////
////        PerjanjianKinerja saved = pkRepository.save(pk);
////
////        return RencanaKinerjaAtasanDTO.builder()
////                .id_rencana_kinerja(saved.getIdRencanaKinerja())
////                .nama_rencana_kinerja(saved.getNamaRencanaKinerja())
////                .tahun(dto.getTahun()) // Keep from original DTO since it's not stored in entity
////                .status_rencana_kinerja(saved.getStatusRencanaKinerja())
////                .catatan(dto.getCatatan()) // Keep from original DTO since it's not stored in entity
////                .kode_opd(dto.getKode_opd()) // Keep from original DTO since it's not stored in entity
////                .nip(dto.getNip()) // Keep from original DTO since it's not stored in entity
////                .nama_pegawai(saved.getNamaPegawai())
////                .build();
////    }
//
////    private LaporanPerjanjianKerjaDTO toDTO();
//}
