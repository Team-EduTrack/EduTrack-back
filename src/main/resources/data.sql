-- ============================================
-- 초기 데이터 삽입 스크립트
-- JPA가 테이블을 생성한 후 Spring Boot가 자동으로 실행합니다.
-- ============================================

-- 1. 역할 초기 데이터
INSERT IGNORE INTO role (name) VALUES ('ADMIN');
INSERT IGNORE INTO role (name) VALUES ('PRINCIPAL');
INSERT IGNORE INTO role (name) VALUES ('TEACHER');
INSERT IGNORE INTO role (name) VALUES ('STUDENT');

-- 2. admin 계정 생성
INSERT IGNORE INTO users (login_id, password, name, phone, email, email_verified, user_status)
VALUES ('admin', '$2a$10$MUcQKa2IoVzB36nK6KXSme.D.lH5/2e8olaRC3MD5mNISTk7.4wFO', '시스템관리자', '01012345678', 'admin@edutrack.com', 1, 'ACTIVE');

-- admin 역할 매핑
INSERT IGNORE INTO user_to_role (user_id, role_id)
SELECT u.id, r.id FROM users u, role r WHERE u.login_id = 'admin' AND r.name = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM user_to_role WHERE user_id = u.id AND role_id = r.id);

-- 3. Principal 1명 생성
INSERT IGNORE INTO users (login_id, password, name, phone, email, email_verified, user_status, academy_id)
VALUES ('principal1', '$2a$10$MUcQKa2IoVzB36nK6KXSme.D.lH5/2e8olaRC3MD5mNISTk7.4wFO', '김원장', '01011111111', 'principal1@edutrack.com', 1, 'ACTIVE', NULL);

-- Principal 역할 매핑
INSERT IGNORE INTO user_to_role (user_id, role_id)
SELECT u.id, r.id FROM users u, role r WHERE u.login_id = 'principal1' AND r.name = 'PRINCIPAL'
AND NOT EXISTS (SELECT 1 FROM user_to_role WHERE user_id = u.id AND role_id = r.id);

-- Academy 생성
INSERT IGNORE INTO academy (principal_user_id, name, code, created_at)
SELECT id, '에듀트랙 학원', 'EDU-0001', NOW() FROM users WHERE login_id = 'principal1'
AND NOT EXISTS (SELECT 1 FROM academy WHERE code = 'EDU-0001');

-- Principal의 academy_id 업데이트
UPDATE users SET academy_id = (SELECT id FROM academy WHERE code = 'EDU-0001') WHERE login_id = 'principal1' AND academy_id IS NULL;

-- 4. Teacher 5명 생성
INSERT IGNORE INTO users (login_id, password, name, phone, email, email_verified, user_status, academy_id)
SELECT 'teacher1', '$2a$10$MUcQKa2IoVzB36nK6KXSme.D.lH5/2e8olaRC3MD5mNISTk7.4wFO', '김철수', '01020000001', 'teacher1@edutrack.com', 1, 'ACTIVE', id FROM academy WHERE code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM users WHERE login_id = 'teacher1');

INSERT IGNORE INTO users (login_id, password, name, phone, email, email_verified, user_status, academy_id)
SELECT 'teacher2', '$2a$10$MUcQKa2IoVzB36nK6KXSme.D.lH5/2e8olaRC3MD5mNISTk7.4wFO', '박영희', '01020000002', 'teacher2@edutrack.com', 1, 'ACTIVE', id FROM academy WHERE code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM users WHERE login_id = 'teacher2');

INSERT IGNORE INTO users (login_id, password, name, phone, email, email_verified, user_status, academy_id)
SELECT 'teacher3', '$2a$10$MUcQKa2IoVzB36nK6KXSme.D.lH5/2e8olaRC3MD5mNISTk7.4wFO', '이민수', '01020000003', 'teacher3@edutrack.com', 1, 'ACTIVE', id FROM academy WHERE code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM users WHERE login_id = 'teacher3');

INSERT IGNORE INTO users (login_id, password, name, phone, email, email_verified, user_status, academy_id)
SELECT 'teacher4', '$2a$10$MUcQKa2IoVzB36nK6KXSme.D.lH5/2e8olaRC3MD5mNISTk7.4wFO', '정수진', '01020000004', 'teacher4@edutrack.com', 1, 'ACTIVE', id FROM academy WHERE code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM users WHERE login_id = 'teacher4');

INSERT IGNORE INTO users (login_id, password, name, phone, email, email_verified, user_status, academy_id)
SELECT 'teacher5', '$2a$10$MUcQKa2IoVzB36nK6KXSme.D.lH5/2e8olaRC3MD5mNISTk7.4wFO', '최지훈', '01020000005', 'teacher5@edutrack.com', 1, 'ACTIVE', id FROM academy WHERE code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM users WHERE login_id = 'teacher5');

-- Teacher 역할 매핑
INSERT IGNORE INTO user_to_role (user_id, role_id)
SELECT u.id, r.id FROM users u, role r 
WHERE u.login_id IN ('teacher1', 'teacher2', 'teacher3', 'teacher4', 'teacher5') AND r.name = 'TEACHER'
AND NOT EXISTS (SELECT 1 FROM user_to_role WHERE user_id = u.id AND role_id = r.id);

-- 5. Student 300명 생성 (Recursive CTE 사용)
INSERT IGNORE INTO users (login_id, password, name, phone, email, email_verified, user_status, academy_id)
WITH RECURSIVE seq AS (
  SELECT 1 as seq_num
  UNION ALL
  SELECT seq_num + 1 FROM seq WHERE seq_num < 300
)
SELECT 
  CONCAT('student', seq.seq_num) as login_id,
  '$2a$10$MUcQKa2IoVzB36nK6KXSme.D.lH5/2e8olaRC3MD5mNISTk7.4wFO' as password,
  CONCAT(
    ELT(MOD(seq.seq_num - 1, 20) + 1, '김', '이', '박', '최', '정', '한', '윤', '오', '강', '임', '신', '조', '배', '송', '권', '안', '유', '문', '전', '홍'),
    ELT(MOD(seq.seq_num - 1, 30) + 1, '민준', '서연', '도윤', '지우', '예준', '소율', '하준', '서윤', '민성', '채원', '시우', '지안', '준서', '서현', '지호', '나은', '준혁', '하린', '지훈', '소은', '민재', '다은', '태현', '서아', '건우', '민수', '지은', '준호', '수진', '민지')
  ) as name,
  CONCAT('0103', LPAD(seq.seq_num, 7, '0')) as phone,
  CONCAT('student', seq.seq_num, '@edutrack.com') as email,
  1 as email_verified,
  'ACTIVE' as user_status,
  (SELECT id FROM academy WHERE code = 'EDU-0001' LIMIT 1) as academy_id
FROM seq;

-- Student 역할 매핑
INSERT IGNORE INTO user_to_role (user_id, role_id)
SELECT u.id, r.id FROM users u, role r
WHERE u.login_id LIKE 'student%' AND r.name = 'STUDENT'
AND NOT EXISTS (SELECT 1 FROM user_to_role WHERE user_id = u.id AND role_id = r.id);

-- 6. Lecture 생성 (Teacher마다 1개씩, 총 5개)
-- teacher1 선생님 강의 1개
INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, image_url, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 수학', 'teacher1 선생님의 중등 수학 강의입니다.', '/images/lecture2.jpeg', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher1' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 수학');

-- teacher2 선생님 강의 1개
INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, image_url, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 영어', 'teacher2 선생님의 중등 영어 강의입니다.', '/images/lecture3.jpeg', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher2' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 영어');

-- teacher3 선생님 강의 1개
INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, image_url, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 국어', 'teacher3 선생님의 중등 국어 강의입니다.', '/images/lecture1.jpeg', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher3' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 국어');

-- teacher4 선생님 강의 1개
INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, image_url, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 과학', 'teacher4 선생님의 중등 과학 강의입니다.', '/images/lecture4.jpeg', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher4' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 과학');

