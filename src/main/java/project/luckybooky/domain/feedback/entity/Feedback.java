package project.luckybooky.domain.feedback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.feedback.entity.type.NegativeFeedback;
import project.luckybooky.domain.feedback.entity.type.PositiveFeedback;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.global.entity.BaseEntity;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Feedback extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_satisfied", nullable = false)
    private Boolean isSatisfied;

    @Column(name = "comment")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "positive_feedback", nullable = false)
    private PositiveFeedback positiveFeedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "negative_feedback", nullable = false)
    private NegativeFeedback negativeFeedback;
}
