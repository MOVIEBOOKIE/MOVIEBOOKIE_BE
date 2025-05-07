package project.luckybooky.global.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSmsCertificationCache implements SmsCertificationCache {

    private final StringRedisTemplate redis;

    @Override
    public void store(String phone, String code, Duration ttl) {
        redis.opsForValue().set(phone, code, ttl);
    }

    @Override
    public String get(String phone) {
        return redis.opsForValue().get(phone);
    }

    @Override
    public void remove(String phone) {
        redis.delete(phone);
    }
}
