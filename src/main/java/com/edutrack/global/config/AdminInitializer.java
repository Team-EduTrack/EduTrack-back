package com.edutrack.global.config;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.entity.LectureStudentId;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.user.entity.*;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AcademyRepository academyRepository;
    private final PasswordEncoder passwordEncoder;
    private final LectureRepository lectureRepository;
    private final LectureStudentRepository lectureStudentRepository;

    private static final String ADMIN_LOGIN_ID = "admin";
    private static final String ADMIN_PASSWORD = "admin@1234";
    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // --------------------
        // 1) ADMIN 계정 생성
        // --------------------
        if (userRepository.findByLoginId(ADMIN_LOGIN_ID).isEmpty()) {

            Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ADMIN 역할이 DB에 존재하지 않습니다."));

            User admin = User.builder()
                    .loginId(ADMIN_LOGIN_ID)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .name("시스템관리자")
                    .phone("01012345678")
                    .email("admin@edutrack.com")
                    .emailVerified(true)
                    .userStatus(UserStatus.ACTIVE)
                    .build();

            admin = userRepository.save(admin);
            admin.addRole(adminRole);
            userRepository.save(admin);

            logger.info(">>> ADMIN 계정 생성 완료");
        }

        // --------------------
        // 2) 테스트용 원장 + 학원 + 학생 생성
        // --------------------

        if (userRepository.existsByLoginId("teststudent")
            || userRepository.existsByEmail("student@test.com")
            || userRepository.existsByPhone("01000000000")) {
            logger.info(">>> 테스트 계정 이미 존재함. 초기화 스킵.");
            return;
        }

        // ROLE 조회
        Role principalRole = roleRepository.findByName(RoleType.PRINCIPAL)
                .orElseThrow(() -> new IllegalStateException("PRINCIPAL 역할 없음"));
        Role studentRole = roleRepository.findByName(RoleType.STUDENT)
                .orElseThrow(() -> new IllegalStateException("STUDENT 역할 없음"));

        // (1) 원장 생성 — academy는 null로 시작
        User principal = new User(
                "principal1",
                passwordEncoder.encode("1234"),
                "테스트원장",
                "01000000001",
                "principal@test.com",
                null
        );
        principal = userRepository.save(principal);

        // (2) 학원 생성 (원장 FK 필요)
        Academy academy = new Academy("테스트학원", "EDU-0001", principal);
        academy = academyRepository.save(academy);

        // (3) 원장에 academy 연결
        principal.setAcademy(academy);
        principal.addRole(principalRole);
        userRepository.save(principal);

        // (4) 학생 생성
        User student = new User(
                "teststudent",
                passwordEncoder.encode("1234"),
                "테스트학생",
                "01000000000",
                "student@test.com",
                academy
        );
        student = userRepository.save(student);

        // (5) 학생에게 STUDENT 역할 부여
        student.addRole(studentRole);
        userRepository.save(student);

        createTestLectureMapping(student);

        logger.info("🔥 테스트 학원 + 학생 만들기 완료");
        logger.info("학원코드 = EDU-0001");
        logger.info("원장 = principal1 / 1234");
        logger.info("학생 = teststudent / 1234");
    }

    /*
     * 학생 - 강의 자동 매핑
     */
    private void createTestLectureMapping(User student) {
        try {
            Lecture lecture = lectureRepository.findById(1L).orElseThrow(() -> new IllegalStateException("강의 id =1 없음"));

            LectureStudentId id = new LectureStudentId(lecture.getId(), student.getId());

            if (!lectureStudentRepository.existsById(id)) {
                LectureStudent lectureStudent = new LectureStudent(lecture, student);
                lectureStudentRepository.save(lectureStudent);
                logger.info("학생-강의 매핑 완료 (lectureId={}, studentId={})",
                        lecture.getId(), student.getId());
            }
        } catch (Exception e) {
            logger.warn("매핑 스킵 : {}",e.getMessage());
        }

    }
}