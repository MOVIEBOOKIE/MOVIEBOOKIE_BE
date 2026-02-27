package project.luckybooky.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.admin.dto.EventUserInfoWebhookDTO;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"local"})
public class EventUserInfoWebhookServiceMock implements EventUserInfoWebhookService {

  @Override
  public void sendEventUserInfo(EventUserInfoWebhookDTO dto) {
    log.info("개발/로컬 환경이므로 이벤트 참가자 유저 정보 웹훅을 전송하지 않습니다. eventTitle={}", dto.getEventTitle());
  }
}

