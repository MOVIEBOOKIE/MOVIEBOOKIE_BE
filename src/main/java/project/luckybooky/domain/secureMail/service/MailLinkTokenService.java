package project.luckybooky.domain.secureMail.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.participation.service.LinkCryptoService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailLinkTokenService {

    private final LinkCryptoService crypto;
    private final StringRedisTemplate redis;

    private final String audience = "mail-link";
    private final long ttlSeconds = 86400L;
    private final boolean singleUse = true;
    private final boolean bindPath = true;

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
            payload.put("path", path); // 경로 바인딩
        }

        return crypto.encryptPayload(payload);
    }

    /**
     * 복호화 → 만료/청중/경로/이벤트 검증 → (옵션) 1회용 소모
     */
    public void validateOrThrow(String token, long pathEventId, String actualPath) {
        Map<String, Object> p;
        try {
            p = crypto.decryptPayload(token);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        long now = LinkCryptoService.nowEpoch();

        // 필수 필드
        Object exp = p.get("exp");
        Object aud = p.get("aud");
        Object eventId = p.get("eventId");
        if (exp == null || aud == null || eventId == null) {
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

        if (singleUse) {
            String jti = String.valueOf(p.get("jti"));
            if (jti == null || jti.isBlank()) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            String redisKey = "mail-link:jti:" + jti;
            long remain = Math.max(1L, expSec - now);
            Boolean ok = redis.opsForValue().setIfAbsent(redisKey, "1", Duration.ofSeconds(remain));
            if (!Boolean.TRUE.equals(ok)) {
                throw new BusinessException(ErrorCode.FORBIDDEN); // 재사용 방지
            }
        }
    }
}
