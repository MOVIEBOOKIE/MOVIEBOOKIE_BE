package project.luckybooky.domain.participation.service;

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
     * 유효성, 만료, eventId=pathEventId 확인
     */
    // MailLinkTokenService.java (핵심 메서드만)
    public void validateOrNotFound(String token, long pathEventId) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .requireAudience(props.getAudience())
                    .build()
                    .parseClaimsJws(token);

            Claims c = jws.getBody();

            Number eventIdNum = c.get("eventId", Number.class);
            if (eventIdNum == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
            }
            long claimEventId = eventIdNum.longValue();
            if (claimEventId != pathEventId) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
            }

            if (Boolean.TRUE.equals(props.getSingleUse())) {
                String jti = c.getId();
                if (jti == null || jti.isBlank()) {
                    throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
                }
                Date exp = c.getExpiration();
                if (exp == null) {
                    throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
                }
                String redisKey = "mail-link:jti:" + jti;
                long ttl = Math.max(1L, (exp.getTime() - System.currentTimeMillis()) / 1000);
                Boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent(redisKey, "1", Duration.ofSeconds(ttl));
                if (Boolean.FALSE.equals(acquired)) {
                    throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
                }
            }
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }


}