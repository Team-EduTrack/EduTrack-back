package com.edutrack.domain.user.service;

import com.edutrack.domain.user.dto.UserSearchResultResponse;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    public List<UserSearchResultResponse> searchUsers(Long academyId, String keyword) {
        List<User> users = userRepository.searchByLoginIdOrPhone(academyId, keyword);

        return users.stream()
                .map(user -> {
                    String roleName = user.getRoles().stream()
                            .findFirst()
                            .map(Role::getName)
                            .orElse("STUDENT");

                    return new UserSearchResultResponse(
                            user.getId(),
                            user.getName(),
                            user.getLoginId(),
                            user.getPhone(),
                            roleName
                    );
                })
                .toList();
    }
}