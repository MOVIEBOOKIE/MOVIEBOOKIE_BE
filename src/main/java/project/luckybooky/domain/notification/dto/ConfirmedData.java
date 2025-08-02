package project.luckybooky.domain.notification.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

/**
 * 대관 확정 이메일에 전달할 데이터 DTO
 */
@Getter
@Builder
public class ConfirmedData {
    /**
     * 영화 원제
     */
    private final String mediaTitle;
    /**
     * 이벤트(상영회) 제목
     */
    private final String eventTitle;
    /**
     * 상영 일자
     */
    private final LocalDate eventDate;
    /**
     * 상영 요일 (예: "토")
     */
    private final String eventDay;
    /**
     * 시작 시간 (예: "19:00")
     */
    private final String eventStartTime;
    /**
     * 종료 시간 (예: "21:00")
     */
    private final String eventEndTime;
    /**
     * 장소 이름
     */
    private final String locationName;
    /**
     * 최대 참여 인원
     */
    private final Integer maxParticipants;
    /**
     * 문의 연락처
     */
    private final String contact;
    /**
     * 참여자 정보 확인 링크
     */
    private final String participantsLink;

    private String hostName;

    private Long eventId;
}
