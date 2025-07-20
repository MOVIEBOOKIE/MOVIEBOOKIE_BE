package project.luckybooky.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.luckybooky.domain.notification.entity.NotificationInfo;

public interface NotificationRepository extends JpaRepository<NotificationInfo, Long> {

    List<NotificationInfo> findByUserIdOrderBySentAtDesc(Long userId);

    @Modifying
    @Query("delete from NotificationInfo n where n.sentAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("delete from NotificationInfo n where n.user.id = :userId and n.id = :id")
    int deleteByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);

    @Modifying
    @Query("update NotificationInfo n set n.isRead = true where n.user.id = :userId and n.id = :id")
    int updateReadStatus(@Param("userId") Long userId, @Param("id") Long id);

}
