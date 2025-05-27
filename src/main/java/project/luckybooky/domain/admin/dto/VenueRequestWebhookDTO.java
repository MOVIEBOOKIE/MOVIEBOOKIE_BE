package project.luckybooky.domain.admin.dto;// src/main/java/project/luckybooky/admin/dto/VenueRequestWebhookDTO.java

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VenueRequestWebhookDTO {
    private final String date;               // yyyy.MM.dd
    private final String time;               // HH:mm-HH:mm
    private final String locationName;
    private final String hostUsername;
    private final String hostPhoneNumber;
    private final int participantCount;
    private final String purpose;            // event.getDescription()
    private final List<ParticipantInfo> participants;
}
