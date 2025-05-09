package project.luckybooky.domain.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.luckybooky.domain.participation.entity.Participation;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
}
