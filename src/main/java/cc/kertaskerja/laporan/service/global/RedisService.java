package cc.kertaskerja.laporan.service.global;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

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
}
