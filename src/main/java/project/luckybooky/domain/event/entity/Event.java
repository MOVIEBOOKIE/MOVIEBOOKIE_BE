package project.luckybooky.domain.event.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.entity.BaseEntity;

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

    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Participation> participationList = new ArrayList<>();

    @Column(name = "media_title", length = 50, nullable = false)
    private String mediaTitle;

    @Column(name = "event_title", nullable = false)
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
    private HostEventButtonState hostEventButtonState = HostEventButtonState.RECRUIT_CANCELED;

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

    @Version
    private Long version;

    public void updateCurrentParticipants(Boolean isPlus) {
        if (isPlus) {
            if (currentParticipants + 1 > maxParticipants) {
                throw new BusinessException(ErrorCode.EVENT_FULL);
            }
            currentParticipants++;
        } else {
            if (currentParticipants <= 0) {
                throw new BusinessException(ErrorCode.INVALID_OPERATION);
            }
            currentParticipants--;

        }
    }

    /** 미신청자 버튼 상태 변경 **/
    public void changeAnonymousButtonState() {
        if (anonymousButtonState == AnonymousButtonState.REGISTER) {
            anonymousButtonState = AnonymousButtonState.REGISTER_DONE;
        } else {
            anonymousButtonState = AnonymousButtonState.REGISTER;
        }
    }

    /**
     * 모집 취소
     **/
    public void recruitCancel() {
        // 이벤트의 현재 상태 검증
        if (eventStatus == EventStatus.RECRUITING && hostEventButtonState == HostEventButtonState.RECRUIT_CANCELED && participantEventButtonState == ParticipantEventButtonState.REGISTER_CANCELED) {
            eventStatus = EventStatus.RECRUIT_CANCELED;
            hostEventButtonState = HostEventButtonState.RECRUIT_CANCELED;
            participantEventButtonState = ParticipantEventButtonState.RECRUIT_CANCELED;
            anonymousButtonState = AnonymousButtonState.RECRUIT_CANCELED;
        } else {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * 모집 완료
     **/
    public void recruitDone() {
        // 이벤트의 현재 상태 검증
        if (eventStatus == EventStatus.RECRUITING && hostEventButtonState == HostEventButtonState.RECRUIT_CANCELED && participantEventButtonState == ParticipantEventButtonState.REGISTER_CANCELED) {
            eventStatus = EventStatus.RECRUITED;
            hostEventButtonState = HostEventButtonState.VENUE_RESERVATION;
            participantEventButtonState = ParticipantEventButtonState.RECRUIT_DONE;
            anonymousButtonState = AnonymousButtonState.RECRUIT_DONE;
        } else {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * 대관 신청
     **/
    public void venueRegister() {
        // 이벤트의 현재 상태 검증
        if (eventStatus == EventStatus.RECRUITED && hostEventButtonState == HostEventButtonState.VENUE_RESERVATION && participantEventButtonState == ParticipantEventButtonState.RECRUIT_DONE) {
            eventStatus = EventStatus.VENUE_RESERVATION_IN_PROGRESS;
            hostEventButtonState = HostEventButtonState.VENUE_RESERVATION_IN_PROGRESS;
            participantEventButtonState = ParticipantEventButtonState.VENUE_RESERVATION_IN_PROGRESS;
        } else {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * 대관 취소
     **/
    public void venueCancel() {
        // 주최자가 임의로 취소하는 경우
        boolean canCancelByHost = eventStatus == EventStatus.RECRUITED
                            && hostEventButtonState == HostEventButtonState.VENUE_RESERVATION
                            && participantEventButtonState == ParticipantEventButtonState.RECRUIT_DONE;

        // 영화관 측에서 취소하는 경우
        boolean canCancelByTheater = eventStatus == EventStatus.VENUE_RESERVATION_IN_PROGRESS
            && hostEventButtonState == HostEventButtonState.VENUE_RESERVATION_IN_PROGRESS
            && participantEventButtonState == ParticipantEventButtonState.VENUE_RESERVATION_IN_PROGRESS;

        if (canCancelByHost || canCancelByTheater) {
            eventStatus = EventStatus.VENUE_RESERVATION_CANCELED;
            hostEventButtonState = HostEventButtonState.VENUE_RESERVATION_CANCELED;
            participantEventButtonState = ParticipantEventButtonState.VENUE_RESERVATION_CANCELED;
        } else {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * 대관 확정
     **/
    public void venueConfirmed() {
        // 이벤트의 현재 상태 검증
        if (eventStatus == EventStatus.VENUE_RESERVATION_IN_PROGRESS && hostEventButtonState == HostEventButtonState.VENUE_RESERVATION_IN_PROGRESS && participantEventButtonState == ParticipantEventButtonState.VENUE_RESERVATION_IN_PROGRESS) {
            eventStatus = EventStatus.VENUE_CONFIRMED;
            hostEventButtonState = HostEventButtonState.TO_TICKET;
            participantEventButtonState = ParticipantEventButtonState.TO_TICKET;
        } else {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * 상영 완료 혹은 취소 처리
     **/
    public void screeningProcess(Integer type) {
        // 이벤트의 현재 상태 검증
        if (eventStatus == EventStatus.VENUE_CONFIRMED && hostEventButtonState == HostEventButtonState.TO_TICKET && participantEventButtonState == ParticipantEventButtonState.TO_TICKET) {
            eventStatus = (type == 0) ? EventStatus.COMPLETED : EventStatus.CANCELLED;
        } else {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    public void resetCurrentParticipants() {
        this.currentParticipants = 0;
    }
}
