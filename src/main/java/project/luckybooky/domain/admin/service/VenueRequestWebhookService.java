package project.luckybooky.domain.admin.service;

import project.luckybooky.domain.admin.dto.VenueRequestWebhookDTO;

public interface VenueRequestWebhookService {
    void sendVenueRequest(VenueRequestWebhookDTO dto);
}
