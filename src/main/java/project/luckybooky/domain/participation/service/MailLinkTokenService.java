package project.luckybooky.domain.notification.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.participation.util.MailLinkProps;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailLinkTokenService {

    private final MailLinkProps props;
    private final StringRedisTemplate redisTemplate;

    private Key key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes());
    }

    public String issueForEvent(Long eventId) {
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getTtlSeconds());

        return Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(eventId))
                .setAudience(props.getAudience())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("eventId", eventId)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 유효성, 만료, eventId=pathEventId 확인. single-use=true면 jti를 즉시 소모 처리.
     */
    public void validateOrThrow(String token, Long pathEventId) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .requireAudience(props.getAudience())
                    .build()
                    .parseClaimsJws(token);

            Claims c = jws.getBody();
            Long claimEventId = c.get("eventId", Number.class).longValue();
            if (!claimEventId.equals(pathEventId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN); // 이벤트 불일치
            }

            if (Boolean.TRUE.equals(props.getSingleUse())) {
                String jti = c.getId();
                String redisKey = "mail-link:jti:" + jti;
                Boolean alreadyUsed = redisTemplate.hasKey(redisKey);
                if (Boolean.TRUE.equals(alreadyUsed)) {
                    throw new BusinessException(ErrorCode.FORBIDDEN); // 재사용 금지
                }
                long ttl = Math.max(1L,
                        (c.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
                redisTemplate.opsForValue().set(redisKey, "1", Duration.ofSeconds(ttl));
            }
        } catch (JwtException e) {
            log.debug("Mail link token invalid: {}", e.toString());
            throw new BusinessException(ErrorCode.UNAUTHORIZED); // 토큰 오류/만료
        }
    }
}