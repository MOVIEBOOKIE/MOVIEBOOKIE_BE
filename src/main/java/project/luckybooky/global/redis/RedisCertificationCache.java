package project.luckybooky.global.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCertificationCache implements SmsCertificationCache {

    private final StringRedisTemplate redis;

    @Override
    public boolean store(String key, String code, Duration ttl) {
        Boolean ok = redis.opsForValue().setIfAbsent(key, code, ttl);       // Lettuce 6+
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public String get(String key) {
        return redis.opsForValue().get(key);
    }

    @Override
    public void remove(String key) {
        redis.delete(key);
    }
}
