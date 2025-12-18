# EduTrack Backend

교육 기관 관리를 위한 백엔드 API 서비스입니다. 학원, 강의, 과제, 시험, 출석 등 교육 과정 전반을 관리할 수 있는 RESTful API를 제공합니다.

## 📋 프로젝트 소개

EduTrack은 학원 운영을 위한 통합 교육 관리 시스템의 백엔드 서비스입니다. 관리자, 원장, 교사, 학생 등 다양한 역할의 사용자가 교육 과정을 효율적으로 관리하고 학습할 수 있도록 지원합니다.

## 🎯 요구사항

### 필수 요구사항
- Java 17 이상
- Gradle 8 이상
- MySQL 8.0 이상
- Redis 7 이상

### 선택 요구사항
- Docker & Docker Compose (컨테이너 기반 실행)
- AWS S3 계정 (파일 저장소)
- Gmail 계정 (이메일 인증)

## 🚀 프로젝트 기능 및 설계

### 1. 사용자 인증 및 권한 관리

#### 1.1 회원가입 기능
- **회원가입 요청** (`POST /api/auth/signup/request`)
  - 사용자 기본 정보 입력 (이름, 이메일, 비밀번호 등)
  - 회원가입 토큰 발급
- **이메일 인증 코드 발송** (`POST /api/auth/send-email-verification`)
  - 회원가입 토큰을 사용하여 이메일 인증 코드 발송
  - Redis를 통한 인증 코드 임시 저장
- **이메일 인증 확인** (`POST /api/auth/verify-email`)
  - 발송된 인증 코드 검증
  - 이메일 인증 완료 처리
- **학원 코드 인증** (`POST /api/auth/academy-verify`)
  - 학원 소속 확인을 위한 학원 코드 검증
- **회원가입 완료** (`POST /api/auth/signup/complete`)
  - 모든 인증 절차 완료 후 최종 회원가입 처리
  - 사용자 계정 생성 및 역할 부여

#### 1.2 로그인/로그아웃 기능
- **로그인** (`POST /api/users/signin`)
  - 이메일과 비밀번호를 통한 인증
  - JWT 액세스 토큰 및 리프레시 토큰 발급
  - 사용자 역할 정보 포함
- **로그아웃** (`POST /api/users/logout`)
  - 현재 세션 종료
  - 리프레시 토큰 무효화 (Redis 활용)
- **내 정보 조회** (`GET /api/users/me`)
  - 현재 로그인한 사용자의 정보 조회
  - JWT 토큰 기반 인증 필요

#### 1.3 역할 기반 접근 제어 (RBAC)
- **관리자 (ADMIN)**: 시스템 전체 관리, 학원 등록
- **원장 (PRINCIPAL)**: 학원 관리, 강의 생성, 학생 관리
- **교사 (TEACHER)**: 강의 관리, 과제/시험 생성 및 채점
- **학생 (STUDENT)**: 강의 수강, 과제 제출, 시험 응시

### 2. 학원 관리

#### 2.1 학원 등록
- **학원 등록** (`POST /api/academy/signup`)
  - 관리자만 접근 가능
  - 학원 정보 등록 및 학원 코드 생성
  - 원장 계정 생성

### 3. 강의 관리

#### 3.1 강의 생성 및 조회
- **강의 생성** (`POST /api/lectures`)
  - 원장/교사 권한 필요
  - 강의명, 설명, 시작일, 종료일 등 설정
- **강의 목록 조회** (`GET /api/lectures`)
  - 교사/원장이 담당하는 강의 목록 조회
- **강의 상세 조회** (`GET /api/lectures/{lectureId}`)
  - 강의 정보 및 통계 데이터 포함
  - 강의 진행률, 과제 제출률, 출석률 등

#### 3.2 학생 배정
- **강의 학생 목록 조회** (`GET /api/lectures/{lectureId}/students`)
  - 해당 강의에 배정된 학생 목록 조회
- **배정 가능한 학생 검색** (`GET /api/lectures/{lectureId}/available-students`)
  - 강의에 배정되지 않은 학생 검색
- **학생 강의 배정** (`POST /api/lectures/{lectureId}/students`)
  - 여러 학생을 한 번에 강의에 배정

### 4. 과제 관리

#### 4.1 과제 생성 및 조회
- **과제 생성** (`POST /api/academies/{academyId}/lectures/{lectureId}/assignments`)
  - 교사 권한 필요
  - 과제명, 설명, 마감일 설정
- **과제 목록 조회** (`GET /api/academies/{academyId}/lectures/{lectureId}/assignments/list`)
  - 학생용: 특정 강의의 과제 목록 조회

