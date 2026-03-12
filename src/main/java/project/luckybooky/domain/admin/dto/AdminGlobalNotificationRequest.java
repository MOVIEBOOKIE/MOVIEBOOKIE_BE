package project.luckybooky.domain.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminGlobalNotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    @Min(1)
    @Max(1000)
    private Integer batchSize;
}
