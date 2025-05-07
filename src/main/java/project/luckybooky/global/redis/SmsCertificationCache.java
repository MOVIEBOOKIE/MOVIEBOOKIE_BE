package project.luckybooky.global.redis;

import java.time.Duration;

public interface SmsCertificationCache {
    void store(String phone, String code, Duration ttl);

    String get(String phone);

    void remove(String phone);
}
