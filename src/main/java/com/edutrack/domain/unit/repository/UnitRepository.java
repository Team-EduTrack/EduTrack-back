package com.edutrack.domain.unit.repository;

import com.edutrack.domain.unit.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    // ID를 통해 단원 엔티티를 조회
    Optional<Unit> findById(Long id);

    // 강의 ID로 단원 목록 조회
    List<Unit> findAllByLectureId(Long lectureId);
}