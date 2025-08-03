package project.luckybooky.domain.participation.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.participation.dto.ParticipantInfoDto;
import project.luckybooky.domain.participation.entity.type.ParticipateRole;
import project.luckybooky.domain.participation.repository.ParticipationRepository;

@Service
@RequiredArgsConstructor
public class ParticipantInfoService {
    private final ParticipationRepository participationRepository;

    @Transactional(readOnly = true)
    public List<ParticipantInfoDto> getParticipantInfo(Long eventId) {
        return participationRepository
                .findAllByEventIdAndRole(eventId, ParticipateRole.PARTICIPANT)
                .stream()
                .map(p -> new ParticipantInfoDto(
                        p.getUser().getUsername(),
                        p.getUser().getPhoneNumber()
                ))
                .collect(Collectors.toList());
    }
}
