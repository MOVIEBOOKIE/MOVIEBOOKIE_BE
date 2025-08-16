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
            long claimEventId = c.get("eventId", Number.class).longValue();

            if (claimEventId != pathEventId) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
            }

            if (Boolean.TRUE.equals(props.getSingleUse())) {
                String jti = c.getId();
                String redisKey = "mail-link:jti:" + jti;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                    throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
                }
                long ttl = Math.max(1L, (c.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
                redisTemplate.opsForValue().set(redisKey, "1", Duration.ofSeconds(ttl));
            }
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }


}