package com.edutrack.domain.user.service;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.user.dto.AcademyVerifyRequest;
import com.edutrack.domain.user.dto.SignupRequest;
import com.edutrack.domain.user.dto.SignupResponse;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.SignupLockRepository;
import com.edutrack.domain.user.repository.TempUserRedisRepository;
import com.edutrack.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AcademyRepository academyRepository;
  private final TempUserRedisRepository tempUserRedisRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final SignupLockRepository signupLockRepository;

  @Override
  public void signupRequest(SignupRequest request) {

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

    // Redis 중복 체크
    if (signupLockRepository.existsByLoginId(request.getLoginId())) {
      throw new IllegalArgumentException("이미 가입 신청된 아이디입니다.");
    }
    if (signupLockRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("이미 가입 신청된 이메일입니다.");
    }
    if (signupLockRepository.existsByPhone(request.getPhone())) {
      throw new IllegalArgumentException("이미 가입 신청된 전화번호입니다.");
    }

    // 임시 등록 TempUser 생성 (비밀번호 암호화)
    TempUser tempUser = TempUser.builder()
        .loginId(request.getLoginId())
        .password(passwordEncoder.encode(request.getPassword()))
        .name(request.getName())
        .phone(request.getPhone())
        .email(request.getEmail())
        .verified(false)
        .build();

    tempUserRedisRepository.save(tempUser, 10 * 60);

    signupLockRepository.lockAll(
        tempUser.getLoginId(),
        tempUser.getEmail(),
        tempUser.getPhone()
    );
  }

  // 학원 코드 검증
  @Override
  public void verifyAcademyCode(AcademyVerifyRequest request){

    TempUser tempUser = tempUserRedisRepository.findByEmail(request.getEmail());

    if(tempUser == null){
      throw new IllegalArgumentException("가입 신청 정보가 없습니다.");
    }

    Academy academy = academyRepository.findByCode(request.getAcademyCode())
        .orElseThrow(() -> new IllegalArgumentException("학원 코드가 올바르지 않습니다."));

    tempUser.updateAcademyCode(academy.getCode());
    tempUserRedisRepository.save(tempUser, 10 * 60);

  }

  // 최종 회원가입
  public SignupResponse completeSignup(String email) {

    TempUser tempUser = tempUserRedisRepository.findByEmail(email);

    if (tempUser == null) {
      throw new IllegalArgumentException("가입 신청 정보가 없습니다.");
    }

    if (!tempUser.isVerified()) {
      throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
    }

    if(tempUser.getAcademyCode() == null){
      throw new IllegalArgumentException("학원 코드 인증이 필요합니다.");
    }

    // 학원 코드로 Academy 조회
    Academy academy = academyRepository.findByCode(tempUser.getAcademyCode())
        .orElseThrow(() -> new IllegalArgumentException("학원 코드가 올바르지 않습니다."));

    // 정식 User 생성 (여기서 비밀번호 암호화)
    User user = User.builder()
        .loginId(tempUser.getLoginId())
        .password(tempUser.getPassword())
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
    tempUserRedisRepository.deleteByEmail(email);

    // 락 해제
    signupLockRepository.unLockAll(
        tempUser.getLoginId(),
        tempUser.getEmail(),
        tempUser.getPhone()
    );

    return new SignupResponse(
        user.getId(),
        user.getLoginId(),
        user.getName(),
        RoleType.STUDENT
    );

  }

}
