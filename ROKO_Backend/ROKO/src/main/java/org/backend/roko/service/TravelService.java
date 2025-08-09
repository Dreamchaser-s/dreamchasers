package org.backend.roko.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlacesSearchResult;
import org.backend.roko.dto.GeminiApiDto;
import org.backend.roko.dto.GeminiResponseDto;
import org.backend.roko.dto.PlaceDto;
import org.backend.roko.dto.RecommendationRequest;
import org.backend.roko.entity.Place;
import org.backend.roko.repository.PlaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TravelService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final PlaceRepository placeRepository;
    private final GeoApiContext geoApiContext;
    private final String geminiApiKey;
    private final String googleMapsApiKey;
    private static final Logger logger = LoggerFactory.getLogger(TravelService.class);

    public TravelService(ObjectMapper objectMapper, PlaceRepository placeRepository,
                         @Value("${google.maps.api.key}") String googleMapsApiKey,
                         @Value("${gemini.api.key}") String geminiApiKey) {
        this.webClient = WebClient.create("https://generativelanguage.googleapis.com");
        this.objectMapper = objectMapper;
        this.placeRepository = placeRepository;
        this.geminiApiKey = geminiApiKey;
        this.googleMapsApiKey = googleMapsApiKey;
        this.geoApiContext = new GeoApiContext.Builder()
                .apiKey(googleMapsApiKey)
                .build();
    }

    public GeminiResponseDto getRecommendations(RecommendationRequest request) {
        try {
            String prompt = String.format("""
            [역할]
            너는 여행 정보를 간결하게 요약해주는 AI 어시스턴트야.

            [엄격한 출력 규칙]
            1. 반드시 단 하나의 JSON 객체 형식으로만 응답해야 해.
            2. 이 JSON 객체는 'itinerarySummary'와 'places' 두 개의 키를 가져야 해.
            3. 'itinerarySummary'는 여행의 핵심 테마나 컨셉을 **한 문장으로만** 요약해서 담아줘.
            4. 'places'는 추천 장소의 JSON 배열이어야 해. 각 장소 객체는 다음 키를 포함해야 해:
               - 'name' (String): 장소의 이름
               - 'description' (String): 장소에 대한 **핵심적인 한 줄 설명**
               - 'latitude' (Double): 위도
               - 'longitude' (Double): 경도
            5. JSON 배열이나 객체 외에 다른 설명, 인사, markdown(```json) 등은 절대 추가하지 마.
            6. 관련 장소를 찾지 못하면, 'itinerarySummary'는 "추천 장소를 찾지 못했어요."로, 'places'는 빈 배열 `[]`로 반환해.

            [사용자 실제 요청]
            "%s"
            """, request.getQuery());

            logger.info("Sending prompt to Gemini API for query: {}", request.getQuery());

            String jsonResponseText = callGeminiApi(prompt);
            if (jsonResponseText == null || jsonResponseText.isEmpty()) {
                return new GeminiResponseDto();
            }

            GeminiResponseDto geminiResponse = objectMapper.readValue(jsonResponseText, GeminiResponseDto.class);
            List<PlaceDto> enrichedPlaces = new ArrayList<>();

            if (geminiResponse.getPlaces() != null) {
                for (PlaceDto geminiPlace : geminiResponse.getPlaces()) {
                    PlacesSearchResult[] results = PlacesApi.textSearchQuery(geoApiContext, geminiPlace.getName()).await().results;

                    if (results != null && results.length > 0) {
                        PlacesSearchResult result = results[0];
                        String photoRef = (result.photos != null && result.photos.length > 0) ? result.photos[0].photoReference : null;

                        String photoUrl = null;
                        if (photoRef != null) {
                            // 👇 Markdown 링크 형식을 제거하고 올바른 URL로 수정했습니다.
                            photoUrl = "https://maps.googleapis.com/maps/api/place/photo"
                                    + "?maxwidth=800"
                                    + "&photoreference=" + photoRef
                                    + "&key=" + this.googleMapsApiKey;
                        }
                        logger.info("### 백엔드가 생성한 최종 사진 URL: {}", photoUrl);

                        PlaceDto enrichedPlace = new PlaceDto(
                                result.name,
                                geminiPlace.getDescription(),
                                result.geometry.location.lat,
                                result.geometry.location.lng,
                                photoUrl,
                                result.rating
                        );
                        enrichedPlaces.add(enrichedPlace);

                        if (!placeRepository.existsByName(result.name)) {
                            Place placeToSave = new Place();
                            placeToSave.setName(enrichedPlace.getName());
                            placeToSave.setDescription(enrichedPlace.getDescription());
                            placeToSave.setLatitude(enrichedPlace.getLatitude());
                            placeToSave.setLongitude(enrichedPlace.getLongitude());
                            placeToSave.setPhotoReference(photoRef);
                            placeToSave.setRating(enrichedPlace.getRating());
                            placeRepository.save(placeToSave);
                        }
                    }
                }
            }

            geminiResponse.setPlaces(enrichedPlaces);
            logger.info("Successfully found {} places for query: {}", enrichedPlaces.size(), request.getQuery());
            return geminiResponse;

        } catch (Exception e) {
            logger.error("Error during getRecommendations for query: " + request.getQuery(), e);
            return new GeminiResponseDto();
        }
    }

    private String callGeminiApi(String prompt) {
        try {
            GeminiApiDto.Request geminiRequest = new GeminiApiDto.Request(prompt);
            GeminiApiDto.Response geminiResponse = this.webClient.post()
                    .uri("/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + this.geminiApiKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(geminiRequest)
                    .retrieve()
                    .bodyToMono(GeminiApiDto.Response.class)
                    .block();

            if (geminiResponse == null || geminiResponse.getCandidates() == null || geminiResponse.getCandidates().isEmpty()) {
                logger.warn("Received an empty or invalid response from Gemini API.");
                return "{}";
            }

            String jsonText = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();
            logger.info("Raw response from Gemini: {}", jsonText);

            if (jsonText.startsWith("```json")) {
                return jsonText.substring(7, jsonText.length() - 3).trim();
            }
            return jsonText;
        } catch (Exception e) {
            logger.error("Error calling Gemini API", e);
            return null;
        }
    }

    public List<Place> findAllPlaces() {
        return placeRepository.findAll();
    }
}