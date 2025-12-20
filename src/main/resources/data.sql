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

-- 6. Lecture 생성 (Teacher마다 3개씩, 총 15개)
-- teacher1 선생님 강의 3개
INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 수학 기초반', 'teacher1 선생님의 중등 수학 기초반입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher1' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 수학 기초반');

INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 수학 심화반', 'teacher1 선생님의 중등 수학 심화반입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher1' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 수학 심화반');

INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '고등 수학 I', 'teacher1 선생님의 고등 수학 I 강의입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher1' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '고등 수학 I');

-- teacher2 선생님 강의 3개
INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 영어 기초반', 'teacher2 선생님의 중등 영어 기초반입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher2' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 영어 기초반');

INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 영어 심화반', 'teacher2 선생님의 중등 영어 심화반입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher2' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 영어 심화반');

INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '고등 영어 독해', 'teacher2 선생님의 고등 영어 독해 강의입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher2' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '고등 영어 독해');

-- teacher3 선생님 강의 3개
INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 국어 기초반', 'teacher3 선생님의 중등 국어 기초반입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher3' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 국어 기초반');

INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 국어 심화반', 'teacher3 선생님의 중등 국어 심화반입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher3' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 국어 심화반');

INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '고등 문학', 'teacher3 선생님의 고등 문학 강의입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher3' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '고등 문학');

-- teacher4 선생님 강의 3개
INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 과학 기초반', 'teacher4 선생님의 중등 과학 기초반입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher4' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 과학 기초반');

INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 과학 심화반', 'teacher4 선생님의 중등 과학 심화반입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher4' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 과학 심화반');

INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '고등 화학 I', 'teacher4 선생님의 고등 화학 I 강의입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher4' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '고등 화학 I');

-- teacher5 선생님 강의 3개
INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 사회 기초반', 'teacher5 선생님의 중등 사회 기초반입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher5' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 사회 기초반');

INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '중등 사회 심화반', 'teacher5 선생님의 중등 사회 심화반입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher5' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '중등 사회 심화반');

INSERT IGNORE INTO lecture (academy_id, teacher_id, title, description, start_date, end_date, created_at)
SELECT a.id, u.id, '고등 한국사', 'teacher5 선생님의 고등 한국사 강의입니다.', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW()
FROM users u, academy a WHERE u.login_id = 'teacher5' AND a.code = 'EDU-0001'
AND NOT EXISTS (SELECT 1 FROM lecture WHERE teacher_id = u.id AND title = '고등 한국사');

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

-- 7. 학생-강의 할당 (각 강의마다 20~30명의 학생 할당)
-- 학생 280명(student1 ~ student280)을 사용하여 할당하고, 20명(student281 ~ student300)은 할당하지 않음
-- 각 강의마다 20~30명의 학생을 랜덤하게 할당 (학생은 여러 강의에 등록 가능)

-- 강의별 학생 할당 (각 강의마다 20~30명 랜덤하게 할당)
INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 0) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 25;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 1) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 22;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 2) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 28;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 3) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 24;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 4) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 26;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 5) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 23;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 6) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 29;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 7) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 21;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 8) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 27;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 9) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 30;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 10) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 25;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 11) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 22;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 12) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 28;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 13) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 24;

INSERT IGNORE INTO lecture_student (lecture_id, user_id, created_at)
SELECT 
    (SELECT id FROM lecture ORDER BY id LIMIT 1 OFFSET 14) as lecture_id,
    u.id as user_id,
    NOW() as created_at
FROM users u
WHERE u.login_id LIKE 'student%' 
AND CAST(SUBSTRING(u.login_id, 8) AS UNSIGNED) <= 280
ORDER BY RAND()
LIMIT 26;
