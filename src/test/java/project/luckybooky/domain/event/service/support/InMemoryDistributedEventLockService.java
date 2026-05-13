package project.luckybooky.domain.event.service.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import project.luckybooky.global.lock.DistributedEventLockService;

public class InMemoryDistributedEventLockService extends DistributedEventLockService {
  private final ConcurrentHashMap<Long, ReentrantLock> perEventLocks = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<RLock, ReentrantLock> lockBindings = new ConcurrentHashMap<>();

  public InMemoryDistributedEventLockService() {
    super(null);
  }

  @Override
  public RLock tryLockForEventRegistration(Long eventId) {
    ReentrantLock localLock = perEventLocks.computeIfAbsent(eventId, id -> new ReentrantLock());
    boolean acquired;
    try {
      acquired = localLock.tryLock(3, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }
    if (!acquired) {
      return null;
    }

    RLock rLock = Mockito.mock(RLock.class);
    lockBindings.put(rLock, localLock);
    return rLock;
  }

  @Override
  public void unlockSafely(RLock lock) {
    if (lock == null) {
      return;
    }
    ReentrantLock bound = lockBindings.remove(lock);
    if (bound != null && bound.isHeldByCurrentThread()) {
      bound.unlock();
    }
  }
}
