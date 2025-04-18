package project.luckybooky.domain.user.converter;

import project.luckybooky.domain.user.entity.User;

public class AuthConverter {
    public static User toUser(String email, String nickname, String profileImage) {
        return User.builder()
                .email(email)
                .username(nickname)
                .profileImage(profileImage)
                .build();
    }
}