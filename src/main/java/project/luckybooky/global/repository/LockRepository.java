package project.luckybooky.global.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LockRepository {

    private final EntityManager em;

    // 반환: true=획득 성공, false=타임아웃/실패
    public boolean getLock(String key, int timeoutSeconds) {
        Object r = em.createNativeQuery("SELECT GET_LOCK(? , ?)")
                .setParameter(1, key)
                .setParameter(2, timeoutSeconds)
                .getSingleResult();
        return r != null && ((Number) r).intValue() == 1;
    }

    // 반 쓰지환값은 굳이 않아도 됨(운영 로깅용으로 받아도 OK)
    public boolean releaseLock(String key) {
        Object r = em.createNativeQuery("SELECT RELEASE_LOCK(?)")
                .setParameter(1, key)
                .getSingleResult();
        return r != null && ((Number) r).intValue() == 1;
    }
}