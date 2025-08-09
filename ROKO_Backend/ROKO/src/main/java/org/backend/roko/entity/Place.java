package org.backend.roko.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double latitude;
    private double longitude;

    @Column(length = 1024) // 이 줄을 추가해 주세요.
    private String photoReference;

    private float rating;
}