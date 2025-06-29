package project.luckybooky.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserTypeResultDTO {
    private String username;
    private String userTypeCode;
    private String title;         // 카드 제목
    private String label;         // 상단 문장
    private String description;   // 카드 하단 문장
}