-- teacher5 선생님 강의 1개
INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, image_url, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 사회', 'teacher5 선생님의 중등 사회 강의입니다.', '/images/lecture5.jpeg', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher5' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 사회');

-- 기존 강의들의 image_url 업데이트 (INSERT IGNORE로 인해 기존 강의는 업데이트되지 않으므로 별도로 처리)
UPDATE lecture SET image_url = '/images/lecture1.jpeg' WHERE title = '중등 국어' AND (image_url IS NULL OR image_url = '');
UPDATE lecture SET image_url = '/images/lecture2.jpeg' WHERE title = '중등 수학' AND (image_url IS NULL OR image_url = '');
UPDATE lecture SET image_url = '/images/lecture3.jpeg' WHERE title = '중등 영어' AND (image_url IS NULL OR image_url = '');
UPDATE lecture SET image_url = '/images/lecture4.jpeg' WHERE title = '중등 과학' AND (image_url IS NULL OR image_url = '');
UPDATE lecture SET image_url = '/images/lecture5.jpeg' WHERE title = '중등 사회' AND (image_url IS NULL OR image_url = '');

-- 강의 요일 설정 (각 강의마다 월요일, 수요일, 금요일)
INSERT IGNORE INTO lecture_days_of_week (lecture_id, day_of_week)
SELECT id, 'MONDAY' FROM lecture
WHERE NOT EXISTS (SELECT 1 FROM lecture_days_of_week WHERE lecture_id = lecture.id AND day_of_week = 'MONDAY');

INSERT IGNORE INTO lecture_days_of_week (lecture_id, day_of_week)
SELECT id, 'WEDNESDAY' FROM lecture
WHERE NOT EXISTS (SELECT 1 FROM lecture_days_of_week WHERE lecture_id = lecture.id AND day_of_week = 'WEDNESDAY');

INSERT IGNORE INTO lecture_days_of_week (lecture_id, day_of_week)
SELECT id, 'FRIDAY' FROM lecture
WHERE NOT EXISTS (SELECT 1 FROM lecture_days_of_week WHERE lecture_id = lecture.id AND day_of_week = 'FRIDAY');

-- 7. 학생-강의 할당 (각 강의마다 80~130명의 학생 할당)
-- 학생 300명(student1 ~ student300) 중 대부분 할당 (약 10~20명은 할당되지 않아도 됨)
-- 학생은 여러 강의에 등록 가능

-- 강의별 학생 할당 (각 강의마다 80~130명 할당)
-- 이미 할당된 학생 수를 확인하고, 목표 인원수에 도달하지 않았을 때만 추가로 할당
-- 중복 할당 방지를 위해 NOT EXISTS 조건과 함께 사용
-- 학생은 여러 강의에 등록 가능하지만, 대부분의 학생이 최소 1개 강의에 할당되도록 보장
-- 각 강의마다 순환적으로 학생을 할당하여 할당되지 않은 학생 수를 최소화

-- 중등 수학 강의에 95명 할당 (student1부터 순차적으로)
INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    l.id as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM lecture l
CROSS JOIN users u
WHERE l.title = '중등 수학'
AND u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 300
AND NOT EXISTS (
    SELECT 1 FROM lecture_student ls 
    WHERE ls.lecture_id = l.id AND ls.user_id = u.id
)
AND (
    SELECT COUNT(*) FROM lecture_student ls2 
    WHERE ls2.lecture_id = l.id
) < 95
ORDER BY CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED)
LIMIT 95;

-- 중등 영어 강의에 110명 할당 (student60부터 순차적으로, 순환)
INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    l.id as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM lecture l
CROSS JOIN users u
WHERE l.title = '중등 영어'
AND u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 300
AND NOT EXISTS (
    SELECT 1 FROM lecture_student ls 
    WHERE ls.lecture_id = l.id AND ls.user_id = u.id
)
AND (
    SELECT COUNT(*) FROM lecture_student ls2 
    WHERE ls2.lecture_id = l.id
) < 110
ORDER BY MOD(CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) + 59, 300) + 1
LIMIT 110;

-- 중등 국어 강의에 85명 할당 (student120부터 순차적으로, 순환)
INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    l.id as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM lecture l
CROSS JOIN users u
WHERE l.title = '중등 국어'
AND u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 300
AND NOT EXISTS (
    SELECT 1 FROM lecture_student ls 
    WHERE ls.lecture_id = l.id AND ls.user_id = u.id
)
AND (
    SELECT COUNT(*) FROM lecture_student ls2 
    WHERE ls2.lecture_id = l.id
) < 85
ORDER BY MOD(CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) + 119, 300) + 1
LIMIT 85;

-- 중등 과학 강의에 125명 할당 (student180부터 순차적으로, 순환)
INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    l.id as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM lecture l
CROSS JOIN users u
WHERE l.title = '중등 과학'
AND u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 300
AND NOT EXISTS (
    SELECT 1 FROM lecture_student ls 
    WHERE ls.lecture_id = l.id AND ls.user_id = u.id
)
AND (
    SELECT COUNT(*) FROM lecture_student ls2 
    WHERE ls2.lecture_id = l.id
) < 125
ORDER BY MOD(CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) + 179, 300) + 1
LIMIT 125;

-- 중등 사회 강의에 100명 할당 (student240부터 순차적으로, 순환)
INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    l.id as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM lecture l
CROSS JOIN users u
WHERE l.title = '중등 사회'
AND u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 300
AND NOT EXISTS (
    SELECT 1 FROM lecture_student ls 
    WHERE ls.lecture_id = l.id AND ls.user_id = u.id
)
AND (
    SELECT COUNT(*) FROM lecture_student ls2 
    WHERE ls2.lecture_id = l.id
) < 100
ORDER BY MOD(CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) + 239, 300) + 1
LIMIT 100;

-- 8. 단원(Unit) 생성 (각 강의마다 5~10개의 단원 생성)
-- 강의 1: 중등 수학
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '1. 자연수와 정수' FROM lecture WHERE title = '중등 수학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '1. 자연수와 정수');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '2. 분수와 소수' FROM lecture WHERE title = '중등 수학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '2. 분수와 소수');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '3. 문자와 식' FROM lecture WHERE title = '중등 수학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '3. 문자와 식');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '4. 일차방정식' FROM lecture WHERE title = '중등 수학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '4. 일차방정식');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '5. 일차함수' FROM lecture WHERE title = '중등 수학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '5. 일차함수');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '6. 이차방정식' FROM lecture WHERE title = '중등 수학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '6. 이차방정식');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '7. 이차함수' FROM lecture WHERE title = '중등 수학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '7. 이차함수');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '8. 도형의 기초' FROM lecture WHERE title = '중등 수학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '8. 도형의 기초');

-- 강의 2: 중등 영어
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '1. 인사와 자기소개' FROM lecture WHERE title = '중등 영어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '1. 인사와 자기소개');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '2. 현재시제' FROM lecture WHERE title = '중등 영어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '2. 현재시제');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '3. 과거시제' FROM lecture WHERE title = '중등 영어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '3. 과거시제');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '4. 미래시제' FROM lecture WHERE title = '중등 영어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '4. 미래시제');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '5. 명사와 대명사' FROM lecture WHERE title = '중등 영어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '5. 명사와 대명사');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '6. 형용사와 부사' FROM lecture WHERE title = '중등 영어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '6. 형용사와 부사');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '7. 완료시제' FROM lecture WHERE title = '중등 영어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '7. 완료시제');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '8. 수동태' FROM lecture WHERE title = '중등 영어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '8. 수동태');

-- 강의 3: 중등 국어
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '1. 문학의 이해' FROM lecture WHERE title = '중등 국어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '1. 문학의 이해');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '2. 시의 이해' FROM lecture WHERE title = '중등 국어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '2. 시의 이해');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '3. 소설의 이해' FROM lecture WHERE title = '중등 국어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '3. 소설의 이해');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '4. 문법 기초' FROM lecture WHERE title = '중등 국어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '4. 문법 기초');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '5. 독서와 이해' FROM lecture WHERE title = '중등 국어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '5. 독서와 이해');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '6. 작문의 기초' FROM lecture WHERE title = '중등 국어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '6. 작문의 기초');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '7. 화법과 표현' FROM lecture WHERE title = '중등 국어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '7. 화법과 표현');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '8. 비문학 독해' FROM lecture WHERE title = '중등 국어' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '8. 비문학 독해');

