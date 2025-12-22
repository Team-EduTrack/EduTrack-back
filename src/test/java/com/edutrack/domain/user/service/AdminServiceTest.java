package com.edutrack.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.edutrack.domain.user.dto.SearchAllUserResponse;
import com.edutrack.domain.user.dto.UserSearchResultResponse;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.UserToRole;
import com.edutrack.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService 단위 테스트")
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 20);
    }

    @Test
    @DisplayName("모든 사용자 조회 성공 - 첫 페이지")
    void 모든_사용자_조회_성공_첫페이지() {
        // given
        List<User> users = createMockUsers(5);
        Page<User> userPage = new PageImpl<>(users, pageable, 100L);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // when
        SearchAllUserResponse response = adminService.getAllUsers(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUsers()).hasSize(5);
        assertThat(response.getTotalCount()).isEqualTo(100L);
        assertThat(response.getTotalPages()).isEqualTo(5);
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getPageSize()).isEqualTo(20);
        assertThat(response.isHasNextPage()).isTrue();
        assertThat(response.isHasPreviousPage()).isFalse();

        // 사용자 정보 검증
        UserSearchResultResponse firstUser = response.getUsers().get(0);
        assertThat(firstUser.id()).isEqualTo(1L);
        assertThat(firstUser.name()).isEqualTo("사용자1");
        assertThat(firstUser.loginId()).isEqualTo("user1");
        assertThat(firstUser.role()).isEqualTo("STUDENT");
    }

    @Test
    @DisplayName("모든 사용자 조회 성공 - 마지막 페이지")
    void 모든_사용자_조회_성공_마지막페이지() {
        // given
        Pageable lastPageable = PageRequest.of(4, 20);
        List<User> users = createMockUsers(20);
        Page<User> userPage = new PageImpl<>(users, lastPageable, 100L);

        when(userRepository.findAll(lastPageable)).thenReturn(userPage);

        // when
        SearchAllUserResponse response = adminService.getAllUsers(lastPageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUsers()).hasSize(20);
        assertThat(response.getTotalCount()).isEqualTo(100L);
        assertThat(response.getCurrentPage()).isEqualTo(5);
        assertThat(response.isHasNextPage()).isFalse();
        assertThat(response.isHasPreviousPage()).isTrue();
    }

    @Test
    @DisplayName("모든 사용자 조회 성공 - 빈 페이지")
    void 모든_사용자_조회_성공_빈페이지() {
        // given
        List<User> emptyUsers = new ArrayList<>();
        Page<User> emptyPage = new PageImpl<>(emptyUsers, pageable, 0L);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // when
        SearchAllUserResponse response = adminService.getAllUsers(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUsers()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0L);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.isHasNextPage()).isFalse();
        assertThat(response.isHasPreviousPage()).isFalse();
    }

    @Test
    @DisplayName("모든 사용자 조회 성공 - 다양한 역할 포함")
    void 모든_사용자_조회_성공_다양한역할() {
        // given
        List<User> users = new ArrayList<>();
        
        // ADMIN 역할 사용자
        users.add(createMockUser(1L, "관리자", "admin", RoleType.ADMIN));
        
        // PRINCIPAL 역할 사용자
        users.add(createMockUser(2L, "원장", "principal1", RoleType.PRINCIPAL));
        
        // TEACHER 역할 사용자
        users.add(createMockUser(3L, "선생님", "teacher1", RoleType.TEACHER));
        
        // STUDENT 역할 사용자
        users.add(createMockUser(4L, "학생", "student1", RoleType.STUDENT));

        Page<User> userPage = new PageImpl<>(users, pageable, 4L);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // when
        SearchAllUserResponse response = adminService.getAllUsers(pageable);

        // then
        assertThat(response.getUsers()).hasSize(4);
        assertThat(response.getUsers().get(0).role()).isEqualTo("ADMIN");
        assertThat(response.getUsers().get(1).role()).isEqualTo("PRINCIPAL");
        assertThat(response.getUsers().get(2).role()).isEqualTo("TEACHER");
        assertThat(response.getUsers().get(3).role()).isEqualTo("STUDENT");
    }

    @Test
    @DisplayName("모든 사용자 조회 성공 - 페이지 크기 변경")
    void 모든_사용자_조회_성공_페이지크기변경() {
        // given
        Pageable customPageable = PageRequest.of(0, 10);
        List<User> users = createMockUsers(10);
        Page<User> userPage = new PageImpl<>(users, customPageable, 50L);

        when(userRepository.findAll(customPageable)).thenReturn(userPage);

        // when
        SearchAllUserResponse response = adminService.getAllUsers(customPageable);

        // then
        assertThat(response.getUsers()).hasSize(10);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalPages()).isEqualTo(5);
    }

    @Test
    @DisplayName("모든 사용자 조회 성공 - 중간 페이지")
    void 모든_사용자_조회_성공_중간페이지() {
        // given
        Pageable middlePageable = PageRequest.of(2, 20);
        List<User> users = createMockUsers(20);
        Page<User> userPage = new PageImpl<>(users, middlePageable, 100L);

        when(userRepository.findAll(middlePageable)).thenReturn(userPage);

        // when
        SearchAllUserResponse response = adminService.getAllUsers(middlePageable);

        // then
        assertThat(response.getCurrentPage()).isEqualTo(3);
        assertThat(response.isHasNextPage()).isTrue();
        assertThat(response.isHasPreviousPage()).isTrue();
    }

    // Helper Methods

    private List<User> createMockUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(createMockUser(
                (long) i,
                "사용자" + i,
                "user" + i,
                RoleType.STUDENT
            ));
        }
        return users;
    }

    private User createMockUser(Long id, String name, String loginId, RoleType roleType) {
        User user = mock(User.class);
        Role role = mock(Role.class);
        UserToRole userToRole = mock(UserToRole.class);

        Set<UserToRole> userToRoles = new HashSet<>();
        userToRoles.add(userToRole);

        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        when(user.getLoginId()).thenReturn(loginId);
        when(user.getPhone()).thenReturn("0101234567" + String.format("%02d", id));
        when(user.getEmail()).thenReturn(loginId + "@test.com");
        when(user.getUserToRoles()).thenReturn(userToRoles);
        when(userToRole.getRole()).thenReturn(role);
        when(role.getName()).thenReturn(roleType);

        return user;
    }
}
