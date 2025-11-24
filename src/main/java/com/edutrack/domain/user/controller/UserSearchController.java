package com.edutrack.domain.user.controller;
import com.edutrack.domain.user.dto.UserSearchResultResponse;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academies/{academyId}/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserQueryService userQueryService;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultResponse>> searchUsers(
            @PathVariable Long academyId,
            @RequestParam(required = false) RoleType role,     // 학생/강사/원장 등
            @RequestParam(required = false) String keyword     // 검색어 (ID 또는 phone)
    ) {
        List<UserSearchResultResponse> result =
                userQueryService.searchUsers(academyId, role, keyword);

        return ResponseEntity.ok(result);
    }
}