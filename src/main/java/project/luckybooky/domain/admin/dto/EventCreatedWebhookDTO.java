package project.luckybooky.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventCreatedWebhookDTO {
    private final String eventTitle;           // 이벤트 제목
    private final String mediaTitle;           // 영화/드라마 제목
    private final String date;                 // yyyy.MM.dd
    private final String time;                 // HH:mm-HH:mm
    private final String locationName;         // 장소명
    private final String hostUsername;         // 주최자 이름
    private final String hostPhoneNumber;      // 주최자 전화번호
    private final int minParticipants;         // 최소 참여 인원
    private final int maxParticipants;         // 최대 참여 인원
    private final String description;          // 이벤트 설명
    private final String categoryName;         // 카테고리명 (영화/드라마)
    private final String recruitmentPeriod;    // 모집 기간
    private final Integer estimatedPrice;      // 예상 비용
}
