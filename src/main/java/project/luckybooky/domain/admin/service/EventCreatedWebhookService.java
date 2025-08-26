package project.luckybooky.domain.admin.service;

import project.luckybooky.domain.admin.dto.EventCreatedWebhookDTO;

public interface EventCreatedWebhookService {
    void sendEventCreated(EventCreatedWebhookDTO dto);
}
