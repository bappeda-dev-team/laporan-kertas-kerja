package cc.kertaskerja.laporan.service.global;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Duration CACHE_TTL = Duration.ofMinutes(60);

    private static final String ACCESS_TOKEN_KEY = "access_token";

    public void saveAccessToken(String token) {
        redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, token, Duration.ofSeconds(1800));
    }

    public String getAccessToken() {
        return redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
    }

    public void deleteAccessToken() {
        redisTemplate.delete(ACCESS_TOKEN_KEY);
    }

    public void saveRekinResponse(String key, String json) {
        redisTemplate.opsForValue().set(key, json, CACHE_TTL);
    }

    public String getRekinResponse(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRekinResponse(String key) {
        redisTemplate.delete(key);
    }

    public <T> void saveObject(String key, T value) {
        saveObject(key, value, CACHE_TTL);
    }

    public <T> void saveObject(String key, T value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Gagal menyimpan object ke Redis untuk key: " + key, e);
        }
    }
    public <T> List<T> getList(String key, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;

        try {
            return objectMapper.readerForListOf(clazz).readValue(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Gagal membaca list dari Redis untuk key: " + key, e);
        }
    }
}
