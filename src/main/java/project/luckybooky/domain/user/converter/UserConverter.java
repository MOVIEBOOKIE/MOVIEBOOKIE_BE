package project.luckybooky.domain.user.converter;

import project.luckybooky.domain.user.dto.response.UserResponseDTO;
import project.luckybooky.domain.user.dto.response.UserTypeResultDTO;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.entity.UserType;


public class UserConverter {
    public static UserResponseDTO.JoinResultDTO toJoinResultDTO(User user) {
        return new UserResponseDTO.JoinResultDTO(
                user.getEmail(),
                user.getUsername(),
                user.getProfileImage(),
                user.getUserType(),
                user.getCertificationEmail(),
                user.getPhoneNumber()
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

    public static UserResponseDTO.AllInfoDTO toAllInfoDTO(User user) {
        return new UserResponseDTO.AllInfoDTO(
                user.getId(),
                user.getEmail(),
                user.getCertificationEmail(),
                user.getUsername(),
                user.getProfileImage(),
                user.getPhoneNumber(),
                user.getHostExperienceCount(),
                user.getParticipationExperienceCount(),
                user.getUserType(),
                user.getGroupType()
        );
    }

    /* 유형검사 조회 DTO */
    public static UserTypeResultDTO toResultDTO(User user) {
        UserType type = user.getUserType();

        return UserTypeResultDTO.builder()
                .username(user.getUsername())
                .userTypeCode(type.name())
                .title(type.getTitle())
                .label(type.getLabel())
                .description(type.getDescription())
                .build();
    }
}