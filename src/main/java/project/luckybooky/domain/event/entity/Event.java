package project.luckybooky.domain.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.event.entity.type.AnonymousButtonState;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "anonymous_event_button_state", length = 20)
    @Builder.Default
    private AnonymousButtonState anonymousButtonState = AnonymousButtonState.REGISTER;

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

    /** 모집 취소 **/
    public void recruitCancel() {
        eventStatus = EventStatus.RECRUIT_CANCELED;
        hostEventButtonState = HostEventButtonState.RECRUIT_CANCELLED;
        participantEventButtonState = ParticipantEventButtonState.RECRUIT_CANCELED;
    }

    /** 모집 완료 **/
    public void recruitDone() {
        eventStatus = EventStatus.RECRUITED;
        hostEventButtonState = HostEventButtonState.VENUE_RESERVATION;
        participantEventButtonState = ParticipantEventButtonState.RECRUIT_DONE;
    }

    /** 대관 신청 **/
    public void venueRegister() {
        eventStatus = EventStatus.VENUE_RESERVATION_IN_PROGRESS;
        hostEventButtonState = HostEventButtonState.VENUE_RESERVATION_IN_PROGRESS;
        participantEventButtonState = ParticipantEventButtonState.VENUE_RESERVATION_IN_PROGRESS;
    }

    /** 대관 취소 **/
    public void venueCancel() {
        eventStatus = EventStatus.VENUE_RESERVATION_CANCELED;
        hostEventButtonState = HostEventButtonState.VENUE_RESERVATION_CANCELED;
        participantEventButtonState = ParticipantEventButtonState.VENUE_RESERVATION_CANCELED;
    }

    /** 대관 확정 **/
    public void venueConfirmed() {
        eventStatus = EventStatus.VENUE_CONFIRMED;
        hostEventButtonState = HostEventButtonState.TO_TICKET;
        participantEventButtonState = ParticipantEventButtonState.TO_TICKET;
    }

    /**
     * 상영 완료 혹은 취소 처리
     **/
    public void screeningProcess(Integer type) {
        eventStatus = (type == 0) ? EventStatus.COMPLETED : EventStatus.CANCELLED;
    }
}
