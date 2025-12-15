package com.edutrack.domain.lecture.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.statistics.service.LectureStatisticsService;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

  private static final Logger log = LoggerFactory.getLogger(LectureServiceTest.class);
  @InjectMocks
  private LectureService lectureService;

  @Mock
  private LectureRepository lectureRepository;

  @Mock
  private LectureStudentRepository lectureStudentRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ExamRepository examRepository;

  @Mock
  private LectureStatisticsService lectureStatisticsService;

  @Mock
  private LectureHelper lectureHelper;

  private Lecture lecture;
  private User teacher;
  private User student1;
  private User student2;
  private Academy academy;

  @BeforeEach
  void setUp() {
    academy = mock(Academy.class);
    lenient().when(academy.getId()).thenReturn(1L);

    teacher = mock(User.class);
    lenient().when(teacher.getId()).thenReturn(10L);
    lenient().when(teacher.hasRole(RoleType.TEACHER)).thenReturn(true);

    student1 = mock(User.class);
    lenient().when(student1.getId()).thenReturn(20L);
    lenient().when(student1.hasRole(RoleType.STUDENT)).thenReturn(true);

    student2 = mock(User.class);
    lenient().when(student2.getId()).thenReturn(30L);
    lenient().when(student2.hasRole(RoleType.STUDENT)).thenReturn(true);

    lecture = mock(Lecture.class);
    lenient().when(lecture.getId()).thenReturn(50L);
    lenient().when(lecture.getTitle()).thenReturn("테스트 강의");
    lenient().when(lecture.getTeacher()).thenReturn(teacher);
    lenient().when(lecture.getAcademy()).thenReturn(academy);
    lenient().when(teacher.getName()).thenReturn("선생님");

  }

  @Test
  void 강사의_강의_목록_조회() {
    // given
    // LectureStudent mock 생성
    LectureStudent ls1 = mock(LectureStudent.class);
    when(ls1.getLecture()).thenReturn(lecture); // chained call NPE 방지
    LectureStudent ls2 = mock(LectureStudent.class);
    when(ls2.getLecture()).thenReturn(lecture);

    //lectureRepository stub
    when(lectureRepository.findAllByTeacherId(teacher.getId()))
        .thenReturn(List.of(lecture));

    //lectureStudentRepository stub
    List<Long> lectureIds = List.of(lecture.getId());
    when(lectureStudentRepository.findAllByLectureIdIn(lectureIds))
        .thenReturn(List.of(ls1, ls2));

    //examRepository stub
    when(examRepository.findByLectureId(lecture.getId()))
        .thenReturn(List.of());

    //lectureStatisticsService stub
    when(lectureStatisticsService.calculateAverageScore(List.of()))
        .thenReturn(0.0);

    //실제 서비스 호출
    var result = lectureService.getLecturesByTeacherId(teacher.getId());

    //검증
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(lectureRepository).findAllByTeacherId(teacher.getId());
    verify(lectureStudentRepository).findAllByLectureIdIn(lectureIds);
    verify(examRepository).findByLectureId(lecture.getId());
    verify(lectureStatisticsService).calculateAverageScore(List.of());

    log.info("=== 강사의 강의 목록 조회 테스트 결과 ===");
    result.forEach(dto -> log.info(
        "강의 ID : {}, 제목 : {}, 학생 수 : {}, 선생님 이름 : {}, 평균 점수 : {}",
        dto.getLectureId(),
        dto.getTitle(),
        dto.getStudentCount(),
        dto.getTeacherName(),
        dto.getAverageGrade()
    ));


  }

  //검증 부분은 컨트롤러 레이어에서 hasAnyRole 으로 이미 검증하므로, 서비스 레이어에서는 생략
  @Test
  void 강의_상세_조회() {
    //given
    LectureStudent ls1 = mock(LectureStudent.class);
    when(ls1.getStudent()).thenReturn(student1);
    LectureStudent ls2 = mock(LectureStudent.class);
    when(ls2.getStudent()).thenReturn(student2);

    when(lectureStudentRepository.findAllByLectureId(lecture.getId())).thenReturn(List.of(ls1, ls2));

    //lectureHelper stub
    when(lectureHelper.getLectureWithValidation(lecture.getId(), teacher.getId()))
        .thenReturn(lecture);

    //when
    var result = lectureService.getLectureDetailForTeacherId(lecture.getId(), teacher.getId());

    //then
    assertNotNull(result);
    assertEquals(lecture.getId(), result.getLectureId());
    assertEquals(2, result.getStudentCount());

    verify(lectureHelper).getLectureWithValidation(lecture.getId(), teacher.getId());
    verify(lectureStudentRepository).findAllByLectureId(lecture.getId());

    log.info("=== 강의 상세 조회 테스트 결과 ===");
    log.info("강의 ID : {}, 학생 수 : {}", result.getLectureId(), result.getStudentCount());
  }

  @Test
  void 강의_학생_목록_조회() {
    //given
    LectureStudent ls1 = mock(LectureStudent.class);
    when(ls1.getStudent()).thenReturn(student1);
    LectureStudent ls2 = mock(LectureStudent.class);
    when(ls2.getStudent()).thenReturn(student2);

    when(student1.getId()).thenReturn(20L);
    when(student1.getName()).thenReturn("홍길동");
    when(student1.getPhone()).thenReturn("01012345678");

    when(student2.getId()).thenReturn(30L);
    when(student2.getName()).thenReturn("김철수");
    when(student2.getPhone()).thenReturn("01012345679");

    when(lectureStudentRepository.findAllByLectureId(lecture.getId())).thenReturn(List.of(ls1, ls2));

    //when
    var result = lectureService.getStudentsByLecture(lecture.getId());

    //then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(20L, result.get(0).getStudentId());
    assertEquals("홍길동", result.get(0).getName());
    assertEquals("01012345678", result.get(0).getPhone());

    assertEquals(30L, result.get(1).getStudentId());
    assertEquals("김철수", result.get(1).getName());
    assertEquals("01012345679", result.get(1).getPhone());


    verify(lectureStudentRepository).findAllByLectureId(lecture.getId());

    log.info("=== 강의 학생 목록 조회 테스트 결과 ===");
    result.forEach(dto -> log.info(
        "학생 ID : {}, 이름 : {}, 전화번호 : {}",
        dto.getStudentId(),
        dto.getName(),
        dto.getPhone()
    ));
  }

  @Test
  void 할당_가능한_학생_조회() {
    // given
    when(lectureHelper.getLectureOrThrow(lecture.getId())).thenReturn(lecture);

    // 이미 배정된 학생 생성
    LectureStudent assigned1 = mock(LectureStudent.class);
    when(assigned1.getStudent()).thenReturn(student1);
    LectureStudent assigned2 = mock(LectureStudent.class);
    when(assigned2.getStudent()).thenReturn(student2);

    when(lectureStudentRepository.findAllByLectureId(lecture.getId()))
        .thenReturn(List.of(assigned1, assigned2));

    // student1, student2 필드 stub
    lenient().when(student1.getId()).thenReturn(20L);
    lenient().when(student1.getAcademy()).thenReturn(academy);
    lenient().when(student1.hasRole(RoleType.STUDENT)).thenReturn(true);

    lenient().when(student2.getId()).thenReturn(30L);
    lenient().when(student2.getAcademy()).thenReturn(academy);
    lenient().when(student2.hasRole(RoleType.STUDENT)).thenReturn(true);

    // 배정 가능한 학생 mock
    User availableStudent1 = mock(User.class);
    when(availableStudent1.getId()).thenReturn(60L);
    when(availableStudent1.getName()).thenReturn("박영희");
    lenient().when(availableStudent1.getAcademy()).thenReturn(academy);

    User availableStudent2 = mock(User.class);
    when(availableStudent2.getId()).thenReturn(70L);
    when(availableStudent2.getName()).thenReturn("김아무개");
    lenient().when(availableStudent2.getAcademy()).thenReturn(academy);

    // 실제로 전달되는 값 사용 (assignedIds는 [20L, 30L], name은 null)
    List<Long> excludedIds = List.of(20L, 30L);
    when(userRepository.findAvailableStudents(eq(academy.getId()), eq(excludedIds), isNull()))
        .thenReturn(List.of(availableStudent1, availableStudent2));

    //lectureHelper stub
    when(lectureHelper.isStudent(availableStudent1)).thenReturn(true);
    when(lectureHelper.isStudent(availableStudent2)).thenReturn(true);

    // when
    var result = lectureService.getAvailableStudents(lecture.getId(), null);

    // then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(dto -> dto.getStudentId() == 60L && dto.getName().equals("박영희")));
    assertTrue(result.stream().anyMatch(dto -> dto.getStudentId() == 70L && dto.getName().equals("김아무개")));


    verify(lectureHelper).getLectureOrThrow(lecture.getId());
    verify(lectureStudentRepository).findAllByLectureId(lecture.getId());
    verify(userRepository).findAvailableStudents(eq(academy.getId()), eq(excludedIds), isNull());

    log.info("=== 할당 가능한 학생 목록 조회 테스트 결과 ===");
    result.forEach(dto -> log.info("학생 ID : {}, 이름 : {}", dto.getStudentId(), dto.getName()));
  }

  @Test
  void 학생_배정() {
    // given
    Long lectureId = lecture.getId();
    List<Long> studentIds = List.of(student1.getId(), student2.getId());
    
    // lectureHelper stub
    when(lectureHelper.getLectureOrThrow(lectureId)).thenReturn(lecture);
    when(lectureHelper.getValidStudents(studentIds, lecture)).thenReturn(Set.of(student1, student2));
    
    // 이미 배정된 학생이 없는 경우
    when(lectureStudentRepository.findAllByLectureId(lectureId)).thenReturn(List.of());
    
    // ArgumentCaptor는 verify에서만 사용
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Iterable<LectureStudent>> captor = ArgumentCaptor.forClass(Iterable.class);
    
    // saveAll은 List를 반환하므로 Set을 List로 변환하여 반환
    when(lectureStudentRepository.saveAll(captor.capture())).thenAnswer(invocation -> {
      Iterable<LectureStudent> iterable = invocation.getArgument(0);
      List<LectureStudent> list = new java.util.ArrayList<>();
      iterable.forEach(list::add);
      return list;
    });
    
    // when
    var result = lectureService.assignStudents(lectureId, studentIds);
    
    // then
    assertNotNull(result);
    assertEquals(lectureId, result.getLectureId());
    assertEquals(2, result.getAssignedCount());
    
    // 저장된 LectureStudent 검증
    verify(lectureStudentRepository).saveAll(captor.capture());
    Iterable<LectureStudent> savedIterable = captor.getValue();
    List<LectureStudent> savedLectureStudents = new java.util.ArrayList<>();
    savedIterable.forEach(savedLectureStudents::add);
    assertEquals(2, savedLectureStudents.size());
    
    // 저장된 학생 ID 확인
    Set<Long> savedStudentIds = savedLectureStudents.stream()
        .map(ls -> ls.getStudent().getId())
        .collect(Collectors.toSet());
    assertTrue(savedStudentIds.contains(student1.getId()));
    assertTrue(savedStudentIds.contains(student2.getId()));
    
    verify(lectureHelper).getLectureOrThrow(lectureId);
    verify(lectureHelper).getValidStudents(studentIds, lecture);
    verify(lectureStudentRepository).findAllByLectureId(lectureId);
    
    log.info("=== 학생 배정 테스트 결과 ===");
    log.info("강의 ID : {}, 배정된 학생 수 : {}", result.getLectureId(), result.getAssignedCount());
  }
}