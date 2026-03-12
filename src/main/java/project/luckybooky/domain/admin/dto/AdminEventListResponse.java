package project.luckybooky.domain.admin.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminEventListResponse {
    private final Integer page;
    private final Integer size;
    private final Integer totalPages;
    private final Long totalElements;
    private final List<AdminEventListItemResponse> events;
}