-- 강의 4: 중등 과학
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '1. 물질의 성질' FROM lecture WHERE title = '중등 과학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '1. 물질의 성질');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '2. 상태 변화' FROM lecture WHERE title = '중등 과학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '2. 상태 변화');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '3. 원소와 화합물' FROM lecture WHERE title = '중등 과학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '3. 원소와 화합물');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '4. 힘과 운동' FROM lecture WHERE title = '중등 과학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '4. 힘과 운동');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '5. 일과 에너지' FROM lecture WHERE title = '중등 과학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '5. 일과 에너지');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '6. 전기와 자기' FROM lecture WHERE title = '중등 과학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '6. 전기와 자기');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '7. 생명의 특성' FROM lecture WHERE title = '중등 과학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '7. 생명의 특성');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '8. 화학 반응' FROM lecture WHERE title = '중등 과학' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '8. 화학 반응');

-- 강의 5: 중등 사회
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '1. 지리와 지도' FROM lecture WHERE title = '중등 사회' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '1. 지리와 지도');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '2. 우리나라의 자연환경' FROM lecture WHERE title = '중등 사회' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '2. 우리나라의 자연환경');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '3. 인구와 도시' FROM lecture WHERE title = '중등 사회' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '3. 인구와 도시');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '4. 경제 활동' FROM lecture WHERE title = '중등 사회' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '4. 경제 활동');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '5. 정치와 시민' FROM lecture WHERE title = '중등 사회' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '5. 정치와 시민');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '6. 법과 사회' FROM lecture WHERE title = '중등 사회' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '6. 법과 사회');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '7. 사회 문제' FROM lecture WHERE title = '중등 사회' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '7. 사회 문제');
INSERT IGNORE INTO unit (lecture_id, name)
SELECT id, '8. 세계의 이해' FROM lecture WHERE title = '중등 사회' AND NOT EXISTS (SELECT 1 FROM unit WHERE lecture_id = lecture.id AND name = '8. 세계의 이해');

-- 9. 시험(Exam) 및 시험 문제(Question, Choice) 생성
-- 각 강의마다 2개의 시험을 생성하고, 각 시험마다 10~20개의 문제를 생성

-- 강의 1: 중등 수학 - 시험 1
INSERT IGNORE INTO exam (lecture_id, title, total_score, status, start_date, end_date, duration_minute, created_at, updated_at)
SELECT id, '중등 수학 1차 시험', 100, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 60, NOW(), NOW()
FROM lecture WHERE title = '중등 수학' AND NOT EXISTS (SELECT 1 FROM exam WHERE lecture_id = lecture.id AND title = '중등 수학 1차 시험');

-- 강의 1: 중등 수학 - 시험 2
INSERT IGNORE INTO exam (lecture_id, title, total_score, status, start_date, end_date, duration_minute, created_at, updated_at)
SELECT id, '중등 수학 2차 시험', 100, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 14 DAY), 60, NOW(), NOW()
FROM lecture WHERE title = '중등 수학' AND NOT EXISTS (SELECT 1 FROM exam WHERE lecture_id = lecture.id AND title = '중등 수학 2차 시험');

-- 강의 2: 중등 영어 - 시험 1
INSERT IGNORE INTO exam (lecture_id, title, total_score, status, start_date, end_date, duration_minute, created_at, updated_at)
SELECT id, '중등 영어 1차 시험', 100, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 60, NOW(), NOW()
FROM lecture WHERE title = '중등 영어' AND NOT EXISTS (SELECT 1 FROM exam WHERE lecture_id = lecture.id AND title = '중등 영어 1차 시험');

-- 강의 2: 중등 영어 - 시험 2
INSERT IGNORE INTO exam (lecture_id, title, total_score, status, start_date, end_date, duration_minute, created_at, updated_at)
SELECT id, '중등 영어 2차 시험', 100, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 14 DAY), 60, NOW(), NOW()
FROM lecture WHERE title = '중등 영어' AND NOT EXISTS (SELECT 1 FROM exam WHERE lecture_id = lecture.id AND title = '중등 영어 2차 시험');

-- 강의 3: 중등 국어 - 시험 1
INSERT IGNORE INTO exam (lecture_id, title, total_score, status, start_date, end_date, duration_minute, created_at, updated_at)
SELECT id, '중등 국어 1차 시험', 100, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 60, NOW(), NOW()
FROM lecture WHERE title = '중등 국어' AND NOT EXISTS (SELECT 1 FROM exam WHERE lecture_id = lecture.id AND title = '중등 국어 1차 시험');

-- 강의 3: 중등 국어 - 시험 2
INSERT IGNORE INTO exam (lecture_id, title, total_score, status, start_date, end_date, duration_minute, created_at, updated_at)
SELECT id, '중등 국어 2차 시험', 100, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 14 DAY), 60, NOW(), NOW()
FROM lecture WHERE title = '중등 국어' AND NOT EXISTS (SELECT 1 FROM exam WHERE lecture_id = lecture.id AND title = '중등 국어 2차 시험');

-- 강의 4: 중등 과학 - 시험 1
INSERT IGNORE INTO exam (lecture_id, title, total_score, status, start_date, end_date, duration_minute, created_at, updated_at)
SELECT id, '중등 과학 1차 시험', 100, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 60, NOW(), NOW()
FROM lecture WHERE title = '중등 과학' AND NOT EXISTS (SELECT 1 FROM exam WHERE lecture_id = lecture.id AND title = '중등 과학 1차 시험');

-- 강의 4: 중등 과학 - 시험 2
INSERT IGNORE INTO exam (lecture_id, title, total_score, status, start_date, end_date, duration_minute, created_at, updated_at)
SELECT id, '중등 과학 2차 시험', 100, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 14 DAY), 60, NOW(), NOW()
FROM lecture WHERE title = '중등 과학' AND NOT EXISTS (SELECT 1 FROM exam WHERE lecture_id = lecture.id AND title = '중등 과학 2차 시험');

-- 강의 5: 중등 사회 - 시험 1
INSERT IGNORE INTO exam (lecture_id, title, total_score, status, start_date, end_date, duration_minute, created_at, updated_at)
SELECT id, '중등 사회 1차 시험', 100, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 60, NOW(), NOW()
FROM lecture WHERE title = '중등 사회' AND NOT EXISTS (SELECT 1 FROM exam WHERE lecture_id = lecture.id AND title = '중등 사회 1차 시험');

-- 강의 5: 중등 사회 - 시험 2
INSERT IGNORE INTO exam (lecture_id, title, total_score, status, start_date, end_date, duration_minute, created_at, updated_at)
SELECT id, '중등 사회 2차 시험', 100, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 14 DAY), 60, NOW(), NOW()
FROM lecture WHERE title = '중등 사회' AND NOT EXISTS (SELECT 1 FROM exam WHERE lecture_id = lecture.id AND title = '중등 사회 2차 시험');

-- 10. 시험 문제(Question) 및 보기(Choice) 생성
-- 각 시험마다 10문제씩 생성하고, 각 문제마다 4개의 보기를 생성

