package project.luckybooky.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.user.dto.request.UserTypeAssignRequest;
import project.luckybooky.domain.user.dto.response.UserTypeAssignResponse;
import project.luckybooky.domain.user.service.UserTypeService;
import project.luckybooky.global.apiPayload.common.BaseResponse;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@RestController
@RequestMapping("/api/user-type")
@RequiredArgsConstructor
public class UserTypeController {

    private final UserTypeService userTypeService;

    @PostMapping
    public CommonResponse<UserTypeAssignResponse> assignUserType(
            @RequestBody UserTypeAssignRequest request) {

        UserTypeAssignResponse response = userTypeService.assignCurrentUser(request);
        return CommonResponse.of(ResultCode.OK, response);
    }
}

