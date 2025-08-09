package org.backend.roko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode 등을 모두 포함
@NoArgsConstructor // 파라미터가 없는 기본 생성자 추가
@AllArgsConstructor // 모든 필드를 포함하는 생성자 추가
public class PlaceDto {
    private String name;
    private String description;
    private double latitude;
    private double longitude;
    private String photoUrl;
    private float rating;
}