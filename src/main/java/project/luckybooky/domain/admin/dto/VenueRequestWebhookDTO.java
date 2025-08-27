package project.luckybooky.domain.admin.dto;// src/main/java/project/luckybooky/admin/dto/VenueRequestWebhookDTO.java

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class VenueRequestWebhookDTO {
    private final String date;
    private final String time;
    private final String locationName;
    private final String hostUsername;
    private final String hostPhoneNumber;
    private final int participantCount;
    private final String purpose;
    private final List<ParticipantInfo> participants;
}
