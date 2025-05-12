package project.luckybooky.global.apiPayload.error.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.luckybooky.global.apiPayload.common.status.BaseStatus;
import project.luckybooky.global.apiPayload.response.ReasonDto;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseStatus {
    // Global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러입니다. 관리자에게 문의하세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "금지된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "찾을 수 없는 요청입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "허용되지 않은 메소드입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    INPUT_VALUE_INVALID(HttpStatus.BAD_REQUEST, "REQUEST_400", "요청사항에 필수 인자가 누락되었습니다"),
    HTTP_MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "G005", "request message body가 없거나, 값 타입이 올바르지 않습니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_415", "지원되지 않는 MediaType입니다."),

    // 카카오 OAuth2 관련 에러
    KAKAO_AUTH_FAILED(HttpStatus.BAD_REQUEST, "KAKAO_400", "카카오 인증 요청 중 오류가 발생했습니다."),
    KAKAO_INVALID_GRANT(HttpStatus.UNAUTHORIZED, "KAKAO_401", "유효하지 않은 카카오 인증 코드입니다."),
    KAKAO_API_ERROR(HttpStatus.BAD_REQUEST, "KAKAO_402", "카카오 API 호출 중 문제가 발생했습니다."),
    KAKAO_JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_500", "카카오 응답 JSON 파싱 중 오류가 발생했습니다."),

    // JWT 관련 에러
    JWT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JWT_500", "JWT 토큰 생성 중 오류가 발생했습니다."),
    JWT_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_401", "유효하지 않은 JWT 토큰입니다."),
    JWT_EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "JWT_402", "만료된 JWT 토큰입니다."),

    // User Error
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "USER_401", "로그인 정보가 없습니다."),
    USER_NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "USER_401", "로그인 하지 않았습니다."),
    USER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "USER_403", "권한이 없습니다."),
    PHONE_ALREADY_USED(HttpStatus.CONFLICT, "USER_409", "이미 사용 중인 전화번호입니다."),

    // S3 관련
    FILE_NOT_UPLOADED(HttpStatus.BAD_REQUEST, "S3_401", "이미지를 업로드 할 수 없습니다."),
    FILE_IS_EMPTY(HttpStatus.BAD_REQUEST, "S3_402", "파일이 비어있습니다."),
    FILE_NOT_IMAGE(HttpStatus.BAD_REQUEST, "S3_403", "이미지 파일만 업로드 가능합니다."),
    IO_EXCEPTION_ON_IMAGE_DELETE(HttpStatus.BAD_REQUEST, "S3_404", "삭제 중 에러가 발생했습니다."),
    INVALID_URL(HttpStatus.BAD_REQUEST, "S3_405", "유효하지 않은 url입니다."),

    // UserType Error
    USER_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_TYPE_404", "해당 조합의 UserType이 없습니다."),
    // UserType 관련
    USER_TYPE_NOT_ASSIGNED(HttpStatus.BAD_REQUEST, "USER_TYPE_400", "아직 유형검사를 완료하지 않았습니다."),

    // location 관련
    LOCATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "LOCATION_401", "영화관을 찾을 수 없습니다."),

    // category 관련
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "CATEGORY_401", "카테고리를 찾을 수 없습니다."),

    // event 관련
    EVENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "EVENT_401", "이벤트를 찾을 수 없습니다."),

    // Certification 관련
    CERTIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_001", "인증번호가 만료되었습니다."),
    CERTIFICATION_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_002", "인증번호가 일치하지 않습니다."),
    CERTIFICATION_DUPLICATED(HttpStatus.BAD_REQUEST, "AUTH_003", "이미 유효한 인증번호가 발송되었습니다."),


    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDto getReason(){
        return ReasonDto.builder()
                .status(httpStatus)
                .code(this.code)
                .message(this.message)
                .build();
    }

}