-- 강의: 중등 수학 - 1차 시험 (10문제)
INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '다음 중 자연수는?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 1차 시험' AND u.name = '1. 자연수와 정수'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '다음 중 자연수는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '0', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '다음 중 자연수는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '1', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '다음 중 자연수는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '-1', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '다음 중 자연수는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '0.5', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '다음 중 자연수는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '2 + 3의 값은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 1차 시험' AND u.name = '1. 자연수와 정수'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '2 + 3의 값은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '4', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '2 + 3의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '5', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '2 + 3의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '6', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '2 + 3의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '7', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '2 + 3의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '1/2 + 1/3의 값은?', 3, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 1차 시험' AND u.name = '2. 분수와 소수'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '1/2 + 1/3의 값은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '2/5', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '1/2 + 1/3의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '3/5', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '1/2 + 1/3의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '5/6', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '1/2 + 1/3의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '2/3', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '1/2 + 1/3의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'x + 5 = 10일 때, x의 값은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 1차 시험' AND u.name = '3. 문자와 식'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'x + 5 = 10일 때, x의 값은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '3', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = 'x + 5 = 10일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '5', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = 'x + 5 = 10일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '7', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = 'x + 5 = 10일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '15', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = 'x + 5 = 10일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '2x - 3 = 7일 때, x의 값은?', 3, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 1차 시험' AND u.name = '4. 일차방정식'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '2x - 3 = 7일 때, x의 값은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '2', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '2x - 3 = 7일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '3', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '2x - 3 = 7일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '5', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '2x - 3 = 7일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '7', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '2x - 3 = 7일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '일차함수 y = 2x + 3에서 x = 1일 때 y의 값은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 1차 시험' AND u.name = '5. 일차함수'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '일차함수 y = 2x + 3에서 x = 1일 때 y의 값은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '3', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '일차함수 y = 2x + 3에서 x = 1일 때 y의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '5', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '일차함수 y = 2x + 3에서 x = 1일 때 y의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '7', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '일차함수 y = 2x + 3에서 x = 1일 때 y의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '9', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '일차함수 y = 2x + 3에서 x = 1일 때 y의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'x² - 5x + 6 = 0의 해는?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 1차 시험' AND u.name = '6. 이차방정식'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'x² - 5x + 6 = 0의 해는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'x = 2 또는 x = 3', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = 'x² - 5x + 6 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'x = 1 또는 x = 6', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = 'x² - 5x + 6 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'x = -2 또는 x = -3', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = 'x² - 5x + 6 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '해가 없음', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = 'x² - 5x + 6 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '이차함수 y = x²의 꼭짓점 좌표는?', 3, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 1차 시험' AND u.name = '7. 이차함수'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '이차함수 y = x²의 꼭짓점 좌표는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '(0, 1)', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '이차함수 y = x²의 꼭짓점 좌표는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '(1, 0)', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '이차함수 y = x²의 꼭짓점 좌표는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '(0, 0)', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '이차함수 y = x²의 꼭짓점 좌표는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '(1, 1)', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '이차함수 y = x²의 꼭짓점 좌표는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '직각삼각형에서 빗변의 길이가 5이고 한 변의 길이가 3일 때, 나머지 한 변의 길이는?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 1차 시험' AND u.name = '8. 도형의 기초'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '직각삼각형에서 빗변의 길이가 5이고 한 변의 길이가 3일 때, 나머지 한 변의 길이는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '4', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '직각삼각형에서 빗변의 길이가 5이고 한 변의 길이가 3일 때, 나머지 한 변의 길이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '5', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '직각삼각형에서 빗변의 길이가 5이고 한 변의 길이가 3일 때, 나머지 한 변의 길이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '6', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '직각삼각형에서 빗변의 길이가 5이고 한 변의 길이가 3일 때, 나머지 한 변의 길이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '7', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '직각삼각형에서 빗변의 길이가 5이고 한 변의 길이가 3일 때, 나머지 한 변의 길이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '원의 반지름이 3일 때, 원의 넓이는?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 1차 시험' AND u.name = '8. 도형의 기초'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '원의 반지름이 3일 때, 원의 넓이는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '6π', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '원의 반지름이 3일 때, 원의 넓이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '9π', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '원의 반지름이 3일 때, 원의 넓이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '12π', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '원의 반지름이 3일 때, 원의 넓이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '15π', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 1차 시험' AND q.content = '원의 반지름이 3일 때, 원의 넓이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

-- 강의: 중등 수학 - 2차 시험 (10문제)
INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '일차함수 y = -x + 4의 x절편은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 2차 시험' AND u.name = '5. 일차함수'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '일차함수 y = -x + 4의 x절편은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '4', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '일차함수 y = -x + 4의 x절편은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '-4', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '일차함수 y = -x + 4의 x절편은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '0', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '일차함수 y = -x + 4의 x절편은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '1', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '일차함수 y = -x + 4의 x절편은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'x² + 4x + 3 = 0의 해는?', 2, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 2차 시험' AND u.name = '6. 이차방정식'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'x² + 4x + 3 = 0의 해는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'x = 1 또는 x = 3', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = 'x² + 4x + 3 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'x = -1 또는 x = -3', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = 'x² + 4x + 3 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'x = 2 또는 x = 2', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = 'x² + 4x + 3 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '해가 없음', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = 'x² + 4x + 3 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '이차함수 y = (x-2)² + 1의 꼭짓점 좌표는?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 2차 시험' AND u.name = '7. 이차함수'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '이차함수 y = (x-2)² + 1의 꼭짓점 좌표는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '(2, 1)', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '이차함수 y = (x-2)² + 1의 꼭짓점 좌표는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '(-2, 1)', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '이차함수 y = (x-2)² + 1의 꼭짓점 좌표는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '(2, -1)', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '이차함수 y = (x-2)² + 1의 꼭짓점 좌표는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '(-2, -1)', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '이차함수 y = (x-2)² + 1의 꼭짓점 좌표는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '정사각형의 한 변의 길이가 4일 때, 대각선의 길이는?', 2, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 2차 시험' AND u.name = '8. 도형의 기초'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '정사각형의 한 변의 길이가 4일 때, 대각선의 길이는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '4', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '정사각형의 한 변의 길이가 4일 때, 대각선의 길이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '4√2', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '정사각형의 한 변의 길이가 4일 때, 대각선의 길이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '8', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '정사각형의 한 변의 길이가 4일 때, 대각선의 길이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '8√2', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '정사각형의 한 변의 길이가 4일 때, 대각선의 길이는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '(-3) + 5의 값은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 2차 시험' AND u.name = '1. 자연수와 정수'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '(-3) + 5의 값은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '-8', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '(-3) + 5의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '2', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '(-3) + 5의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '8', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '(-3) + 5의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '-2', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '(-3) + 5의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '0.75를 분수로 나타내면?', 3, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 2차 시험' AND u.name = '2. 분수와 소수'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '0.75를 분수로 나타내면?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '1/4', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '0.75를 분수로 나타내면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '1/2', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '0.75를 분수로 나타내면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '3/4', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '0.75를 분수로 나타내면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '4/5', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '0.75를 분수로 나타내면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '3x = 15일 때, x의 값은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 2차 시험' AND u.name = '3. 문자와 식'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '3x = 15일 때, x의 값은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '3', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '3x = 15일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '5', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '3x = 15일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '12', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '3x = 15일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '18', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '3x = 15일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '3x + 2 = 11일 때, x의 값은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 2차 시험' AND u.name = '4. 일차방정식'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '3x + 2 = 11일 때, x의 값은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '3', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '3x + 2 = 11일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '4', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '3x + 2 = 11일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '5', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '3x + 2 = 11일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '6', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '3x + 2 = 11일 때, x의 값은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '일차함수 y = 3x - 2의 y절편은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 2차 시험' AND u.name = '5. 일차함수'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '일차함수 y = 3x - 2의 y절편은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '3', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '일차함수 y = 3x - 2의 y절편은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '-3', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '일차함수 y = 3x - 2의 y절편은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '2', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '일차함수 y = 3x - 2의 y절편은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '-2', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = '일차함수 y = 3x - 2의 y절편은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'x² - 9 = 0의 해는?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 수학 2차 시험' AND u.name = '6. 이차방정식'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'x² - 9 = 0의 해는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'x = 3 또는 x = -3', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = 'x² - 9 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'x = 9 또는 x = -9', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = 'x² - 9 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'x = 0', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = 'x² - 9 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '해가 없음', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 수학 2차 시험' AND q.content = 'x² - 9 = 0의 해는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

