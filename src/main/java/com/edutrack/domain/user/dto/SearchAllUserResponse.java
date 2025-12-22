package com.edutrack.domain.user.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchAllUserResponse {
    private final List<UserSearchResultResponse> users;
    private final Long totalCount;
    private final int totalPages;
    private final int currentPage;
    private final int pageSize;
    private final boolean hasNextPage;
    private final boolean hasPreviousPage;
}
