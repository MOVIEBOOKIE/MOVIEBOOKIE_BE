package project.luckybooky.domain.admin.dto;

import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminEventUpdateRequest {
    private String eventTitle;
    private String description;
    private LocalDate eventDate;
    private String eventStartTime;
    private Integer eventProgressTime;
    private Long locationId;
    @Min(value = 1, message = "최소 참여 인원은 1명 이상이어야 합니다.")
    private Integer minParticipants;
    @Min(value = 1, message = "최대 참여 인원은 1명 이상이어야 합니다.")
    private Integer maxParticipants;
    private String posterImageUrl;
}