-- 강의: 중등 영어 - 1차 시험 (10문제)
INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '다음 중 인사 표현이 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 1차 시험' AND u.name = '1. 인사와 자기소개'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '다음 중 인사 표현이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Hello', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '다음 중 인사 표현이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Hi', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '다음 중 인사 표현이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Goodbye', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '다음 중 인사 표현이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Apple', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '다음 중 인사 표현이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '"안녕하세요"를 영어로 옮기면?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 1차 시험' AND u.name = '1. 인사와 자기소개'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '"안녕하세요"를 영어로 옮기면?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Hello', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '"안녕하세요"를 영어로 옮기면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Goodbye', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '"안녕하세요"를 영어로 옮기면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Thank you', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '"안녕하세요"를 영어로 옮기면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Sorry', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '"안녕하세요"를 영어로 옮기면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'I _____ to school every day. 빈칸에 들어갈 말은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 1차 시험' AND u.name = '2. 현재시제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'I _____ to school every day. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'goes', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ to school every day. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'go', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ to school every day. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'went', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ to school every day. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'going', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ to school every day. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'She _____ English well. 빈칸에 들어갈 말은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 1차 시험' AND u.name = '2. 현재시제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'She _____ English well. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'speaks', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'She _____ English well. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'speak', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'She _____ English well. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'speaking', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'She _____ English well. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'spoke', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'She _____ English well. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'I _____ to the park yesterday. 빈칸에 들어갈 말은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 1차 시험' AND u.name = '3. 과거시제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'I _____ to the park yesterday. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'go', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ to the park yesterday. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'went', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ to the park yesterday. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'goes', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ to the park yesterday. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'going', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ to the park yesterday. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'They _____ a movie last night. 빈칸에 들어갈 말은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 1차 시험' AND u.name = '3. 과거시제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'They _____ a movie last night. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'watched', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'They _____ a movie last night. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'watch', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'They _____ a movie last night. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'watching', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'They _____ a movie last night. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'watches', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'They _____ a movie last night. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'I _____ visit my friend tomorrow. 빈칸에 들어갈 말은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 1차 시험' AND u.name = '4. 미래시제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'I _____ visit my friend tomorrow. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'am', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ visit my friend tomorrow. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'will', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ visit my friend tomorrow. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'was', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ visit my friend tomorrow. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'were', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ visit my friend tomorrow. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '다음 중 대명사가 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 1차 시험' AND u.name = '5. 명사와 대명사'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '다음 중 대명사가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'I', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '다음 중 대명사가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'you', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '다음 중 대명사가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'he', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '다음 중 대명사가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'book', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = '다음 중 대명사가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'She is very _____. 빈칸에 들어갈 형용사는?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 1차 시험' AND u.name = '6. 형용사와 부사'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'She is very _____. 빈칸에 들어갈 형용사는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'beautiful', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'She is very _____. 빈칸에 들어갈 형용사는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'beautifully', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'She is very _____. 빈칸에 들어갈 형용사는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'beauty', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'She is very _____. 빈칸에 들어갈 형용사는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'beautify', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'She is very _____. 빈칸에 들어갈 형용사는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'I _____ finished my homework. 빈칸에 들어갈 말은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 1차 시험' AND u.name = '7. 완료시제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'I _____ finished my homework. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'have', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ finished my homework. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'has', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ finished my homework. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'had', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ finished my homework. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'having', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 1차 시험' AND q.content = 'I _____ finished my homework. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

-- 강의: 중등 영어 - 2차 시험 (10문제)
INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'We _____ going to the beach next week. 빈칸에 들어갈 말은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 2차 시험' AND u.name = '4. 미래시제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'We _____ going to the beach next week. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'are', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'We _____ going to the beach next week. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'is', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'We _____ going to the beach next week. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'was', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'We _____ going to the beach next week. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'were', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'We _____ going to the beach next week. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'This is _____ book. 빈칸에 들어갈 말은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 2차 시험' AND u.name = '5. 명사와 대명사'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'This is _____ book. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'I', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'This is _____ book. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'my', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'This is _____ book. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'me', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'This is _____ book. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'mine', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'This is _____ book. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'He runs very _____. 빈칸에 들어갈 부사는?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 2차 시험' AND u.name = '6. 형용사와 부사'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'He runs very _____. 빈칸에 들어갈 부사는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'fast', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'He runs very _____. 빈칸에 들어갈 부사는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'fastly', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'He runs very _____. 빈칸에 들어갈 부사는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'faster', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'He runs very _____. 빈칸에 들어갈 부사는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'fastest', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'He runs very _____. 빈칸에 들어갈 부사는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'She _____ lived here for five years. 빈칸에 들어갈 말은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 2차 시험' AND u.name = '7. 완료시제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'She _____ lived here for five years. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'has', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'She _____ lived here for five years. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'have', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'She _____ lived here for five years. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'had', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'She _____ lived here for five years. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'having', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'She _____ lived here for five years. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'The book _____ written by him. 빈칸에 들어갈 말은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 2차 시험' AND u.name = '8. 수동태'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'The book _____ written by him. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'was', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'The book _____ written by him. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'is', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'The book _____ written by him. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'are', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'The book _____ written by him. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'were', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'The book _____ written by him. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '"만나서 반갑습니다"를 영어로 옮기면?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 2차 시험' AND u.name = '1. 인사와 자기소개'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '"만나서 반갑습니다"를 영어로 옮기면?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Goodbye', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = '"만나서 반갑습니다"를 영어로 옮기면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Nice to meet you', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = '"만나서 반갑습니다"를 영어로 옮기면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Thank you', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = '"만나서 반갑습니다"를 영어로 옮기면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'Sorry', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = '"만나서 반갑습니다"를 영어로 옮기면?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'We _____ soccer on weekends. 빈칸에 들어갈 말은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 2차 시험' AND u.name = '2. 현재시제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'We _____ soccer on weekends. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'plays', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'We _____ soccer on weekends. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'play', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'We _____ soccer on weekends. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'played', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'We _____ soccer on weekends. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'playing', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'We _____ soccer on weekends. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'He _____ breakfast this morning. 빈칸에 들어갈 말은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 2차 시험' AND u.name = '3. 과거시제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'He _____ breakfast this morning. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'ate', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'He _____ breakfast this morning. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'eat', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'He _____ breakfast this morning. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'eats', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'He _____ breakfast this morning. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'eating', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'He _____ breakfast this morning. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'These are _____ pencils. 빈칸에 들어갈 말은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 2차 시험' AND u.name = '5. 명사와 대명사'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'These are _____ pencils. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'I', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'These are _____ pencils. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'our', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'These are _____ pencils. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'we', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'These are _____ pencils. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'us', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'These are _____ pencils. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, 'The letter _____ delivered yesterday. 빈칸에 들어갈 말은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 영어 2차 시험' AND u.name = '8. 수동태'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = 'The letter _____ delivered yesterday. 빈칸에 들어갈 말은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'was', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'The letter _____ delivered yesterday. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'is', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'The letter _____ delivered yesterday. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'are', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'The letter _____ delivered yesterday. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'were', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 영어 2차 시험' AND q.content = 'The letter _____ delivered yesterday. 빈칸에 들어갈 말은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

