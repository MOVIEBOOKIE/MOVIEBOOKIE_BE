package project.luckybooky.domain.user.converter;

import project.luckybooky.domain.user.dto.response.MypageResponseDTO;
import project.luckybooky.domain.user.entity.User;

public class MypageConverter {

    public static MypageResponseDTO toDto(User user, int ticketCount) {
        String rawTitle = user.getUserType() != null
                ? user.getUserType().getTitle()
                : null;

        String typeTitle = rawTitle != null
                ? rawTitle.replace("\n", " ")
                : null;

        return MypageResponseDTO.builder()
                .profileImage(user.getProfileImage())
                .username(user.getUsername())
                .userTypeTitle(typeTitle)
                .certificationEmail(user.getCertificationEmail())
                .hostExperienceCount(user.getHostExperienceCount())
                .participationExperienceCount(user.getParticipationExperienceCount())
                .ticketCount(ticketCount)
                .build();
    }
}
