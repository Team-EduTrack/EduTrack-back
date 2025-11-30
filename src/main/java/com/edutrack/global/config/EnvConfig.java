package com.edutrack.global.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

  @Bean
  public Dotenv dotenv() {
    return Dotenv.configure()
        .directory("./")     // 프로젝트 루트에서 .env 찾기
        .ignoreIfMissing()   // .env 없어도 에러 안 나도록
        .load();
  }
}
