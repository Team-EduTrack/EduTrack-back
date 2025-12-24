package com.edutrack.domain.unit.service;

import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.service.LectureHelper;
import com.edutrack.domain.unit.dto.UnitCreateRequest;
import com.edutrack.domain.unit.dto.UnitResponse;
import com.edutrack.domain.unit.entity.Unit;
import com.edutrack.domain.unit.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UnitService {

    private final UnitRepository unitRepository;
    private final LectureHelper lectureHelper;

    /**
     * 단원 추가
     * - 강의 담당 강사 또는 해당 학원 원장만 추가 가능
     */
    public UnitResponse createUnit(Long userId, UnitCreateRequest request) {
        // 강의 조회 및 권한 검증
        Lecture lecture = lectureHelper.getLectureWithValidation(request.getLectureId(), userId);

        // 단원 생성
        Unit unit = Unit.builder()
                .lecture(lecture)
                .name(request.getName())
                .build();

        Unit savedUnit = unitRepository.save(unit);

        return UnitResponse.from(savedUnit);
    }

    /**
     * 강의별 단원 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UnitResponse> getUnitsByLectureId(Long lectureId) {
        List<Unit> units = unitRepository.findAllByLectureId(lectureId);
        return units.stream()
                .map(UnitResponse::from)
                .collect(Collectors.toList());
    }
}