-- 강의: 중등 국어 - 1차 시험 (10문제)
INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '다음 중 문학의 기능이 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 1차 시험' AND u.name = '1. 문학의 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '다음 중 문학의 기능이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '감정 표현', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '다음 중 문학의 기능이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '사상 전달', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '다음 중 문학의 기능이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '미적 체험', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '다음 중 문학의 기능이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '수학 계산', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '다음 중 문학의 기능이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '시에서 운율을 만드는 요소는?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 1차 시험' AND u.name = '2. 시의 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '시에서 운율을 만드는 요소는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '율격', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '시에서 운율을 만드는 요소는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '어휘', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '시에서 운율을 만드는 요소는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '문법', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '시에서 운율을 만드는 요소는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '맞춤법', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '시에서 운율을 만드는 요소는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '소설의 3요소가 아닌 것은?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 1차 시험' AND u.name = '3. 소설의 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '소설의 3요소가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '인물', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '소설의 3요소가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '배경', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '소설의 3요소가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '사건', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '소설의 3요소가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '수식', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '소설의 3요소가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '다음 중 주어가 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 1차 시험' AND u.name = '4. 문법 기초'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '다음 중 주어가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '나는', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '다음 중 주어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '너는', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '다음 중 주어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '그는', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '다음 중 주어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '책을', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '다음 중 주어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '독서의 목적이 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 1차 시험' AND u.name = '5. 독서와 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '독서의 목적이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '지식 습득', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '독서의 목적이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '사고력 향상', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '독서의 목적이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '상상력 확장', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '독서의 목적이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '게임하기', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '독서의 목적이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '작문의 3단계가 아닌 것은?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 1차 시험' AND u.name = '6. 작문의 기초'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '작문의 3단계가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '계획', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '작문의 3단계가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '초고', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '작문의 3단계가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '수정', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '작문의 3단계가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '게임', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '작문의 3단계가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '공식적인 자리에서 사용하는 말투는?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 1차 시험' AND u.name = '7. 화법과 표현'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '공식적인 자리에서 사용하는 말투는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '격식체', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '공식적인 자리에서 사용하는 말투는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '비격식체', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '공식적인 자리에서 사용하는 말투는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '구어체', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '공식적인 자리에서 사용하는 말투는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '문어체', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '공식적인 자리에서 사용하는 말투는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '비문학 독해에서 중요한 것은?', 2, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 1차 시험' AND u.name = '8. 비문학 독해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '비문학 독해에서 중요한 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '감정', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '비문학 독해에서 중요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '논리', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '비문학 독해에서 중요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '상상', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '비문학 독해에서 중요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '추측', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '비문학 독해에서 중요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '문학 작품을 감상할 때 필요한 것은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 1차 시험' AND u.name = '1. 문학의 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '문학 작품을 감상할 때 필요한 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '상상력', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '문학 작품을 감상할 때 필요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '계산력', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '문학 작품을 감상할 때 필요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '운동력', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '문학 작품을 감상할 때 필요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '기억력', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '문학 작품을 감상할 때 필요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '시에서 사용하는 표현 기법은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 1차 시험' AND u.name = '2. 시의 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '시에서 사용하는 표현 기법은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '비유', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '시에서 사용하는 표현 기법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '수식', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '시에서 사용하는 표현 기법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '계산', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '시에서 사용하는 표현 기법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '측정', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 1차 시험' AND q.content = '시에서 사용하는 표현 기법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

-- 강의: 중등 국어 - 2차 시험 (10문제)
INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '소설에서 인물의 성격을 드러내는 방법은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 2차 시험' AND u.name = '3. 소설의 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '소설에서 인물의 성격을 드러내는 방법은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '행동', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '소설에서 인물의 성격을 드러내는 방법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '수식', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '소설에서 인물의 성격을 드러내는 방법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '계산', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '소설에서 인물의 성격을 드러내는 방법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '측정', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '소설에서 인물의 성격을 드러내는 방법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '다음 중 서술어가 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 2차 시험' AND u.name = '4. 문법 기초'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '다음 중 서술어가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '간다', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '다음 중 서술어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '먹는다', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '다음 중 서술어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '읽는다', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '다음 중 서술어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '책을', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '다음 중 서술어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '효과적인 독서 방법은?', 2, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 2차 시험' AND u.name = '5. 독서와 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '효과적인 독서 방법은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '빠르게 읽기', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '효과적인 독서 방법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '비판적 읽기', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '효과적인 독서 방법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '무작정 읽기', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '효과적인 독서 방법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '건너뛰기', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '효과적인 독서 방법은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '좋은 글의 조건이 아닌 것은?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 2차 시험' AND u.name = '6. 작문의 기초'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '좋은 글의 조건이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '명확성', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '좋은 글의 조건이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '일관성', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '좋은 글의 조건이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '간결성', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '좋은 글의 조건이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '복잡성', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '좋은 글의 조건이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '대화에서 중요한 것은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 2차 시험' AND u.name = '7. 화법과 표현'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '대화에서 중요한 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '경청', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '대화에서 중요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '독백', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '대화에서 중요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '침묵', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '대화에서 중요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '무시', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '대화에서 중요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '비문학 텍스트를 읽을 때 필요한 것은?', 2, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 2차 시험' AND u.name = '8. 비문학 독해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '비문학 텍스트를 읽을 때 필요한 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '감정', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '비문학 텍스트를 읽을 때 필요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '분석', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '비문학 텍스트를 읽을 때 필요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '추측', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '비문학 텍스트를 읽을 때 필요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '상상', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '비문학 텍스트를 읽을 때 필요한 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '문학 작품의 가치는?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 2차 시험' AND u.name = '1. 문학의 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '문학 작품의 가치는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '인간 이해', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '문학 작품의 가치는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '수학 계산', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '문학 작품의 가치는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '운동 능력', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '문학 작품의 가치는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '기억력', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '문학 작품의 가치는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '시에서 리듬을 만드는 요소는?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 2차 시험' AND u.name = '2. 시의 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '시에서 리듬을 만드는 요소는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '음보', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '시에서 리듬을 만드는 요소는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '어휘', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '시에서 리듬을 만드는 요소는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '문법', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '시에서 리듬을 만드는 요소는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '맞춤법', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '시에서 리듬을 만드는 요소는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '다음 중 목적어가 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 2차 시험' AND u.name = '4. 문법 기초'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '다음 중 목적어가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '책을', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '다음 중 목적어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '밥을', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '다음 중 목적어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '물을', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '다음 중 목적어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '나는', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '다음 중 목적어가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '독서 후 해야 할 일은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 국어 2차 시험' AND u.name = '5. 독서와 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '독서 후 해야 할 일은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '정리', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '독서 후 해야 할 일은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '잊기', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '독서 후 해야 할 일은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '무시', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '독서 후 해야 할 일은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '건너뛰기', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 국어 2차 시험' AND q.content = '독서 후 해야 할 일은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

-- 강의: 중등 과학 - 1차 시험 (10문제)
INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '다음 중 물질의 성질이 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 1차 시험' AND u.name = '1. 물질의 성질'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '다음 중 물질의 성질이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '색깔', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '다음 중 물질의 성질이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '냄새', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '다음 중 물질의 성질이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '맛', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '다음 중 물질의 성질이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '가격', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '다음 중 물질의 성질이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '물질의 상태가 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 1차 시험' AND u.name = '1. 물질의 성질'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '물질의 상태가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '고체', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '물질의 상태가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '액체', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '물질의 상태가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '기체', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '물질의 상태가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '플라즈마', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '물질의 상태가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '고체가 액체로 변하는 현상은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 1차 시험' AND u.name = '2. 상태 변화'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '고체가 액체로 변하는 현상은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '용해', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '고체가 액체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '응결', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '고체가 액체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '승화', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '고체가 액체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '응고', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '고체가 액체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '다음 중 원소가 아닌 것은?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 1차 시험' AND u.name = '3. 원소와 화합물'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '다음 중 원소가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '수소', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '다음 중 원소가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '산소', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '다음 중 원소가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '질소', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '다음 중 원소가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '물', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '다음 중 원소가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '물체에 작용하는 힘의 단위는?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 1차 시험' AND u.name = '4. 힘과 운동'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '물체에 작용하는 힘의 단위는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'kg', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '물체에 작용하는 힘의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'N', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '물체에 작용하는 힘의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'm', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '물체에 작용하는 힘의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 's', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '물체에 작용하는 힘의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '일의 단위는?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 1차 시험' AND u.name = '5. 일과 에너지'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '일의 단위는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'J', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '일의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'N', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '일의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'W', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '일의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'V', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '일의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '전류의 단위는?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 1차 시험' AND u.name = '6. 전기와 자기'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '전류의 단위는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'A', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '전류의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'V', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '전류의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'W', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '전류의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'J', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '전류의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '생물의 특징이 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 1차 시험' AND u.name = '7. 생명의 특성'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '생물의 특징이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '호흡', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '생물의 특징이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '번식', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '생물의 특징이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '성장', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '생물의 특징이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '광합성', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '생물의 특징이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '화학 반응에서 변하지 않는 것은?', 2, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 1차 시험' AND u.name = '8. 화학 반응'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '화학 반응에서 변하지 않는 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '원소', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '화학 반응에서 변하지 않는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '원자', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '화학 반응에서 변하지 않는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '분자', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '화학 반응에서 변하지 않는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '화합물', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '화학 반응에서 변하지 않는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '액체가 기체로 변하는 현상은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 1차 시험' AND u.name = '2. 상태 변화'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '액체가 기체로 변하는 현상은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '증발', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '액체가 기체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '응결', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '액체가 기체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '승화', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '액체가 기체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '응고', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 1차 시험' AND q.content = '액체가 기체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

