package project.luckybooky.domain.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.luckybooky.domain.location.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
