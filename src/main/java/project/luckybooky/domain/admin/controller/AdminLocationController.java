package project.luckybooky.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.adminUser.service.AdminContextService;
import project.luckybooky.domain.location.dto.request.LocationRequest;
import project.luckybooky.domain.location.dto.response.LocationResponse;
import project.luckybooky.domain.location.service.LocationService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "Admin Location", description = "관리자용 장소 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/locations")
public class AdminLocationController {

    private final AdminContextService adminContextService;
    private final LocationService locationService;

    @Operation(summary = "관리자 장소 등록", description = "신규 장소 정보를 등록합니다.")
    @PostMapping
    public CommonResponse<LocationResponse.CreateLocationResultDTO> createLocation(
            @Valid @RequestBody LocationRequest.CreateLocationRequestDTO request
    ) {
        adminContextService.getCurrentAdminUser();
        LocationResponse.CreateLocationResultDTO response = locationService.createLocation(request);
        return CommonResponse.of(ResultCode.CREATED, response);
    }
}
