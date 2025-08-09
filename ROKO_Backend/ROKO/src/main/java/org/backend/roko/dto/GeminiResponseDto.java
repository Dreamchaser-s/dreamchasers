package org.backend.roko.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;


// Gemini의 새로운 응답 형식을 담기 위한 클래스
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponseDto {
    private String itinerarySummary;
    private List<PlaceDto> places;

    // Getter와 Setter
    public String getItinerarySummary() { return itinerarySummary; }
    public void setItinerarySummary(String itinerarySummary) { this.itinerarySummary = itinerarySummary; }
    public List<PlaceDto> getPlaces() { return places; }
    public void setPlaces(List<PlaceDto> places) { this.places = places; }
}