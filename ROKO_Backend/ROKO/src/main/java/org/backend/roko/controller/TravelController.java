package org.backend.roko.controller;

import org.backend.roko.dto.GeminiResponseDto;
import org.backend.roko.dto.RecommendationRequest;
import org.backend.roko.service.TravelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TravelController {

    private final TravelService travelService;

    public TravelController(TravelService travelService) {
        this.travelService = travelService;
    }

    @PostMapping("/recommendations")
    public ResponseEntity<GeminiResponseDto> recommendPlaces(@RequestBody RecommendationRequest request) {
        GeminiResponseDto response = travelService.getRecommendations(request);
        return ResponseEntity.ok(response);
    }
}