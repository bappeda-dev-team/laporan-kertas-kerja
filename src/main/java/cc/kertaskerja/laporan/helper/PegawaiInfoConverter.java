package cc.kertaskerja.laporan.helper;

import cc.kertaskerja.laporan.dto.PegawaiInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PegawaiInfoConverter implements AttributeConverter<PegawaiInfo, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(PegawaiInfo pegawaiInfo) {
        if (pegawaiInfo == null) return null;
        try {
            return objectMapper.writeValueAsString(pegawaiInfo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert PegawaiInfo to JSON", e);
        }
    }

    @Override
    public PegawaiInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) return null;
        try {
            return objectMapper.readValue(dbData, PegawaiInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to PegawaiInfo", e);
        }
    }
}
