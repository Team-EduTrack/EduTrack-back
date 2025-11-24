package com.edutrack.domain.user.service;
import com.edutrack.domain.user.dto.MyInfoResponse;
import com.edutrack.domain.user.dto.SignInRequest;
import com.edutrack.domain.user.dto.SignInResponse;
import com.edutrack.domain.user.dto.UserInfo;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.edutrack.domain.user.exception.InvalidLoginException;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.edutrack.domain.user.util.RoleUtils.extractPrimaryRoleName;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public SignInResponse signIn(SignInRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(InvalidLoginException::new);

        boolean matches = passwordEncoder.matches(request.password(), user.getPassword());
        if (!matches) {
            throw new InvalidLoginException();
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        // 대표 Role 1개만 뽑아서 사용
        String roleName = extractPrimaryRoleName(user);

        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getName(),
                roleName
        );

        return new SignInResponse(accessToken, refreshToken, userInfo);
    }

    public MyInfoResponse getMyInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String roleName = extractPrimaryRoleName(user);

        return new MyInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                roleName
        );
    }
}