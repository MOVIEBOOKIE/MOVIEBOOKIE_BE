package project.luckybooky.domain.user.converter;

import project.luckybooky.domain.user.dto.response.UserResponseDTO;
import project.luckybooky.domain.user.entity.User;


public class UserConverter {
    public static UserResponseDTO.JoinResultDTO toJoinResultDTO(User user) {
        return new UserResponseDTO.JoinResultDTO(
                user.getEmail(),
                user.getUsername(),
                user.getProfileImage()
        );
    }

    public static UserResponseDTO.JoinInfoResultDTO toJoinInfoResultDTO(User user) {
        return new UserResponseDTO.JoinInfoResultDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getProfileImage()
        );
    }
}