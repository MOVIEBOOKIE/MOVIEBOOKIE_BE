package project.luckybooky.domain.admin.service;

import com.google.firebase.messaging.Message;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.admin.dto.AdminBulkNotificationRequest;
import project.luckybooky.domain.admin.dto.AdminBulkNotificationResponse;
import project.luckybooky.domain.admin.dto.AdminBulkNotificationTargetType;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.domain.notification.converter.NotificationConverter;
import project.luckybooky.domain.notification.entity.NotificationInfo;
import project.luckybooky.domain.notification.repository.NotificationRepository;
import project.luckybooky.domain.notification.service.NotificationService;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class AdminBulkNotificationService {

    private final EventService eventService;
    private final ParticipationRepository participationRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Transactional
    public AdminBulkNotificationResponse sendBulkNotification(Long eventId, AdminBulkNotificationRequest request) {
        // 이벤트 존재 검증
        eventService.findOne(eventId);

        List<User> targets = resolveTargets(eventId, request.getTargetType());
        int pushSentCount = 0;
        int pushSkippedCount = 0;
        List<NotificationInfo> infos = new ArrayList<>();

        for (User user : targets) {
            Message message = NotificationConverter.toMessage(user, request.getTitle(), request.getBody());
            if (message == null) {
                pushSkippedCount++;
            } else {
                notificationService.send(message);
                pushSentCount++;
            }

            infos.add(NotificationInfo.builder()
                    .user(user)
                    .title(request.getTitle())
                    .body(request.getBody())
                    .eventId(eventId)
                    .sentAt(LocalDateTime.now())
                    .isRead(false)
                    .build());
        }

        if (!infos.isEmpty()) {
            notificationRepository.saveAll(infos);
        }

        return AdminBulkNotificationResponse.builder()
                .eventId(eventId)
                .targetType(request.getTargetType())
                .targetCount(targets.size())
                .pushSentCount(pushSentCount)
                .pushSkippedCount(pushSkippedCount)
                .savedCount(infos.size())
                .build();
    }

    private List<User> resolveTargets(Long eventId, AdminBulkNotificationTargetType targetType) {
        return switch (targetType) {
            case HOST -> participationRepository
                    .findFirstByEventIdAndParticipateRoleWithUser(eventId, ParticipateRole.HOST)
                    .map(participation -> List.of(participation.getUser()))
                    .orElse(List.of());
            case PARTICIPANTS -> participationRepository
                    .findAllByEventIdAndParticipateRoleWithUser(eventId, ParticipateRole.PARTICIPANT)
                    .stream()
                    .map(Participation::getUser)
                    .toList();
            case ALL -> resolveAllTargets(eventId);
        };
    }

    private List<User> resolveAllTargets(Long eventId) {
        LinkedHashMap<Long, User> uniqueUsers = new LinkedHashMap<>();

        participationRepository.findFirstByEventIdAndParticipateRoleWithUser(eventId, ParticipateRole.HOST)
                .map(Participation::getUser)
                .ifPresent(user -> uniqueUsers.put(user.getId(), user));

        participationRepository.findAllByEventIdAndParticipateRoleWithUser(eventId, ParticipateRole.PARTICIPANT)
                .stream()
                .map(Participation::getUser)
                .forEach(user -> uniqueUsers.put(user.getId(), user));

        return new ArrayList<>(uniqueUsers.values());
    }
}
