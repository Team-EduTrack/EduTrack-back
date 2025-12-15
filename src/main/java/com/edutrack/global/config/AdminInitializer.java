package com.edutrack.global.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.UserStatus;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;

@Component
@Profile("!test")
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

        // -------------------------------------------------------
        // 1) ADMIN 계정 생성
        // -------------------------------------------------------
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

        // -------------------------------------------------------
        // 2) 테스트용 원장 + 학원 + 학생 생성
        // -------------------------------------------------------

        // 이미 테스트 계정이 있으면 전체 초기화 스킵
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
        Role teacherRole = roleRepository.findByName(RoleType.TEACHER)
                .orElseThrow(() -> new IllegalStateException("TEACHER 역할 없음"));

        // (1) 원장 생성
        User principal = User.builder()
                .loginId("principal1")
                .password(passwordEncoder.encode("1234"))
                .name("테스트원장")
                .phone("01000000001")
                .email("principal@test.com")
                .emailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();
        principal = userRepository.save(principal);

        // (2) 학원 생성
        Academy academy = new Academy("테스트학원", "EDU-0001", principal);
        academy = academyRepository.save(academy);

        // (3) 원장에 학원 연결 + 역할 부여
        principal.setAcademy(academy);
        principal.addRole(principalRole);
        userRepository.save(principal);

        // (4) 학생 생성
        User student = User.builder()
                .loginId("teststudent")
                .password(passwordEncoder.encode("1234"))
                .name("테스트학생")
                .phone("01000000000")
                .email("student@test.com")
                .academy(academy)
                .emailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();
        student = userRepository.save(student);
        student.addRole(studentRole);
        userRepository.save(student);

        // 강사 계정 생성
        User teacher = User.builder()
                .loginId("testteacher")
                .password(passwordEncoder.encode("1234"))
                .name("테스트강사")
                .phone("01000000002")
                .email("teacher@test.com")
                .academy(academy)
                .emailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();
        teacher = userRepository.save(teacher);
        teacher.addRole(teacherRole);
        userRepository.save(teacher);

        logger.info("🔥 테스트 학원 + 학생 + 강사 생성 완료");
        logger.info("학원코드 = EDU-0001");
        logger.info("원장 = principal1 / 1234");
        logger.info("학생 = teststudent / 1234");
        logger.info("강사 = testteacher / 1234");

        // -------------------------------------------------------
        // 3) 테스트용 학생 추가 생성 (4명)
        // -------------------------------------------------------

        // ❌ [오류 수정] new User(...) 대신 User.builder() 사용
        // (1) 학생2 생성
        User student2 = User.builder()
                .loginId("teststudent2")
                .password(passwordEncoder.encode("1234"))
                .name("학생2")
                .phone("01000000011")
                .email("student2@test.com")
                .academy(academy)
                .emailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();
        student2 = userRepository.save(student2);
        student2.addRole(studentRole);
        userRepository.save(student2);

        // (2) 학생3 생성
        User student3 = User.builder()
                .loginId("teststudent3")
                .password(passwordEncoder.encode("1234"))
                .name("학생3")
                .phone("01000000012")
                .email("student3@test.com")
                .academy(academy)
                .emailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();
        student3 = userRepository.save(student3);
        student3.addRole(studentRole);
        userRepository.save(student3);

        // (3) 학생4 생성
        User student4 = User.builder()
                .loginId("teststudent4")
                .password(passwordEncoder.encode("1234"))
                .name("학생4")
                .phone("01000000013")
                .email("student4@test.com")
                .academy(academy)
                .emailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();
        student4 = userRepository.save(student4);
        student4.addRole(studentRole);
        userRepository.save(student4);

        // (4) 학생5 생성
        User student5 = User.builder()
                .loginId("teststudent5")
                .password(passwordEncoder.encode("1234"))
                .name("학생5")
                .phone("01000000014")
                .email("student5@test.com")
                .academy(academy)
                .emailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();
        student5 = userRepository.save(student5);
        student5.addRole(studentRole);
        userRepository.save(student5);

        // -------------------------------------------------------
        // 4) 테스트용 강의 생성 (A강의, B강의)
        // -------------------------------------------------------

        // ❌ [오류 수정] new Lecture(...) 대신 Lecture.builder() 사용
        // (1) A강의 생성
        Lecture lectureA = Lecture.builder()
                .academy(academy)
                .teacher(teacher)
                .title("A강의")
                .description("A강의 설명입니다")
                .dayOfWeek(DayOfWeek.MONDAY)
                .startDate(LocalDateTime.of(LocalDate.now().minusDays(7), java.time.LocalTime.of(9, 0)))
                .endDate(LocalDateTime.of(LocalDate.now().plusDays(30), java.time.LocalTime.of(10, 0)))
                .build();
        lectureA = lectureRepository.save(lectureA);

        // (2) B강의 생성
        Lecture lectureB = Lecture.builder()
                .academy(academy)
                .teacher(teacher)
                .title("B강의")
                .description("B강의 설명입니다")
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startDate(LocalDateTime.of(LocalDate.now().minusDays(5), java.time.LocalTime.of(14, 0)))
                .endDate(LocalDateTime.of(LocalDate.now().plusDays(35), java.time.LocalTime.of(15, 0)))
                .build();
        lectureB = lectureRepository.save(lectureB);

        // -------------------------------------------------------
        // 5) 학생-강의 배정
        // -------------------------------------------------------

        // (1) A강의에 학생 배정
        LectureStudent lectureA_student = new LectureStudent(lectureA, student);
        LectureStudent lectureA_student2 = new LectureStudent(lectureA, student2);
        lectureStudentRepository.saveAll(List.of(lectureA_student, lectureA_student2));

        // (2) B강의에 학생 배정
        LectureStudent lectureB_student = new LectureStudent(lectureB, student);
        LectureStudent lectureB_student3 = new LectureStudent(lectureB, student3);
        LectureStudent lectureB_student4 = new LectureStudent(lectureB, student4);
        LectureStudent lectureB_student5 = new LectureStudent(lectureB, student5);
        lectureStudentRepository.saveAll(List.of(
                lectureB_student,
                lectureB_student3,
                lectureB_student4,
                lectureB_student5
        ));

        logger.info("🔥 테스트 강의 및 학생 배정 완료");
        logger.info("A강의 ID = {}, 학생 수 = 2명 (teststudent, 학생2)", lectureA.getId());
        logger.info("B강의 ID = {}, 학생 수 = 4명 (teststudent, 학생3, 학생4, 학생5)", lectureB.getId());
        logger.info("중복 학생 = teststudent (A강의와 B강의 모두 수강)");
    }
}