package project.luckybooky.domain.admin.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminEventListItemResponse {
    private final Long eventId;
    private final String eventTitle;
    private final LocalDate eventDate;
    private final String eventStatus;
    private final Long locationId;
    private final String locationName;
    private final String hostName;
    private final Integer currentParticipants;
    private final Integer maxParticipants;
    private final Integer recruitmentRate;
}
