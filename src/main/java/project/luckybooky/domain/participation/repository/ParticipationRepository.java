package project.luckybooky.domain.participation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
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

    /**
     * 진행 중인 이벤트 조회
     **/
    @Query("SELECT p.event FROM Participation p WHERE p.user.id = :userId AND p.participateRole = :participateRole AND p.event.eventStatus IN :statuses "
            +
            "ORDER BY CASE " +
            "WHEN p.event.eventStatus = project.luckybooky.domain.event.entity.type.EventStatus.RECRUITING THEN 0" +
            " ELSE 1 END, p.event.recruitmentEnd ASC ")
    Page<Event> findByUserIdAndEventStatusesType1(@Param("userId") Long userId,
                                                  @Param("participateRole") ParticipateRole participateRole,
                                                  @Param("statuses") List<EventStatus> statuses, Pageable pageable);

    /**
     * 확정된 이벤트 조회
     **/
    @Query("SELECT p.event FROM Participation p WHERE p.user.id = :userId AND p.participateRole = :participateRole AND p.event.eventStatus IN :statuses ORDER BY p.event.eventDate")
    Page<Event> findByUserIdAndEventStatusesType2(@Param("userId") Long userId,
                                                  @Param("participateRole") ParticipateRole participateRole,
                                                  @Param("statuses") List<EventStatus> statuses, Pageable pageable);

    /**
     * 취소된 이벤트 조회
     **/
    @Query("SELECT p.event FROM Participation p WHERE p.user.id = :userId AND p.participateRole = :participateRole AND p.event.eventStatus IN :statuses ORDER BY p.event.eventDate DESC")
    Page<Event> findByUserIdAndEventStatusesType3(@Param("userId") Long userId,
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

    @EntityGraph(
            value = "Participation.withUserAndEvent",
            type = EntityGraphType.FETCH
    )
    Optional<Participation> findByUser_IdAndEvent_IdAndParticipateRole(
            Long userId,
            Long eventId,
            ParticipateRole participateRole
    );

    Optional<Participation> findFirstByEventIdAndParticipateRole(Long eventId, ParticipateRole role);

    /** 특정 날짜에 계획된 이벤트 존재 여부 조회 **/
    @Query("SELECT COUNT(p) > 0 FROM Participation p WHERE p.user.id = :id AND p.event.eventDate = :date AND p.event.eventStatus IN (" +
            "project.luckybooky.domain.event.entity.type.EventStatus.RECRUITING," +
            "project.luckybooky.domain.event.entity.type.EventStatus.RECRUIT_DONE," +
            "project.luckybooky.domain.event.entity.type.EventStatus.VENUE_RESERVATION_IN_PROGRESS," +
            "project.luckybooky.domain.event.entity.type.EventStatus.VENUE_CONFIRMED)")
    boolean existsByUserIdAndEventDate(@Param("id") Long userId, @Param("date") LocalDate date);

    boolean existsByUser_IdAndEvent_IdAndParticipateRole(Long userId, Long eventId, ParticipateRole role);

}
