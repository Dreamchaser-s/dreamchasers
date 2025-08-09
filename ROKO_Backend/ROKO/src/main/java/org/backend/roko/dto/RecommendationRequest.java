package org.backend.roko.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendationRequest {
    private String query; // "강릉 근처 맛집" 같은 검색어
}