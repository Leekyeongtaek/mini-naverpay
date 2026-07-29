package com.minipay.point.controller;

import com.minipay.point.dto.PointChargeRequest;
import com.minipay.point.dto.PointResponse;
import com.minipay.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/charge")
    public ResponseEntity<PointResponse> chargePoint(@Valid @RequestBody PointChargeRequest request) {
        PointResponse response = pointService.chargePoint(request);
        return ResponseEntity.ok(response);
    }
}
