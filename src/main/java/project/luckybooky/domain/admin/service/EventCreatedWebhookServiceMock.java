package project.luckybooky.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.admin.dto.EventCreatedWebhookDTO;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class EventCreatedWebhookServiceMock implements EventCreatedWebhookService {
    
    @Override
    public void sendEventCreated(EventCreatedWebhookDTO dto) {
        log.info("개발/로컬 환경이므로 이벤트 생성 웹훅을 전송하지 않습니다. eventTitle={}", dto.getEventTitle());
    }
}
