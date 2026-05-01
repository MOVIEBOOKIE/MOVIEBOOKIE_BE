package project.luckybooky.global.messaging.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.luckybooky.global.messaging.entity.OutboxEvent;
import project.luckybooky.global.messaging.entity.OutboxStatus;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
            select oe
            from OutboxEvent oe
            where oe.status = :status
              and (oe.nextRetryAt is null or oe.nextRetryAt <= :now)
            order by oe.createdAt asc
            """)
    List<OutboxEvent> findPublishableEvents(
            @Param("status") OutboxStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    long countByStatus(OutboxStatus status);

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
}