#### 4.2 과제 제출
- **Presigned URL 요청** (`POST /api/academies/{academyId}/assignments/{assignmentId}/submissions/presigned-url`)
  - AWS S3에 직접 업로드하기 위한 Presigned URL 발급
  - 보안을 위해 제한된 시간 동안만 유효
- **과제 제출** (`POST /api/academies/{academyId}/assignments/{assignmentId}/submissions/submit`)
  - 학생이 과제 파일 업로드 후 제출
  - S3 파일 키 저장
  - 중복 제출 방지

#### 4.3 과제 채점
- **과제 제출 목록 조회** (`GET /api/academies/{academyId}/assignments/{assignmentId}/submissions/list`)
  - 교사용: 특정 과제의 모든 제출물 조회
- **과제 제출 상세 조회** (`GET /api/academies/{academyId}/assignments/{assignmentId}/submissions/{submissionId}`)
  - 교사용: 제출물 상세 정보 및 파일 다운로드
- **과제 채점** (`PATCH /api/academies/{academyId}/assignments/{assignmentId}/submissions/{submissionId}/grade`)
  - 점수 및 피드백 입력
- **내 제출물 조회** (`GET /api/academies/{academyId}/assignments/{assignmentId}/submissions/my-submission`)
  - 학생용: 자신이 제출한 과제 및 채점 결과 조회

### 5. 시험 관리

#### 5.1 시험 생성 및 문제 등록
- **시험 생성** (`POST /api/lectures/{lectureId}/exams`)
  - 교사/원장 권한 필요
  - 시험명, 설명, 시작일시, 종료일시 설정
- **문제 등록** (`POST /api/lectures/{lectureId}/exams/{examId}/mcq`)
  - 객관식 문제 등록
  - 문제 내용, 선택지, 정답, 배점, 난이도 설정
- **시험 상세 조회** (`GET /api/lectures/{lectureId}/exams/{examId}`)
  - 시험 정보 및 등록된 문제 목록 조회

#### 5.2 시험 응시
- **시험 시작** (`POST /api/student/exams/{examId}/start`)
  - 학생이 시험 응시 시작
  - 문제 목록 반환
  - 시험 시작 시간 기록
- **답안 저장** (`POST /api/student/exams/{examId}/answers`)
  - 제출 전 답안 임시 저장
  - 자동 저장 기능 지원
- **시험 제출** (`POST /api/student/exams/{examId}/submit`)
  - 최종 답안 제출
  - 자동 채점 실행
  - 제출 시간 기록

#### 5.3 시험 결과 조회
- **시험 결과 조회** (`GET /api/student/exams/{examId}/result`)
  - 학생용: 채점 완료된 시험 결과 확인
  - 점수, 정답률, 문제별 정답 여부 포함
- **시험 기록 목록** (`GET /api/student/exams/records`)
  - 학생이 응시한 모든 시험 기록 조회

### 6. 출석 관리

#### 6.1 출석 체크인
- **출석 체크인** (`POST /api/student/attendance/check-in`)
  - 학생이 출석 체크인
  - 중복 체크인 방지
  - 출석 시간 기록

### 7. 학생 대시보드

#### 7.1 내 강의 조회
- **내 강의 목록** (`GET /api/student/lectures`)
  - 학생이 수강 중인 강의 목록 조회
- **강의 상세 정보** (`GET /api/student/lectures/{lectureId}`)
  - 강의 정보 및 학습 현황 조회

#### 7.2 과제 및 시험 현황
- **내 과제 목록** (`GET /api/student/assignments`)
  - 학생의 과제 목록 및 제출 현황 조회
- **내 시험 목록** (`GET /api/student/exams`)
  - 학생의 시험 목록 및 응시 현황 조회

### 8. 통계 및 리포트

#### 8.1 학생 리포트
- **학생 통합 분석** (`GET /api/analysis/student/{studentId}`)
  - 학생의 종합 학습 분석 리포트
  - 시험 성적, 과제 제출률, 출석률 등 통합 정보
- **시험 요약** (`GET /api/students/{studentId}/analysis/exams`)
  - 학생의 모든 시험 성적 요약
- **취약 단원 분석** (`GET /api/students/{studentId}/analysis/weak-units`)
  - 정답률이 낮은 단원 분석 (기본 5개)
- **월간 출석 현황** (`GET /api/students/{studentId}/lectures/{lectureId}/attendance/monthly`)
  - 특정 강의의 월별 출석 현황 조회

#### 8.2 강의 통계
- **강의 통계 조회** (`GET /api/lectures/{lectureId}`)
  - 강의 진행률, 과제 제출률, 출석률 등
  - 교사/원장 권한 필요

