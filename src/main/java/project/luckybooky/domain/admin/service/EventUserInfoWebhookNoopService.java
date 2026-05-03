package project.luckybooky.domain.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.admin.dto.EventUserInfoWebhookDTO;

@Slf4j
@Service
@Profile("!dev & !prod")
public class EventUserInfoWebhookNoopService implements EventUserInfoWebhookService {

    @Override
    public void sendEventUserInfo(EventUserInfoWebhookDTO dto) {
        log.info("Skip event-user-info webhook in non-dev/prod profile. eventTitle={}", dto.getEventTitle());
    }
}
