package project.luckybooky.domain.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.event.entity.type.EventStatus;
import project.luckybooky.domain.event.entity.type.HostEventButtonState;
import project.luckybooky.domain.event.entity.type.ParticipantEventButtonState;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.global.entity.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Event extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Participation> participationList = new ArrayList<>();

    @Column(name = "media_title", length = 20, nullable = false)
    private String mediaTitle;

    @Column(name = "event_title", length = 20, nullable = false)
    private String eventTitle;

    @Column(name = "description")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_start_time", nullable = false)
    private String eventStartTime;

    @Column(name = "event_end_time", nullable = false)
    private String eventEndTime;

    @Column(name = "recruitment_start", nullable = false)
    private LocalDate recruitmentStart;

    @Column(name = "recruitment_end", nullable = false)
    private LocalDate recruitmentEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", length = 20)
    @Builder.Default
    private EventStatus eventStatus = EventStatus.RECRUITING;

    @Enumerated(EnumType.STRING)
    @Column(name = "host_event_button_state", length = 20)
    @Builder.Default
    private HostEventButtonState hostEventButtonState = HostEventButtonState.RECRUIT_CANCELLED;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_event_button_state", length = 20)
    @Builder.Default
    private ParticipantEventButtonState participantEventButtonState = ParticipantEventButtonState.REGISTER_CANCELED;

    @Column(name = "estimated_status", nullable = false)
    private Integer estimatedPrice;

    @Column(name = "poster_image_url", nullable = false)
    private String posterImageUrl;

    @Column(name = "min_participants", nullable = false)
    private Integer minParticipants;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Builder.Default
    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;

    public void updateCurrentParticipants(Boolean isPlus) {
        currentParticipants += isPlus ? 1 : -1;
    }
}
