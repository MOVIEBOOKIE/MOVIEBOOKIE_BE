package project.luckybooky.domain.feedback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.luckybooky.domain.feedback.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
