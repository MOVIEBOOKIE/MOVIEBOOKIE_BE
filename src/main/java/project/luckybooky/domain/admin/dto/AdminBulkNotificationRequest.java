package project.luckybooky.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminBulkNotificationRequest {

    @NotNull
    private AdminBulkNotificationTargetType targetType;

    @NotBlank
    private String title;

    @NotBlank
    private String body;
}
