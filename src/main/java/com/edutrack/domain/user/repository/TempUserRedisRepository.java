package com.edutrack.domain.user.repository;

import com.edutrack.domain.user.entity.TempUser;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TempUserRedisRepository {

  /**
   * RedisConfig 에서 설정한
   * RedisTemplate<String , TempUser> 타입 Bean 중
   * 이름이 "tempUserRedisTemplate" 인 Bean 주입
   */
  @Qualifier("tempUserRedisTemplate")
  private final RedisTemplate<String, TempUser> tempUserRedisTemplate;

  private static final String PREFIX = "tempUser:";

  public void save(TempUser tempUser, long ttlSeconds) {
    tempUserRedisTemplate.opsForValue().set(PREFIX + tempUser.getEmail(), tempUser, ttlSeconds,
        TimeUnit.SECONDS);
  }

  public TempUser findByEmail(String email) {
    return tempUserRedisTemplate.opsForValue().get(PREFIX + email);
  }

  public void deleteByEmail(String email) {
    tempUserRedisTemplate.delete(PREFIX + email);
  }

}
