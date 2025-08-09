package org.backend.roko.repository;

import org.backend.roko.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    // 'name' 필드로 데이터가 존재하는지 여부(true/false)를 반환하는 메소드
    boolean existsByName(String name);
}