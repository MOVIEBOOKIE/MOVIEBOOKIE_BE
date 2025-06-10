package project.luckybooky.global.oauth.handler;

import lombok.Getter;
import project.luckybooky.global.apiPayload.common.BaseResponse;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;

@Getter
public class AuthFailureHandler extends RuntimeException {
    private final ErrorCode errorCode;

    public AuthFailureHandler(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BaseResponse<String> toResponse() {
        return new BaseResponse<>(false, errorCode.getCode(), errorCode.getMessage());
    }
}
