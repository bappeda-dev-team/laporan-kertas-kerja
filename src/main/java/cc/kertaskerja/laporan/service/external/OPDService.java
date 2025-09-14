package cc.kertaskerja.laporan.service.external;

import java.util.List;
import java.util.Map;

public interface OPDService {
    List<Map<String, Object>> findAllOPD(String sessionId);
}