-- 강의: 중등 과학 - 2차 시험 (10문제)
INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '다음 중 화합물이 아닌 것은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 2차 시험' AND u.name = '3. 원소와 화합물'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '다음 중 화합물이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '수소', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '다음 중 화합물이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '물', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '다음 중 화합물이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '이산화탄소', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '다음 중 화합물이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '소금', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '다음 중 화합물이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '뉴턴의 제1법칙은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 2차 시험' AND u.name = '4. 힘과 운동'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '뉴턴의 제1법칙은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '관성의 법칙', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '뉴턴의 제1법칙은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '작용 반작용', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '뉴턴의 제1법칙은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '가속도의 법칙', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '뉴턴의 제1법칙은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '만유인력', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '뉴턴의 제1법칙은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '에너지의 종류가 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 2차 시험' AND u.name = '5. 일과 에너지'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '에너지의 종류가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '운동 에너지', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '에너지의 종류가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '위치 에너지', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '에너지의 종류가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '열 에너지', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '에너지의 종류가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '시간 에너지', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '에너지의 종류가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '전압의 단위는?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 2차 시험' AND u.name = '6. 전기와 자기'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '전압의 단위는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'A', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '전압의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'V', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '전압의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'W', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '전압의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'J', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '전압의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '식물이 햇빛을 이용하여 양분을 만드는 과정은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 2차 시험' AND u.name = '7. 생명의 특성'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '식물이 햇빛을 이용하여 양분을 만드는 과정은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '광합성', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '식물이 햇빛을 이용하여 양분을 만드는 과정은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '호흡', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '식물이 햇빛을 이용하여 양분을 만드는 과정은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '번식', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '식물이 햇빛을 이용하여 양분을 만드는 과정은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '성장', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '식물이 햇빛을 이용하여 양분을 만드는 과정은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '산소와 결합하는 반응은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 2차 시험' AND u.name = '8. 화학 반응'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '산소와 결합하는 반응은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '산화', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '산소와 결합하는 반응은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '환원', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '산소와 결합하는 반응은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '중화', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '산소와 결합하는 반응은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '분해', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '산소와 결합하는 반응은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '물질의 밀도 단위는?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 2차 시험' AND u.name = '1. 물질의 성질'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '물질의 밀도 단위는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'kg', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '물질의 밀도 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'kg/m³', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '물질의 밀도 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'm', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '물질의 밀도 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 's', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '물질의 밀도 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '기체가 액체로 변하는 현상은?', 2, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 2차 시험' AND u.name = '2. 상태 변화'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '기체가 액체로 변하는 현상은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '용해', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '기체가 액체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '응결', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '기체가 액체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '승화', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '기체가 액체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '응고', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '기체가 액체로 변하는 현상은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '뉴턴의 제2법칙은?', 3, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 2차 시험' AND u.name = '4. 힘과 운동'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '뉴턴의 제2법칙은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '관성의 법칙', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '뉴턴의 제2법칙은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '작용 반작용', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '뉴턴의 제2법칙은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '가속도의 법칙', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '뉴턴의 제2법칙은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '만유인력', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '뉴턴의 제2법칙은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '일률의 단위는?', 3, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 과학 2차 시험' AND u.name = '5. 일과 에너지'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '일률의 단위는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'J', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '일률의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'N', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '일률의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'W', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '일률의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'V', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 과학 2차 시험' AND q.content = '일률의 단위는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

-- 강의: 중등 사회 - 1차 시험 (10문제)
INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '지도의 방위 중 북쪽을 나타내는 것은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 1차 시험' AND u.name = '1. 지리와 지도'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '지도의 방위 중 북쪽을 나타내는 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'N', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '지도의 방위 중 북쪽을 나타내는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'S', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '지도의 방위 중 북쪽을 나타내는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'E', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '지도의 방위 중 북쪽을 나타내는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'W', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '지도의 방위 중 북쪽을 나타내는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '한국의 기후 특징은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 1차 시험' AND u.name = '2. 우리나라의 자연환경'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '한국의 기후 특징은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '계절풍', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '한국의 기후 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '무역풍', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '한국의 기후 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '편서풍', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '한국의 기후 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '극동풍', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '한국의 기후 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '인구 밀도가 높은 지역은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 1차 시험' AND u.name = '3. 인구와 도시'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '인구 밀도가 높은 지역은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '도시', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '인구 밀도가 높은 지역은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '농촌', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '인구 밀도가 높은 지역은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '산지', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '인구 밀도가 높은 지역은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '해안', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '인구 밀도가 높은 지역은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '경제의 3주체가 아닌 것은?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 1차 시험' AND u.name = '4. 경제 활동'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '경제의 3주체가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '가계', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '경제의 3주체가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '기업', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '경제의 3주체가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '정부', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '경제의 3주체가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '학교', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '경제의 3주체가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '민주주의의 원칙이 아닌 것은?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 1차 시험' AND u.name = '5. 정치와 시민'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '민주주의의 원칙이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '다수결', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '민주주의의 원칙이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '소수 보호', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '민주주의의 원칙이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '자유', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '민주주의의 원칙이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '독재', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '민주주의의 원칙이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '법의 목적이 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 1차 시험' AND u.name = '6. 법과 사회'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '법의 목적이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '질서 유지', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '법의 목적이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '정의 실현', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '법의 목적이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '권리 보호', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '법의 목적이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '혼란 조장', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '법의 목적이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '현대 사회의 문제가 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 1차 시험' AND u.name = '7. 사회 문제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '현대 사회의 문제가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '환경', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '현대 사회의 문제가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '빈부격차', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '현대 사회의 문제가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '인권', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '현대 사회의 문제가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '자연재해', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '현대 사회의 문제가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '세계의 대륙이 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 1차 시험' AND u.name = '8. 세계의 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '세계의 대륙이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '아시아', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '세계의 대륙이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '유럽', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '세계의 대륙이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '아프리카', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '세계의 대륙이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '태평양', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '세계의 대륙이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '지도의 축척이 작을수록 나타나는 지역은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 1차 시험' AND u.name = '1. 지리와 지도'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '지도의 축척이 작을수록 나타나는 지역은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '넓은 지역', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '지도의 축척이 작을수록 나타나는 지역은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '좁은 지역', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '지도의 축척이 작을수록 나타나는 지역은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '상세한 지역', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '지도의 축척이 작을수록 나타나는 지역은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '작은 지역', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '지도의 축척이 작을수록 나타나는 지역은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '한국의 지형 특징은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 1차 시험' AND u.name = '2. 우리나라의 자연환경'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '한국의 지형 특징은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '산지가 많음', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '한국의 지형 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '평지가 많음', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '한국의 지형 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '사막이 많음', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '한국의 지형 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '빙하가 많음', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 1차 시험' AND q.content = '한국의 지형 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

