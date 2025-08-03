package project.luckybooky.domain.participation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.user.entity.User;

@Entity
@Table(
        name = "participation",
        indexes = {
                @Index(
                        name = "idx_participation_user_event_role",
                        columnList = "user_id,event_id,participate_role"
                )
        }
)
@NamedEntityGraph(
        name = "Participation.withUserAndEvent",
        attributeNodes = {
                @NamedAttributeNode("user"),
                @NamedAttributeNode("event")
        }
)
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