### 9. 파일 관리

#### 9.1 AWS S3 연동
- **Presigned URL 발급**
  - 클라이언트에서 직접 S3에 파일 업로드
  - 보안을 위한 제한된 시간 유효 URL
  - 과제 제출 파일 업로드에 활용

## 🛠 기술 스택

### Backend Framework
- **Spring Boot 3.5.7**: Java 기반 웹 애플리케이션 프레임워크
- **Java 17**: 프로그래밍 언어

### Database
- **MySQL 8.0**: 관계형 데이터베이스
- **Redis 7**: 캐시 및 세션 저장소

### Security
- **Spring Security**: 인증 및 권한 관리
- **JWT (JSON Web Token)**: 토큰 기반 인증
  - 액세스 토큰: 1시간 유효
  - 리프레시 토큰: 14일 유효

### File Storage
- **AWS S3**: 파일 저장소
- **AWS SDK for Java 2.x**: S3 연동

### Email
- **Spring Mail**: 이메일 발송
- **SMTP (Gmail)**: 이메일 서버

### API Documentation
- **SpringDoc OpenAPI**: API 문서화
- **Swagger UI**: API 테스트 인터페이스

### Build Tool
- **Gradle 8**: 빌드 및 의존성 관리

### Container
- **Docker**: 컨테이너화
- **Docker Compose**: 다중 컨테이너 관리

### 기타
- **Lombok**: 보일러플레이트 코드 감소
- **JPA/Hibernate**: ORM 프레임워크
- **Bean Validation**: 데이터 검증

## 🚀 시작하기

### 로컬 환경에서 실행

1. **저장소 클론**
```bash
git clone <repository-url>
cd EduTrack-back
```

2. **환경 변수 설정**

프로젝트 루트에 `.env` 파일을 생성하거나 시스템 환경 변수로 다음 값들을 설정합니다:

```bash
# 이메일 설정
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# AWS S3 설정
AWS_ACCESS_KEY=your-aws-access-key
AWS_SECRET_KEY=your-aws-secret-key

# JWT 설정 (선택사항, 기본값 사용 가능)
JWT_SECRET=your-jwt-secret-key
```

### Docker Compose를 이용한 실행

전체 스택을 Docker Compose로 실행할 수 있습니다:

```bash
docker-compose up -d
```

이 명령어는 다음 서비스들을 시작합니다:
- MySQL 데이터베이스 (포트 3306)
- Redis 캐시 (포트 6379)
- 백엔드 애플리케이션 (포트 8080)

로그 확인:
```bash
docker-compose logs -f backend
```

서비스 중지:
```bash
docker-compose down
```

## 📚 API 문서 (현재 접근 불가 수정 예정)

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

Swagger UI를 통해 API를 테스트하고 요청/응답 예시를 확인할 수 있습니다.

### 주요 API 엔드포인트 요약

| 기능 | 메서드 | 엔드포인트 | 권한 |
|------|--------|-----------|------|
| 회원가입 요청 | POST | `/api/auth/signup/request` | 공개 |
| 이메일 인증 코드 발송 | POST | `/api/auth/send-email-verification` | 공개 |
| 이메일 인증 확인 | POST | `/api/auth/verify-email` | 공개 |
| 학원 코드 인증 | POST | `/api/auth/academy-verify` | 공개 |
| 회원가입 완료 | POST | `/api/auth/signup/complete` | 공개 |
| 로그인 | POST | `/api/users/signin` | 공개 |
| 로그아웃 | POST | `/api/users/logout` | 인증 필요 |
| 내 정보 조회 | GET | `/api/users/me` | 인증 필요 |
| 학원 등록 | POST | `/api/academy/signup` | ADMIN |
| 강의 생성 | POST | `/api/lectures` | PRINCIPAL, TEACHER |
| 강의 목록 조회 | GET | `/api/lectures` | PRINCIPAL, TEACHER |
| 강의 상세 조회 | GET | `/api/lectures/{lectureId}` | PRINCIPAL, TEACHER |
| 학생 강의 배정 | POST | `/api/lectures/{lectureId}/students` | PRINCIPAL, TEACHER |
| 과제 생성 | POST | `/api/academies/{academyId}/lectures/{lectureId}/assignments` | TEACHER |
| 과제 제출 | POST | `/api/academies/{academyId}/assignments/{assignmentId}/submissions/submit` | STUDENT |
| 과제 채점 | PATCH | `/api/academies/{academyId}/assignments/{assignmentId}/submissions/{submissionId}/grade` | TEACHER |
| 시험 생성 | POST | `/api/lectures/{lectureId}/exams` | PRINCIPAL, TEACHER |
| 시험 시작 | POST | `/api/student/exams/{examId}/start` | STUDENT |
| 시험 제출 | POST | `/api/student/exams/{examId}/submit` | STUDENT |
| 시험 결과 조회 | GET | `/api/student/exams/{examId}/result` | STUDENT |
| 출석 체크인 | POST | `/api/student/attendance/check-in` | STUDENT |
| 내 강의 목록 | GET | `/api/student/lectures` | STUDENT |
| 학생 통합 분석 | GET | `/api/analysis/student/{studentId}` | STUDENT |

