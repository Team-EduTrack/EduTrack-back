package com.edutrack.domain.user.service;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.user.dto.SignupRequest;
import com.edutrack.domain.user.dto.SignupResponse;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.UserStatus;
import com.edutrack.domain.user.entity.UserToRole;
import com.edutrack.domain.user.entity.UserToRoleId;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.domain.user.repository.UserToRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AcademyRepository academyRepository;
  private final RoleRepository roleRepository;
  private final UserToRoleRepository userToRoleRepository;

  @Override
  public SignupResponse signup(SignupRequest request) {

    // 아이디 중복 체크
    if (userRepository.existsByLoginId(request.getLoginId())) {
      throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
    }

    // 이메일 중복 체크
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("이미 등록된 이메일입니다.");
    }

    // 전화번호 중복 체크
    if (userRepository.existsByPhone(request.getPhone())) {
      throw new IllegalArgumentException("이미 등록된 전화번호입니다.");
    }

    // 학원 코드 검증
    Academy academy = academyRepository.findByCode(request.getAcademyCode())
        .orElseThrow(() -> new IllegalArgumentException("학원 코드가 올바르지 않습니다."));

    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // 엔티티 생성
    User user = User.builder()
        .loginId(request.getLoginId())
        .password(encodedPassword)
        .name(request.getName())
        .phone(request.getPhone())
        .email(request.getEmail())
        .academy(academy)
        .emailVerified(false)
        .userStatus(UserStatus.ACTIVE)
        .build();

    User saved = userRepository.save(user);

    Role studentRole = roleRepository.findByName(RoleType.STUDENT)
        .orElseThrow(() -> new IllegalArgumentException("학생 역할이 사전에 세팅되어 있지 않습니다."));

    UserToRole userToRole = UserToRole.builder()
        .id(new UserToRoleId(saved.getId(), studentRole.getId()))
        .user(saved)
        .role(studentRole)
        .build();

    userToRoleRepository.save(userToRole);

    return new SignupResponse(saved.getId(), saved.getLoginId(), saved.getName(), RoleType.STUDENT);
  }

}
