package com.edutrack.domain.user.service;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.user.dto.SignupRequest;
import com.edutrack.domain.user.dto.SignupResponse;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.TempUserRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.domain.user.repository.UserToRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AcademyRepository academyRepository;
  private final TempUserRepository tempUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final UserToRoleRepository userToRoleRepository;

  @Override
  public void signup(SignupRequest request) {

    // 이미 정식 회원인지 체크
    if (userRepository.existsByLoginId(request.getLoginId())) {
      throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("이미 등록된 이메일입니다.");
    }

    if (userRepository.existsByPhone(request.getPhone())) {
      throw new IllegalArgumentException("이미 등록된 전화번호입니다.");
    }

    // 이미 가입 신청(TempUser) 되어 있는지 체크
    if (tempUserRepository.existsByLoginId(request.getLoginId())) {
      throw new IllegalArgumentException("이미 가입 신청된 아이디입니다.");
    }
    if (tempUserRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("이미 가입 신청된 이메일입니다.");
    }
    if (tempUserRepository.existsByPhone(request.getPhone())) {
      throw new IllegalArgumentException("이미 가입 신청된 전화번호입니다.");
    }

    // 학원 코드 검증 (이때는 academyCode 만 확인)
    academyRepository.findByCode(request.getAcademyCode())
        .orElseThrow(() -> new IllegalArgumentException("학원 코드가 올바르지 않습니다."));

    TempUser tempUser = TempUser.builder()
        .loginId(request.getLoginId())
        .password(request.getPassword())
        .name(request.getName())
        .phone(request.getPhone())
        .email(request.getEmail())
        .academyCode(request.getAcademyCode())
        .build();

    tempUserRepository.save(tempUser);
  }

  public SignupResponse completeSignup(String email){

    TempUser tempUser = tempUserRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("가입 신청 정보가 없습니다."));

    if(!tempUser.isVerified()){
      throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
    }

    // 학원 코드로 Academy 조회
    Academy academy = academyRepository.findByCode(tempUser.getAcademyCode())
        .orElseThrow(() -> new IllegalArgumentException("학원 코드가 올바르지 않습니다."));

    // 정식 User 생성 (여기서 비밀번호 암호화)
    User user = User.builder()
        .loginId(tempUser.getLoginId())
        .password(passwordEncoder.encode(tempUser.getPassword()))
        .name(tempUser.getName())
        .phone(tempUser.getPhone())
        .email(tempUser.getEmail())
        .academy(academy)
        .build(); // 기본값 : emailVerified=false , status=PENDING 이면 override 가능

    // 이메일 인증을 이미 마친 상태이므로 바로 활성화
    user.markEmailVerified();

    // 상태를 ACTIVE 로 변경
    user.activate();

    // 기본 역할 STUDENT 부여
    Role studentRole = roleRepository.findByName(RoleType.STUDENT)
        .orElseThrow(() -> new IllegalArgumentException("학생 역할이 사전에 세팅되어 있지 않습니다."));

    user.addRole(studentRole);

    userRepository.save(user);

    // TempUser 정리
    tempUserRepository.delete(tempUser);

    return new SignupResponse(
        user.getId(),
        user.getLoginId(),
        user.getName(),
        RoleType.STUDENT
    );

  }

}
