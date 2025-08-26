package project.luckybooky.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.admin.dto.VenueRequestWebhookDTO;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class VenueRequestWebhookServiceMock implements VenueRequestWebhookService {
    
    @Override
    public void sendVenueRequest(VenueRequestWebhookDTO dto) {
        log.info("개발/로컬 환경이므로 대관 신청 웹훅을 전송하지 않습니다. 장소={}, 날짜={}", dto.getLocationName(), dto.getDate());
    }
}
