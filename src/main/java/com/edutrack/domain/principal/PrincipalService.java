package com.edutrack.domain.principal;

import com.edutrack.domain.principal.dto.PrincipalRegistrationRequest;
import com.edutrack.domain.principal.dto.PrincipalRegistrationResponse;
import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.UserStatus;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class PrincipalService {

    private final UserRepository userRepository;
    private final AcademyRepository academyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public PrincipalRegistrationResponse registerAcademy(PrincipalRegistrationRequest request) {

        //비밀번호 일치검사
        if (!request.getPassword()
                .equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        //중복 데이터 검사
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new ConflictException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("이미 등록된 전화번호입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("이미 등록된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Role principalRole = roleRepository.findByName(RoleType.PRINCIPAL)
                .orElseThrow(() -> new IllegalArgumentException("PRINCIPAL 역할 데이터가 없습니다."));

    User newPrincipal = User.builder()
        .loginId(request.getLoginId())
        .password(encodedPassword)
        .name(request.getPrincipalName())
        .phone(request.getPhone())
        .email(request.getEmail())
        .userStatus(UserStatus.ACTIVE)
        .emailVerified(false)
        .build();

    User savedPrincipal = userRepository.save(newPrincipal);

    //학원코드 생성
    String uniqueCode = generateUniqueAcademyCode();
    Academy newAcademy = new Academy(request
        .getAcademyName(), uniqueCode, savedPrincipal);
    Academy savedAcademy = academyRepository.save(newAcademy);

    // 원장 역할 부여 (필요하면 여기서 추가 Role도 더 붙이면 됨)
    savedPrincipal.addRole(principalRole);

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
    } while (academyRepository.findByCode(code).isPresent());
    return code;
  }


}
