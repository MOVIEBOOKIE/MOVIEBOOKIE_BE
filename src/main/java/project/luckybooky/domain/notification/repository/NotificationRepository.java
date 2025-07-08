package project.luckybooky.domain.notification.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import project.luckybooky.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderBySentAtDesc(Long userId);

    void deleteByUserIdAndId(Long userId, Long id);


}
