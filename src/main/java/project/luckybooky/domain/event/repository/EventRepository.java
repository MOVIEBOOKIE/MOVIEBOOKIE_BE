package project.luckybooky.domain.event.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.luckybooky.domain.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event e WHERE e.eventStatus='RECRUITING' ORDER BY e.createdAt")
    Page<Event> findOrderByCreatedAt(Pageable pageable);

    @Query("SELECT e FROM Event e JOIN e.category c WHERE c.categoryName = :categoryName AND e.eventStatus='RECRUITING' ORDER BY e.recruitmentEnd")
    Page<Event> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventStatus='RECRUITING' ORDER BY (e.currentParticipants * 1.0) / e.maxParticipants DESC")
    Page<Event> findOrderByPopularity(Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.recruitmentEnd < :now and e.eventStatus='RECRUITING'")
    List<Event> findExpiredEvent(@Param("now") LocalDate now);

    @Query("SELECT e FROM Event e \n" +
            "WHERE :content IN (e.category.categoryName) \n" +
            "   OR e.mediaTitle LIKE CONCAT('%', :content, '%')")
    Page<Event> findEventsBySearch(@Param("content") String content, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.category.id = :categoryId " +
            "AND e.eventStatus = 'RECRUITING'" +
            "ORDER BY (1.0 * e.currentParticipants / e.maxParticipants) DESC")
    List<Event> findEventListByUserType(@Param("categoryId") Long categoryId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithLock(@Param("id") Long id);

    /**
     * 시간대 겹치는 영화관 ID 목록
     **/
    @Query("SELECT e.location.id FROM Event e " +
            "WHERE e.eventDate = :date " +
            "AND (e.eventStartTime < :endTime) AND (:startTime < e.eventEndTime)")
    List<Long> findOverlappingLocationsByTime(
            @Param("date") LocalDate date,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );
}
