package project.luckybooky.domain.participation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.entity.type.EventStatus;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.user.entity.User;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT p.event FROM Participation p WHERE p.user.id = :userId AND p.participateRole = :participateRole AND p.event.eventStatus IN :statuses " +
            "ORDER BY CASE " +
            "WHEN p.event.eventStatus IN (project.luckybooky.domain.event.entity.type.EventStatus.RECRUITING, project.luckybooky.domain.event.entity.type.EventStatus.VENUE_CONFIRMED) THEN 0" +
            " ELSE 1 END, p.event.eventDate ASC ")
    Page<Event> findByUserIdAndEventStatuses(@Param("userId") Long userId,
                                             @Param("participateRole") ParticipateRole participateRole,
                                             @Param("statuses") List<EventStatus> statuses, Pageable pageable);


    @Query("SELECT p FROM Participation p " + " WHERE p.event.id = :eventId AND p.participateRole = :role")
    List<Participation> findAllByEventIdAndRole(@Param("eventId") Long eventId, @Param("role") ParticipateRole role);

    @Query("SELECT p.user.id FROM Participation p " +
            "WHERE p.event.id = :eventId AND p.participateRole = :role")
    Optional<Long> findByUserIdAndEventIdAndRole(@Param("eventId") Long eventId,
                                                 @Param("role") ParticipateRole role);

    @Query("SELECT p.user FROM Participation p WHERE p.event.id = :eventId AND p.participateRole = 'HOST'")
    User findHostParticipationByEventId(@Param("eventId") Long eventId);

    List<Participation> findByUserId(Long userId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    List<Participation> findAllByEventIdAndParticipateRole(Long eventId, ParticipateRole role);

    // 이미 정의하신 메서드
    Optional<Participation> findByUser_IdAndEvent_IdAndParticipateRole(
            Long userId,
            Long eventId,
            ParticipateRole participateRole
    );

    Optional<Participation> findFirstByEventIdAndParticipateRole(Long eventId, ParticipateRole role);

}
