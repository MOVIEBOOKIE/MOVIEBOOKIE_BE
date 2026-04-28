package project.luckybooky.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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

    @Operation(summary = "관리자 장소 등록", description = "신규 장소 정보와 이미지 파일을 multipart/form-data로 등록합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<LocationResponse.CreateLocationResultDTO> createLocation(
            @Parameter(description = "등록할 장소 정보 JSON", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LocationRequest.CreateLocationRequestDTO.class)))
            @Valid @RequestPart(name = "request") LocationRequest.CreateLocationRequestDTO request,
            @Parameter(
                    description = "업로드할 장소 이미지",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart(name = "locationImage") MultipartFile locationImage
    ) {
        adminContextService.getCurrentAdminUser();
        LocationResponse.CreateLocationResultDTO response = locationService.createLocation(request, locationImage);
        return CommonResponse.of(ResultCode.CREATED, response);
    }
}
