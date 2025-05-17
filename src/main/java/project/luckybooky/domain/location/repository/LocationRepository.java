package project.luckybooky.domain.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.luckybooky.domain.location.entity.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("SELECT l FROM Location l " +
            "WHERE (:min BETWEEN 1 AND l.seatCount OR :max BETWEEN 1 AND l.seatCount) " +
            "AND (:mediaType = '기타' OR l.availableMediaType = 'ALL') " +
            "AND (l.isStartTimeRestricted = FALSE OR " +
            "    (l.isStartTimeRestricted = TRUE AND :startTime IN (SELECT a FROM l.allowedStartTimes a))) " +
            "AND (l.availableTimes = 0 OR l.availableTimes = :progressTime)" +
            "ORDER BY l.pricePerHour")
    List<Location> findLocationsByEventOptions(
            @Param("min") int min,
            @Param("max") int max,
            @Param("mediaType") String mediaType,
            @Param("startTime") String startTime,
            @Param("progressTime") int progressTime
    );
}
