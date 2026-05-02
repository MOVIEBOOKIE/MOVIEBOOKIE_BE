package project.luckybooky.global.jwt;

import java.time.Duration;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redis;
    private static final String USER_REFRESH_KEY_PREFIX = "refreshToken:user:";
    private static final String ADMIN_REFRESH_KEY_PREFIX = "refreshToken:admin:";

    // 저장
    public void storeRefreshToken(Long userId, String token, long ttlSeconds) {
        storeToken(USER_REFRESH_KEY_PREFIX, userId, token, ttlSeconds);
    }

    // 조회
    public String getStoredRefreshToken(Long userId) {
        return getToken(USER_REFRESH_KEY_PREFIX, userId);
    }

    // 삭제
    public void deleteAllRefreshTokens(Long userId) {
        deleteToken(USER_REFRESH_KEY_PREFIX, userId);
    }

    public boolean compareAndSetRefreshToken(Long userId, String expectedToken, String newToken, long ttlSeconds) {
        return compareAndSetToken(USER_REFRESH_KEY_PREFIX, userId, expectedToken, newToken, ttlSeconds);
    }

    // ===== Admin 전용 토큰 관리 =====
    public void storeAdminRefreshToken(Long adminUserId, String token, long ttlSeconds) {
        storeToken(ADMIN_REFRESH_KEY_PREFIX, adminUserId, token, ttlSeconds);
    }

    public String getStoredAdminRefreshToken(Long adminUserId) {
        return getToken(ADMIN_REFRESH_KEY_PREFIX, adminUserId);
    }

    public void deleteAllAdminRefreshTokens(Long adminUserId) {
        deleteToken(ADMIN_REFRESH_KEY_PREFIX, adminUserId);
    }

    private void storeToken(String prefix, Long id, String token, long ttlSeconds) {
        String key = prefix + id;
        redis.opsForValue().set(key, token, Duration.ofSeconds(ttlSeconds));
    }

    private String getToken(String prefix, Long id) {
        String key = prefix + id;
        return redis.opsForValue().get(key);
    }

    private void deleteToken(String prefix, Long id) {
        String key = prefix + id;
        redis.delete(key);
    }

    private boolean compareAndSetToken(String prefix, Long id, String expected, String updated, long ttlSeconds) {
        String key = prefix + id;
        String script = """
                local current = redis.call('GET', KEYS[1])
                if current == ARGV[1] then
                    redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[3])
                    return 1
                end
                return 0
                """;
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redis.execute(
                redisScript,
                Collections.singletonList(key),
                expected,
                updated,
                String.valueOf(ttlSeconds)
        );
        return Long.valueOf(1L).equals(result);
    }
}
