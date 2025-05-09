package project.luckybooky.domain.participation.converter;

import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.participation.entity.Participation;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.user.entity.User;

public class ParticipationConverter {
    public static Participation toParticipation(User user, Event event, ParticipateRole role) {
        return Participation.builder()
                .user(user)
                .event(event)
                .participateRole(role)
                .build();
    }
}
