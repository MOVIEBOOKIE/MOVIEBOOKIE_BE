package project.luckybooky.domain.user.converter;

import project.luckybooky.domain.user.dto.response.MypageResponseDTO;
import project.luckybooky.domain.user.entity.User;

public class MypageConverter {

    public static MypageResponseDTO toDto(User user) {
        return MypageResponseDTO.builder()
                .profileImage(user.getProfileImage())
                .username(user.getUsername())
                .userType(user.getUserType() != null
                        ? user.getUserType().getLabel()
                        : null)
                .certificationEmail(user.getCertificationEmail())
                .hostExperienceCount(user.getHostExperienceCount())
                .participationExperienceCount(user.getParticipationExperienceCount())
                .build();
    }
}
