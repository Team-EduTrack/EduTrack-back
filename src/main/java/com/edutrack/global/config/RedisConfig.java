package com.edutrack.global.config;

import com.edutrack.domain.user.entity.TempUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  /**
   * TempUser 객체를 Redis에 저장/조회하기 위한 RedisTemplate Bean
   * - key : String
   * - value : TempUser(JSON 직렬화)
   */
  @Bean
  public RedisTemplate<String, TempUser> tempUserRedisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, TempUser> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new Jackson2JsonRedisSerializer<>(TempUser.class));
    template.afterPropertiesSet(); // 위에서 설정한 값들로 내부 초기화 작업 수행
    return template;
  }

  /**
   * 단순 문자열 용도로 사용하는 RedisTemplate Bean
   * - 이메일 인증 코드, 락(lock) 같은 용도
   * - key : String
   * - value : String
   */
  @Bean
  public RedisTemplate<String, String> lockRedisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    return template;
  }

}
