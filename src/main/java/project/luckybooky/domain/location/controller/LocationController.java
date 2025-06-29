package project.luckybooky.domain.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.location.dto.request.LocationRequest;
import project.luckybooky.domain.location.dto.response.LocationResponse;
import project.luckybooky.domain.location.service.LocationService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService locationService;

    @Operation(summary = "이벤트 생성 시 영화관 목록 추천", description = "이벤트에 대한 정보를 Request Body에 넣어주세요!!")
    @PostMapping
    public CommonResponse<List<LocationResponse.ReadLocationsResultDTO>> findLocationsByEventOptions(
            @RequestBody LocationRequest.ReadLocationsRequestDTO request
    ) {
        List<LocationResponse.ReadLocationsResultDTO> locations =
                locationService.findLocationsByEventOptions(request);
        return CommonResponse.of(ResultCode.OK, locations);
    }
}
