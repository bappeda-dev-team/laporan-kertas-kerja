package cc.kertaskerja.laporan.service.PerjanjianKerja;

import cc.kertaskerja.laporan.dto.LaporanPerjanjianKerjaDTO;
import cc.kertaskerja.laporan.exception.ResourceNotFoundException;
import cc.kertaskerja.laporan.service.global.ManajemenRisikoFraudService;
import cc.kertaskerja.laporan.service.global.RencanaKinerjaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PerjanjianKerjaServiceImpl implements PerjanjianKerjaService {

    private final RencanaKinerjaService rekinService;
    private final ManajemenRisikoFraudService manriskFraudService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LaporanPerjanjianKerjaDTO findOnePK(String idRekin) {
        Map<String, Object> rekinDetail = rekinService.getDetailRencanaKinerja(idRekin);
        Object rkObj = rekinDetail.get("rencana-kinerja");

        if (rkObj == null) {
            throw new ResourceNotFoundException("Data rencana kinerja is not found");
        }

        System.out.println(rkObj + "<<<<< RK");
        return null;
    }

//    private LaporanPerjanjianKerjaDTO toDTO();


}
