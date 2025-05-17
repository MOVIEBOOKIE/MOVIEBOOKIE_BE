package project.luckybooky.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MypageResponseDTO {

    private String profileImage;
    private String username;
    private String userType;
    private String certificationEmail;
    private int hostExperienceCount;
    private int participationExperienceCount;
}