package project.luckybooky.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SendVenueConfirmedMailRequestDTO {

    @Schema(description = "영화사명", example = "롯데컬처웍스")
    private String companyName;

    @Schema(description = "담당자명/연락처", example = "홍길동 010-1234-5678")
    private String contactInfo;

    @Schema(description = "계좌번호 및 기타 안내사항", example = "국민 123-456-7890-00 홍길동 / 세금계산서 발행 필요")
    private String accountAndNotes;
}


