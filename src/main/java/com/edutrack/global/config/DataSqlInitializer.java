package com.edutrack.global.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1) // 다른 초기화보다 먼저 실행
public class DataSqlInitializer implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        try {
            log.info(">>> data.sql 실행 시작");
            
            // data.sql 파일 로드
            ClassPathResource resource = new ClassPathResource("data.sql");
            
            // 파일 존재 여부 확인
            if (!resource.exists()) {
                log.error(">>> data.sql 파일을 찾을 수 없습니다: {}", resource.getPath());
                return;
            }
            
            log.info(">>> data.sql 파일 발견: {}", resource.getURI());
            
            // SQL 스크립트 실행
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(resource);
            populator.setContinueOnError(false); // 에러 발생 시 중단하여 에러 확인
            populator.setIgnoreFailedDrops(true); // DROP 실패 무시
            populator.setSeparator(";"); // 세미콜론으로 구분
            
            try {
                populator.execute(dataSource);
                log.info(">>> data.sql 실행 완료");
            } catch (ScriptException e) {
                log.error(">>> data.sql 실행 중 에러 발생: {}", e.getMessage());
                log.error(">>> 에러 상세: ", e);
                // 에러가 발생해도 애플리케이션은 계속 실행되도록 함
            }
            
        } catch (ScriptException e) {
            // INSERT IGNORE로 인한 중복 에러는 정상적인 경우이므로 무시
            if (e.getMessage() != null && 
                (e.getMessage().contains("Duplicate entry") || 
                 e.getMessage().contains("already exists"))) {
                log.debug(">>> data.sql 실행 완료 (일부 중복 데이터 무시됨)");
            } else {
                log.warn(">>> data.sql 실행 중 경고: {}", e.getMessage());
                log.warn(">>> 스택 트레이스: ", e);
            }
        } catch (Exception e) {
            log.error(">>> data.sql 실행 실패: {}", e.getMessage(), e);
            // 에러가 발생해도 애플리케이션은 계속 실행되도록 함
        }
    }
}

