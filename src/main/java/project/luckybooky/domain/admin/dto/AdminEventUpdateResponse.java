package project.luckybooky.domain.admin.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminEventUpdateResponse {
    private final Long eventId;
    private final String eventTitle;
    private final String description;
    private final LocalDate eventDate;
    private final String eventStartTime;
    private final String eventEndTime;
    private final Long locationId;
    private final Integer minParticipants;
    private final Integer maxParticipants;
    private final String posterImageUrl;
    private final Integer estimatedPrice;
}
