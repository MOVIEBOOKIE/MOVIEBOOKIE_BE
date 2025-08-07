package project.luckybooky.domain.notification.dto.response;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import project.luckybooky.domain.notification.entity.NotificationInfo;

@Getter
@AllArgsConstructor
public class NotificationResponseDTO {
    private Long id;
    private String title;
    private String message;
    private String timeAgo;

    public static NotificationResponseDTO fromEntity(NotificationInfo e) {
        return new NotificationResponseDTO(
                e.getEventId(),
                e.getTitle(),
                e.getBody(),
                calculateTimeAgo(e.getSentAt())
        );
    }

    private static String calculateTimeAgo(LocalDateTime sentAt) {
        Duration d = Duration.between(sentAt, LocalDateTime.now());
        long days = d.toDays();
        if (days > 0) {
            return days + "일 전";
        }
        long hours = d.toHours();
        if (hours > 0) {
            return hours + "시간 전";
        }
        long mins = d.toMinutes();
        if (mins > 0) {
            return mins + "분 전";
        }
        return "방금 전";
    }
}
