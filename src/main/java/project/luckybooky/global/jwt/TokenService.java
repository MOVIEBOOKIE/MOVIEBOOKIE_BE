package project.luckybooky.global.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
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
}
