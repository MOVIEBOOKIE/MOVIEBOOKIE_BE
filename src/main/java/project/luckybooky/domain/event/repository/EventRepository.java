package project.luckybooky.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.luckybooky.domain.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
