package project.luckybooky.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.oauth.handler.AuthFailureHandler;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .claim("category", "access")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidity))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String createRefreshToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .claim("category", "refresh")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidity))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new AuthFailureHandler(ErrorCode.JWT_EXPIRED_TOKEN);
        } catch (JwtException e) {
            throw new AuthFailureHandler(ErrorCode.JWT_INVALID_TOKEN);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractCategory(String token) {
        return parseClaims(token).get("category", String.class);
    }

    public long getRemainingSeconds(String token) {
        Date exp = parseClaims(token).getExpiration();
        return (exp.getTime() - System.currentTimeMillis()) / 1000;
    }

    public int getAccessTokenValidity() {
        return (int) accessTokenValidity;
    }

    public int getRefreshTokenValidity() {
        return (int) refreshTokenValidity;
    }
}
