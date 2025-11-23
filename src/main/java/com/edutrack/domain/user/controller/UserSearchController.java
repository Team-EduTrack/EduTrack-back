package com.edutrack.domain.user.controller;
import com.edutrack.domain.user.dto.UserSearchResultResponse;
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
            @RequestParam("keyword") String keyword
    ) {
        List<UserSearchResultResponse> result =
                userQueryService.searchUsers(academyId, keyword);

        return ResponseEntity.ok(result);
    }
}