package project.luckybooky.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.luckybooky.domain.user.entity.GroupType;
import project.luckybooky.domain.user.entity.UserType;

public class UserResponseDTO {
    @Getter
    @AllArgsConstructor
    public static class JoinResultDTO {
        private String email;
        private String nickname;
        private String profileImage;
    }
    @Getter
    @AllArgsConstructor
    public static class JoinInfoResultDTO {
        private Long userId;
        private String email;
        private String nickname;
        private String profileImage;
    }

    @Getter
    @AllArgsConstructor
    public static class AllInfoDTO {
        private Long         id;
        private String       email;
        private String       username;
        private String       profileImage;
        private String       phoneNumber;
        private int          hostExperienceCount;
        private int          participationExperienceCount;
        private UserType userType;
        private GroupType groupType;
    }
}
