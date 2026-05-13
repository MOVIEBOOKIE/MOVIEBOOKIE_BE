package project.luckybooky.global.redis;

import java.time.Duration;

public interface SmsCertificationCache {
    boolean store(String phone, String code, Duration ttl);

    void put(String key, String value, Duration ttl);

    String get(String phone);

    void remove(String phone);
}
