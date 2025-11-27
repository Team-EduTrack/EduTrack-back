package com.edutrack.domain.exam.dto;

import com.edutrack.domain.exam.entity.Difficulty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionRegistrationRequest {


    @NotBlank(message = "문제 내용은 필수입니다.")
    private String content;

    @NotEmpty(message = "문제의 보기는 최소 1개 이상이어야 합니다.")
    private List<String> choices;
    @NotNull(message = "정답 번호는 필수입니다.")
    @Min(value = 1, message = "정답 번호는 1 이상이어야 합니다.")
    @Max(value = 5, message = "정답 번호는 1~5 사이여야 합니다.")
    private Integer answerNumber;

    @NotNull(message = "배점은 필수입니다.")
    @Min(value = 1, message = "배점은 1점 이상이어야 합니다.")
    private Integer score;

    @NotNull(message = "단원 ID는 필수입니다.")
    private Long unitId; // 단원 ID

    @NotNull(message = "난이도는 필수입니다.")
    private Difficulty difficulty;


}
