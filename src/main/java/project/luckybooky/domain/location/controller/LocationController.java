package project.luckybooky.domain.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import project.luckybooky.domain.location.dto.request.LocationRequest;
import project.luckybooky.domain.location.dto.response.LocationResponse;
import project.luckybooky.domain.location.service.LocationService;
import project.luckybooky.global.apiPayload.common.BaseResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService locationService;

    @Operation(summary = "이벤트 생성 시 영화관 목록 추천", description = "이벤트에 대한 정보를 Request Body에 넣어주세요!! ")
    @PostMapping
    public BaseResponse<List<LocationResponse.ReadLocationsResultDTO>> findLocationsByEventOptions(@RequestBody LocationRequest.ReadLocationsRequestDTO request) {
        return BaseResponse.onSuccess(locationService.findLocationsByEventOptions(request));
    }
}
