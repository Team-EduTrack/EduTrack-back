package com.edutrack.domain.user.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SignupLockRepository {

  /**
   * RedisConfig 에서 설정한 RedisTemplate<String, String> 타입 Bean 중 이름이 "lockRedisTemplate" 인 Bean을
   * 주입받는다. (인증 코드, 락 같은 단순 문자열 용도)
   */

  @Qualifier("lockRedisTemplate")
  private final RedisTemplate<String, String> lockRedisTemplate;

  private static final String LOGIN_ID_KEY = "lock:loginId:";
  private static final String EMAIL_KEY = "lock:email:";
  private static final String PHONE_KEY = "lock:phone:";

  /**
   * 해당 loginId 가 현재 "락" 상태인지 확인
   */
  public boolean existsByLoginId(String loginId) {
    return Boolean.TRUE.equals(lockRedisTemplate.hasKey(LOGIN_ID_KEY + loginId));
  }

  /**
   * 해당 email이 현재 "락" 상태인지 확인
   */
  public boolean existsByEmail(String email) {
    return Boolean.TRUE.equals(lockRedisTemplate.hasKey(EMAIL_KEY + email));
  }

  /**
   * 해당 phone이 현재 "락" 상태인지 확인
   */
  public boolean existsByPhone(String phone) {
    return Boolean.TRUE.equals(lockRedisTemplate.hasKey(PHONE_KEY + phone));
  }

  public void lockAll(String loginId, String email, String phone){
    lockLoginId(loginId);
    lockEmail(email);
    lockPhone(phone);
  }
  public void unLockAll(String loginId, String email, String phone){
    unlockLoginId(loginId);
    unlockEmail(email);
    unlockPhone(phone);
  }

  /**
   * loginId 를 15분 동안 락(잠금) 처리 같은 loginId 로 동시에 회원가입 못하도록 막는다
   */
  private void lockLoginId(String loginId) {
    lockRedisTemplate.opsForValue()
        .set(LOGIN_ID_KEY + loginId, "1", 15, TimeUnit.MINUTES);
  }

  /**
   * email 를 15분 동안 락(잠금) 처리 같은 email 로 동시에 회원가입 못하도록 막는다
   */
  private void lockEmail(String email) {
    lockRedisTemplate.opsForValue()
        .set(EMAIL_KEY + email, "1", 15, TimeUnit.MINUTES);
  }

  /**
   * phone 를 15분 동안 락(잠금) 처리 같은 phone 로 동시에 회원가입 못하도록 막는다
   */
  private void lockPhone(String phone) {
    lockRedisTemplate.opsForValue()
        .set(PHONE_KEY + phone, "1", 15, TimeUnit.MINUTES);
  }


  private void unlockLoginId(String email) {
    lockRedisTemplate.delete(LOGIN_ID_KEY + email);
  }

  private void unlockEmail(String email) {
    lockRedisTemplate.delete(EMAIL_KEY + email);
  }

  private void unlockPhone(String email) {
    lockRedisTemplate.delete(PHONE_KEY + email);
  }


}
