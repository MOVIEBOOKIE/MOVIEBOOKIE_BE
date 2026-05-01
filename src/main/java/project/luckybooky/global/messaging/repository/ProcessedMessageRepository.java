package project.luckybooky.global.messaging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.luckybooky.global.messaging.entity.ProcessedMessage;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, Long> {
}