-- 강의: 중등 사회 - 2차 시험 (10문제)
INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '도시화의 문제점이 아닌 것은?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 2차 시험' AND u.name = '3. 인구와 도시'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '도시화의 문제점이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '주택 부족', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '도시화의 문제점이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '교통 혼잡', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '도시화의 문제점이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '환경 오염', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '도시화의 문제점이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '농업 발전', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '도시화의 문제점이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '시장 경제의 특징은?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 2차 시험' AND u.name = '4. 경제 활동'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '시장 경제의 특징은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '경쟁', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '시장 경제의 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '독점', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '시장 경제의 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '통제', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '시장 경제의 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '제한', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '시장 경제의 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '시민의 권리가 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 2차 시험' AND u.name = '5. 정치와 시민'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '시민의 권리가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '참정권', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '시민의 권리가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '자유권', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '시민의 권리가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '평등권', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '시민의 권리가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '독재권', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '시민의 권리가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '헌법의 특징이 아닌 것은?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 2차 시험' AND u.name = '6. 법과 사회'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '헌법의 특징이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '최고 법규', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '헌법의 특징이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '국가 기본법', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '헌법의 특징이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '국민의 권리', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '헌법의 특징이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '임의 변경', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '헌법의 특징이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '환경 문제의 원인이 아닌 것은?', 4, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 2차 시험' AND u.name = '7. 사회 문제'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '환경 문제의 원인이 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '공업화', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '환경 문제의 원인이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '도시화', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '환경 문제의 원인이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '인구 증가', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '환경 문제의 원인이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '자연 보호', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '환경 문제의 원인이 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '국제 기구가 아닌 것은?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 2차 시험' AND u.name = '8. 세계의 이해'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '국제 기구가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'UN', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '국제 기구가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'WTO', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '국제 기구가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'EU', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '국제 기구가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '학교', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '국제 기구가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '지도의 방위 중 동쪽을 나타내는 것은?', 3, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 2차 시험' AND u.name = '1. 지리와 지도'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '지도의 방위 중 동쪽을 나타내는 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'N', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '지도의 방위 중 동쪽을 나타내는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'S', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '지도의 방위 중 동쪽을 나타내는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'E', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '지도의 방위 중 동쪽을 나타내는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, 'W', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '지도의 방위 중 동쪽을 나타내는 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '한국의 강수량 특징은?', 1, 10, u.id, 'EASY'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 2차 시험' AND u.name = '2. 우리나라의 자연환경'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '한국의 강수량 특징은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '여름에 많음', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '한국의 강수량 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '겨울에 많음', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '한국의 강수량 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '봄에 많음', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '한국의 강수량 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '가을에 많음', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '한국의 강수량 특징은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '경제의 기본 문제가 아닌 것은?', 4, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 2차 시험' AND u.name = '4. 경제 활동'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '경제의 기본 문제가 아닌 것은?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '생산', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '경제의 기본 문제가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '분배', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '경제의 기본 문제가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '소비', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '경제의 기본 문제가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '파괴', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '경제의 기본 문제가 아닌 것은?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

INSERT IGNORE INTO question (exam_id, content, answer_number, score, unit_id, difficulty)
SELECT e.id, '민주주의의 기본 원리는?', 1, 10, u.id, 'MEDIUM'
FROM exam e JOIN lecture l ON e.lecture_id = l.id JOIN unit u ON u.lecture_id = l.id
WHERE e.title = '중등 사회 2차 시험' AND u.name = '5. 정치와 시민'
AND NOT EXISTS (SELECT 1 FROM question WHERE exam_id = e.id AND content = '민주주의의 기본 원리는?');

INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '국민 주권', 1 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '민주주의의 기본 원리는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 1);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '군주 주권', 2 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '민주주의의 기본 원리는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 2);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '정당 주권', 3 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '민주주의의 기본 원리는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 3);
INSERT IGNORE INTO choice (question_id, content, choice_number)
SELECT q.id, '기업 주권', 4 FROM question q JOIN exam e ON q.exam_id = e.id WHERE e.title = '중등 사회 2차 시험' AND q.content = '민주주의의 기본 원리는?' AND NOT EXISTS (SELECT 1 FROM choice WHERE question_id = q.id AND choice_number = 4);

-- 11. 학생 시험 응시 및 채점 완료 데이터 생성
-- 각 시험마다 수강생들이 이미 시험을 응시하고 채점까지 완료된 상태로 생성
-- 학생마다 다른 정답률 (5문제, 6문제, 7문제 맞춤, 다 맞춤, 다 틀림 등)

-- 각 시험마다 수강생들의 시험 응시 기록 생성
-- 응시율 약 90% (10~15%는 응시하지 않은 상태)
-- earned_score는 답안 생성 후 서브쿼리로 계산하여 업데이트
INSERT IGNORE INTO exam_student (exam_id, user_id, earned_score, status, exam_started_at, submitted_at)
SELECT 
    e.id as exam_id,
    ls.user_id,
    0 as earned_score,  -- 임시값, 답안 생성 후 실제 점수로 업데이트됨
    'GRADED' as status,
    DATE_SUB(NOW(), INTERVAL 2 DAY) as exam_started_at,
    DATE_SUB(NOW(), INTERVAL 1 DAY) as submitted_at
FROM exam e
JOIN lecture_student ls ON ls.lecture_id = e.lecture_id
WHERE e.status = 'PUBLISHED'
AND NOT EXISTS (
    SELECT 1 FROM exam_student es 
    WHERE es.exam_id = e.id AND es.user_id = ls.user_id
)
-- 응시율 약 90%로 제한
-- 학생 ID와 시험 ID를 조합하여 일관되게 응시 여부 결정
-- MOD(ls.user_id + e.id, 10) < 9로 약 90%만 응시 (10%는 응시하지 않음)
-- 각 시험마다 다른 학생이 응시하지 않도록 시험 ID를 포함
AND MOD(ls.user_id + e.id, 10) < 9;

-- 각 학생의 시험 답안 생성 (정답/오답 배치)
-- 학생 ID와 문제 ID를 조합해서 정답 여부 결정 (학생마다 다른 정답률)
INSERT IGNORE INTO exam_student_answer (
    exam_id, user_id, question_id, unit_id, difficulty, 
    submitted_answer_number, is_correct, earned_score
)
SELECT 
    es.exam_id,
    es.user_id,
    q.id as question_id,
    q.unit_id,
    q.difficulty,
    -- 학생 ID와 문제 ID를 조합해서 정답 여부 결정
    -- MOD(es.user_id + q.id, 10) < MOD(es.user_id, 11)로 정답 개수 조절
    CASE 
        -- 정답인 경우: 정답 번호 선택
        WHEN MOD(es.user_id + q.id, 10) < MOD(es.user_id, 11) 
        THEN q.answer_number
        -- 오답인 경우: 정답이 아닌 다른 번호 선택
        ELSE CASE 
            WHEN q.answer_number = 1 THEN 2
            WHEN q.answer_number = 2 THEN 3
            WHEN q.answer_number = 3 THEN 4
            WHEN q.answer_number = 4 THEN 1
            ELSE 1
        END
    END as submitted_answer_number,
    -- 정답 여부
    CASE 
        WHEN MOD(es.user_id + q.id, 10) < MOD(es.user_id, 11) 
        THEN 1  -- 정답
        ELSE 0  -- 오답
    END as is_correct,
    -- 획득 점수
    CASE 
        WHEN MOD(es.user_id + q.id, 10) < MOD(es.user_id, 11) 
        THEN q.score  -- 정답이면 문제 점수
        ELSE 0  -- 오답이면 0점
    END as earned_score
FROM exam_student es
JOIN exam e ON e.id = es.exam_id
JOIN question q ON q.exam_id = e.id
WHERE es.status = 'GRADED'
AND NOT EXISTS (
    SELECT 1 FROM exam_student_answer esa
    WHERE esa.exam_id = es.exam_id 
    AND esa.user_id = es.user_id 
    AND esa.question_id = q.id
);

-- exam_student의 earned_score를 실제 답안 점수 합계로 업데이트
-- Safe update mode를 일시적으로 비활성화하여 UPDATE 실행
SET SQL_SAFE_UPDATES = 0;

UPDATE exam_student es
INNER JOIN (
    SELECT exam_id, user_id, COALESCE(SUM(earned_score), 0) as total_score
    FROM exam_student_answer
    GROUP BY exam_id, user_id
) score_sum ON score_sum.exam_id = es.exam_id AND score_sum.user_id = es.user_id
SET es.earned_score = score_sum.total_score
WHERE es.exam_id = score_sum.exam_id 
AND es.user_id = score_sum.user_id;

-- Safe update mode 다시 활성화
SET SQL_SAFE_UPDATES = 1;

