package project.luckybooky.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.luckybooky.domain.user.entity.GroupType;
import project.luckybooky.domain.user.entity.UserType;

public class UserResponseDTO {

    /* 회원가입(최초) 결과 */
    @Getter
    @AllArgsConstructor
    public static class JoinResultDTO {
        private String email;
        private String nickname;
        private String profileImage;
        private UserType userType;
        private String certificationEmail;
        private String phoneNumber;
    }

    /* 회원가입 단계에서 사용자 추가 정보 입력 완료 후 결과 */
    @Getter
    @AllArgsConstructor
    public static class JoinInfoResultDTO {
        private Long userId;
        private String email;
        private String nickname;
        private String profileImage;
    }

    /* 마이페이지 등 모든 정보 조회용 */
    @Getter
    @AllArgsConstructor
    public static class AllInfoDTO {
        private Long id;
        private String email;               // 주 이메일
        private String certificationEmail;  // 인증이 완료된 이메일
        private String username;
        private String profileImage;
        private String phoneNumber;
        private int hostExperienceCount;
        private int participationExperienceCount;
        private UserType userType;
        private GroupType groupType;
    }
}
