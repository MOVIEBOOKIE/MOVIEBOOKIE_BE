package project.luckybooky.domain.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.luckybooky.domain.participation.entity.Participation;

import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findByUserIdAndEventId(Long userId, Long eventId);
    void deleteByUserIdAndEventId(Long userId, Long eventId);
}
