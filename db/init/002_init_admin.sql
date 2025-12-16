-- admin 계정 생성
INSERT INTO users (
  login_id, password, name, phone, email, email_verified, user_status
) VALUES (
  'admin',
  '$2a$10$MUcQKa2IoVzB36nK6KXSme.D.lH5/2e8olaRC3MD5mNISTk7.4wFO',
  '시스템관리자',
  '01012345678',
  'admin@edutrack.com',
  true,
  'ACTIVE'
);

-- admin ↔ ADMIN 역할 매핑
INSERT INTO user_to_role (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN role r ON r.name = 'ADMIN'
WHERE u.login_id = 'admin';