package project.luckybooky.domain.admin.service;

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
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.global.messaging.service.NotificationOutboxProducer;

@Service
@RequiredArgsConstructor
public class AdminBulkNotificationService {

    private final EventService eventService;
    private final ParticipationRepository participationRepository;
    private final NotificationOutboxProducer notificationOutboxProducer;

    @Transactional
    public AdminBulkNotificationResponse sendBulkNotification(Long eventId, AdminBulkNotificationRequest request) {
        // 이벤트 존재 검증
        eventService.findOne(eventId);

        List<User> targets = resolveTargets(eventId, request.getTargetType());
        int pushSentCount = 0;
        int pushSkippedCount = 0;

        for (User user : targets) {
            if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
                pushSkippedCount++;
            } else {
                notificationOutboxProducer.enqueueDirectNotification(
                        user.getId(),
                        request.getTitle(),
                        request.getBody(),
                        eventId
                );
                pushSentCount++;
            }
        }

        return AdminBulkNotificationResponse.builder()
                .eventId(eventId)
                .targetType(request.getTargetType())
                .targetCount(targets.size())
                .pushSentCount(pushSentCount)
                .pushSkippedCount(pushSkippedCount)
                .savedCount(pushSentCount)
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
