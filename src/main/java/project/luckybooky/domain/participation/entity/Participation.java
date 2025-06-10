package project.luckybooky.domain.participation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.user.entity.User;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "participate_role", nullable = false)
    private ParticipateRole participateRole;
}
