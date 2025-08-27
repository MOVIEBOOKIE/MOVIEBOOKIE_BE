package project.luckybooky.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ParticipantInfo {
    private final String certificationEmail;
    private final String username;
    private final String phoneNumber;
    private final String userTypeTitle;
}
