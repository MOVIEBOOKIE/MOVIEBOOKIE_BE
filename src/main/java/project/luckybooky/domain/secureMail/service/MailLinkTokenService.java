package project.luckybooky.domain.secureMail.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.participation.service.LinkCryptoService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class MailLinkTokenService {

    private final LinkCryptoService crypto;
    private final StringRedisTemplate redis;

    private final String audience = "mail-link";
    private final long ttlSeconds = 86400L;
    private final boolean singleUse = true;
    private final boolean bindPath = true;
    private final boolean enforceLatestOnly = true;

    private String latestKey(long eventId) {
        return "mail-link:latest:" + eventId;
    }

    private String usedKey(String jti) {
        return "mail-link:jti:" + jti;
    }

    public String issueForEvent(Long eventId, String path) {
        String jti = UUID.randomUUID().toString();
        long now = LinkCryptoService.nowEpoch();
        long exp = now + ttlSeconds;

        Map<String, Object> payload = new HashMap<>();
        payload.put("jti", jti);
        payload.put("aud", audience);
        payload.put("iat", now);
        payload.put("exp", exp);
        payload.put("eventId", eventId);
        if (bindPath) {
            payload.put("path", path);
        }

        String token = crypto.encryptPayload(payload);

        if (enforceLatestOnly) {
            redis.opsForValue().set(latestKey(eventId), jti, Duration.ofSeconds(ttlSeconds));
        }
        return token;
    }

    /**
     * 복호화 → 만료/경로/이벤트 검증 → (선택) 최신 jti 일치/1회용 소모
     */
    public void validateOrThrow(String token, long pathEventId, String actualPath) {
        Map<String, Object> p;
        try {
            p = crypto.decryptPayload(token);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        long now = LinkCryptoService.nowEpoch();

        Object exp = p.get("exp");
        Object aud = p.get("aud");
        Object eventId = p.get("eventId");
        Object jtiObj = p.get("jti");

        if (exp == null || aud == null || eventId == null || jtiObj == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!audience.equals(aud.toString())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        long expSec = ((Number) exp).longValue();
        if (now > expSec) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED); // 만료
        }
        long claimEventId = ((Number) eventId).longValue();
        if (claimEventId != pathEventId) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (bindPath) {
            Object path = p.get("path");
            if (path == null || !path.toString().equals(actualPath)) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        String jti = jtiObj.toString();

        // ★ 최신 토큰만 허용
        if (enforceLatestOnly) {
            String latest = redis.opsForValue().get(latestKey(pathEventId));
            if (latest == null || !latest.equals(jti)) {
                throw new BusinessException(ErrorCode.FORBIDDEN); // 더 최신 토큰이 있음
            }
        }

        // (선택) 1회용 소모 처리
        if (singleUse) {
            String k = usedKey(jti);
            long remain = Math.max(1L, expSec - now);
            Boolean ok = redis.opsForValue().setIfAbsent(k, "1", Duration.ofSeconds(remain));
            if (!Boolean.TRUE.equals(ok)) {
                throw new BusinessException(ErrorCode.FORBIDDEN); // 재사용 금지
            }
        }
    }

    public String newLink(Long eventId, String baseUrl, String path) {
        String et = issueForEvent(eventId, path);
        String base = (baseUrl != null && baseUrl.endsWith("/")) ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + path + "?et=" + et;
    }
}
