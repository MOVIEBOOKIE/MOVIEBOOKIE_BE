package project.luckybooky.domain.admin.service;

import project.luckybooky.domain.admin.dto.EventUserInfoWebhookDTO;

public interface EventUserInfoWebhookService {

    void sendEventUserInfo(EventUserInfoWebhookDTO dto);
}