## 🧪 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "com.edutrack.domain.user.UserServiceTest"
```

## 🔧 트러블슈팅

### 데이터베이스 연결 오류
- MySQL이 실행 중인지 확인: `mysql -u edutrack -p`
- 데이터베이스가 생성되었는지 확인
- `application.properties`의 데이터베이스 연결 정보 확인

### Redis 연결 오류
- Redis 서버가 실행 중인지 확인: `redis-cli ping`
- 포트 6379가 사용 가능한지 확인

### 이메일 발송 실패
- Gmail 앱 비밀번호를 사용해야 합니다 (일반 비밀번호 아님)
- 2단계 인증이 활성화되어 있어야 합니다
- `MAIL_USERNAME`과 `MAIL_PASSWORD` 환경 변수가 올바르게 설정되었는지 확인

### AWS S3 연결 오류
- AWS 자격 증명이 올바르게 설정되었는지 확인
- S3 버킷이 존재하고 접근 권한이 있는지 확인
- 리전 설정이 올바른지 확인 (기본값: ap-northeast-2)

### JWT 토큰 오류
- 토큰이 만료되었는지 확인 (액세스 토큰: 1시간)
- 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
- 로그아웃 후 다시 로그인

## 📁 프로젝트 구조

```
src/main/java/com/edutrack/
├── domain/                    # 도메인별 비즈니스 로직
│   ├── academy/              # 학원 관리
│   ├── assignment/           # 과제 관리
│   │   ├── controller/       # 과제 API 컨트롤러
│   │   ├── dto/              # 과제 관련 DTO
│   │   ├── entity/           # 과제 엔티티
│   │   ├── repository/       # 과제 리포지토리
│   │   └── service/          # 과제 서비스
│   ├── attendance/           # 출석 관리
│   ├── exam/                 # 시험 관리
│   ├── lecture/              # 강의 관리
│   ├── principal/            # 원장 관리
│   ├── statistics/           # 통계 및 리포트
│   ├── student/              # 학생 대시보드
│   ├── unit/                 # 단원 관리
│   └── user/                 # 사용자 관리
├── global/                   # 전역 설정 및 공통 기능
│   ├── common/              # 공통 응답 DTO
│   ├── config/              # 설정 클래스
│   ├── exception/           # 예외 처리
│   ├── mail/                # 이메일 서비스
│   ├── s3/                  # S3 파일 업로드
│   └── security/            # 보안 설정
└── EdutrackApplication.java # 메인 애플리케이션
```

## 💻 개발 가이드

### 코드 스타일
- Java 코드는 Google Java Style Guide를 따릅니다
- Lombok을 사용하여 보일러플레이트 코드를 최소화합니다
- DTO는 요청/응답별로 분리하여 관리합니다

### 아키텍처 패턴
- **도메인 주도 설계 (DDD)**: 도메인별로 패키지를 분리
- **계층형 아키텍처**: Controller → Service → Repository 구조
- **RESTful API**: REST 원칙을 따르는 API 설계

### 예외 처리
- 커스텀 예외 클래스를 통해 비즈니스 예외를 처리합니다
- `CustomExceptionHandler`를 통해 전역 예외 처리를 수행합니다
- 예외 메시지는 사용자에게 명확하게 전달됩니다

### 보안
- JWT 기반 인증을 사용합니다
- Spring Security를 통해 역할 기반 접근 제어를 구현합니다
- 비밀번호는 BCrypt로 해시화하여 저장합니다

### 파일 업로드
- AWS S3 Presigned URL을 사용하여 클라이언트에서 직접 파일을 업로드합니다
- 보안을 위해 업로드 URL은 제한된 시간 동안만 유효합니다

### 데이터베이스
- JPA를 사용하여 엔티티를 기반으로 스키마를 관리합니다
- `spring.jpa.hibernate.ddl-auto=validate`로 프로덕션 환경에서는 스키마 검증만 수행합니다

## 👥 기여자

- 손창우
- 정효은
- 송하연
- 정용화
- 정창민

## 📄 라이선스

이 프로젝트는 팀 프로젝트로 개발되었습니다.