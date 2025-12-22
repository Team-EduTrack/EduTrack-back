package com.edutrack.domain.user.service;

import static com.edutrack.domain.user.util.RoleUtils.extractPrimaryRoleName;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edutrack.domain.user.dto.SearchAllUserResponse;
import com.edutrack.domain.user.dto.UserSearchResultResponse;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;

  /**
   * 관리자 전용 모든 사용자 조회
   * 모든 학원의 모든 사용자를 조회합니다.
   */
  @Transactional(readOnly = true)
  public SearchAllUserResponse getAllUsers(Pageable pageable) {
    Page<User> users = userRepository.findAll(pageable);

    List<UserSearchResultResponse> userList = users.getContent().stream()
            .map(user -> new UserSearchResultResponse(
                    user.getId(),
                    user.getName(),
                    user.getLoginId(),
                    user.getPhone(),
                    user.getEmail(),
                    extractPrimaryRoleName(user)  // 대표 Role 1개
            ))
            .toList();

    Long totalCount = users.getTotalElements();

    return SearchAllUserResponse.builder()
        .users(userList)
        .totalCount(totalCount)
        .totalPages(users.getTotalPages())
        .currentPage(users.getNumber() + 1)
        .pageSize(users.getSize())
        .hasNextPage(users.hasNext())
        .hasPreviousPage(users.hasPrevious())
        .build();
  }
}
