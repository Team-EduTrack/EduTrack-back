package com.edutrack.domain.user.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis 인증 코드 저장
 */
@Repository
@RequiredArgsConstructor
public class UserEmailVerificationRedisRepository {

  @Qualifier("lockRedisTemplate")
  private final RedisTemplate<String, String> lockRedisTemplate;

  private static final String PREFIX = "emailVerify:";

  public void saveCode(String email, String code, long ttlSeconds) {
    lockRedisTemplate.opsForValue().set(PREFIX + email, code, ttlSeconds, TimeUnit.SECONDS);
  }

  public String getCode(String email) {
    return lockRedisTemplate.opsForValue().get(PREFIX + email);
  }

  public void deleteCode(String email) {
    lockRedisTemplate.delete(PREFIX + email);
  }

}
