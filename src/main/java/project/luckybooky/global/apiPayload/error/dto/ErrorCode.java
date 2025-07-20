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
    INVALID_TOKEN_TYPE(HttpStatus.BAD_REQUEST, "AUTH_400", "잘못된 토큰 유형입니다."),
    MULTI_ENV_LOGIN(HttpStatus.UNAUTHORIZED, "AUTH_401", "다른 환경에서 로그인되어 세션이 만료되었습니다."),


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
    EVENT_FULL(HttpStatus.BAD_REQUEST, "EVENT_402", "모집 인원이 가득 찼습니다."),
    INVALID_OPERATION(HttpStatus.BAD_REQUEST, "EVENT_403", "유효하지 않은 작업입니다."),
    ALREADY_REGISTERED_EVENT(HttpStatus.BAD_REQUEST, "USER_EVENT_401", "이미 신청된 이벤트입니다."),

    // Certification 관련
    CERTIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_001", "인증번호가 만료되었습니다."),
    CERTIFICATION_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_002", "인증번호가 일치하지 않습니다."),
    CERTIFICATION_DUPLICATED(HttpStatus.BAD_REQUEST, "AUTH_003", "이미 유효한 인증번호가 발송되었습니다."),
    EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_001", "이메일 전송에 실패했습니다."),

    // Participation 관련
    PARTICIPATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "PARTICIPATION_401", "해당 이벤트를 참여하지 않았습니다."),
    PARTICIPATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PARTICIPATION_402", "해당 이벤트의 주최자가 아닙니다."),

    // Notification 관련
    NOTIFICATION_FCM_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "NOTIFICATION_400", "FCM 토큰이 등록되지 않았습니다."),
    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTIFICATION_500", "알림 전송 중 오류가 발생했습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "NOTIFICATION_401", "알림 내역을 찾을 수 없습니다."),
    NOTIFICATION_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "NOTIFICATION_402", "알림 타입을 찾을 수 없습니다."),
    NOTIFICATION_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTIFICATION_503", "알림 내역 저장에 실패했습니다."),

    // FCM 관련
    FCM_TOKEN_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTIFICATION_503", "FCM 토큰 등록에 실패했습니다."),
    FCM_INITIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTIFICATION_504", "FCM 초기화에 실패했습니다."),

    // Ticket 관련
    TICKET_NOT_FOUND(HttpStatus.BAD_REQUEST, "TICKET_401", "해당 티켓을 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "HOST_403", "접근이 거부되었습니다."),

    // Redis 관련
    SYSTEM_BUSY(HttpStatus.SERVICE_UNAVAILABLE, "SYSTEM_503", "시스템이 바쁩니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDto getReason() {
        return ReasonDto.builder()
                .status(httpStatus)
                .code(this.code)
                .message(this.message)
                .build();
    }

}
