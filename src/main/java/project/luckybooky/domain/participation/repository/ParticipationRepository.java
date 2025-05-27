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

    void deleteByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT p.event FROM Participation p WHERE p.user.id = :userId AND p.participateRole = :participateRole AND p.event.eventStatus IN :statuses")
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

    @Query("SELECT p.participateRole FROM Participation p WHERE p.event.id = :eventId AND p.user = :user")
    Optional<ParticipateRole> findRoleByUser(@Param("eventId") Long eventId, @Param("user") User user);

    @Query("SELECT p FROM Participation p JOIN FETCH p.user WHERE p.event.id = :eventId")
    List<Participation> findAllWithUserByEventId(@Param("eventId") Long eventId);
}
