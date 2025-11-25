package com.edutrack.api.principal;

import com.edutrack.api.principal.dto.PrincipalRegistrationRequest;
import com.edutrack.api.principal.dto.PrincipalRegistrationResponse;
import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.UserStatus;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PrincipalService {

  private final UserRepository userRepository;
  private final AcademyRepository academyRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public PrincipalRegistrationResponse registerAcademy(
      PrincipalRegistrationRequest principalRegistrationRequest) {

    //비밀번호 일치검사
    if (!principalRegistrationRequest.getPassword()
        .equals(principalRegistrationRequest.getPasswordConfirm())) {
      throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }

    //중복 데이터 검사
    if (userRepository.existsByLoginId(principalRegistrationRequest.getLoginId())) {
      throw new ConflictException("이미 존재하는 아이디입니다.");
    }
    if (userRepository.existsByPhone(principalRegistrationRequest.getPhone())) {
      throw new ConflictException("이미 등록된 전화번호입니다.");
    }
    if (userRepository.existsByEmail(principalRegistrationRequest.getEmail())) {
      throw new ConflictException("이미 등록된 이메일입니다.");
    }

    String encodedPassword = passwordEncoder.encode(principalRegistrationRequest.getPassword());

    Role principalRole = roleRepository.findByName(RoleType.PRINCIPAL)
        .orElseThrow(() -> new IllegalArgumentException("PRINCIPAL 역할 데이터가 없습니다."));

    User newPrincipal = User.builder()
        .loginId(principalRegistrationRequest.getLoginId())
        .password(encodedPassword)
        .name(principalRegistrationRequest.getPrincipalName())
        .phone(principalRegistrationRequest.getPhone())
        .email(principalRegistrationRequest.getEmail())
        .emailVerified(false)
        .userStatus(UserStatus.ACTIVE)
        .build();

    User savedPrincipal = userRepository.save(newPrincipal);

    // 원장 역할 부여 (필요하면 여기서 추가 Role도 더 붙이면 됨)
    savedPrincipal.addRole(principalRole);
    userRepository.save(savedPrincipal);

    //학원코드 생성
    String uniqueCode = generateUniqueAcademyCode();
    Academy newAcademy = new Academy(principalRegistrationRequest
        .getAcademyName(), uniqueCode, savedPrincipal);
    Academy savedAcademy = academyRepository.save(newAcademy);

    savedPrincipal.setAcademy(savedAcademy);  // 원장을 학원에 매핑 (User도 양방향)
    userRepository.save(savedPrincipal);

    return PrincipalRegistrationResponse.builder()
        .id(savedAcademy.getId())
        .academyName(savedAcademy.getName())
        .academyCode(savedAcademy.getCode())
        .build();

  }

  private String generateUniqueAcademyCode() {
    String code;
    do {
      int randomNum = ThreadLocalRandom.current().nextInt(0, 10000);
      code = "EDU-" + randomNum;
    } while (academyRepository.existsByCode(code));
    return code;
  }


}
