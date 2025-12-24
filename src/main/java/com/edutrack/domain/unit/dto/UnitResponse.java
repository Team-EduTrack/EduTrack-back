package com.edutrack.domain.unit.dto;

import com.edutrack.domain.unit.entity.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UnitResponse {

    private final Long unitId;
    private final Long lectureId;
    private final String name;

    public static UnitResponse from(Unit unit) {
        return UnitResponse.builder()
                .unitId(unit.getId())
                .lectureId(unit.getLecture().getId())
                .name(unit.getName())
                .build();
    }
}

