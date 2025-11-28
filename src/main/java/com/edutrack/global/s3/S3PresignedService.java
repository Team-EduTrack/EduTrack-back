package com.edutrack.global.s3;

import io.github.cdimascio.dotenv.Dotenv;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

/**
 * Presigned URL 생성
 */
@Service
@RequiredArgsConstructor
public class S3PresignedService {

  private final Dotenv dotenv; // .env 로딩 객체

//  @Value("${cloud.aws.credentials.access-key}")
//  private String accessKey;
//
//  @Value("${cloud.aws.credentials.secret-key}")
//  private String secretKey;
//
//  @Value("${cloud.aws.region.static}")
//  private String region;
//
//  @Value("${cloud.aws.s3.bucket}")
//  private String bucket;

  // Presigned URL 생성
  public PresignedPutObjectRequest createPresignedUrl(String dir, String originalFileName) {

    // .env 값 로드
    String accessKey = dotenv.get("AWS_ACCESS_KEY");
    String secretKey = dotenv.get("AWS_SECRET_KEY");
    String region = dotenv.get("AWS_REGION");
    String bucket = dotenv.get("AWS_BUCKET");

    // 파일명을 랜덤 UUID + 원본 파일명으로 구성
    String key = dir + "/" + UUID.randomUUID() + "_" + originalFileName;

    // Presigner 생성 (환경변수 기반)
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

    S3Presigner s3Presigner = S3Presigner.builder()
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .region(Region.of(region))
        .build();

    // S3 업로드 요청 정보
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    // Presigned URL 생성 (5분 유효)
    PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(
        b -> b.putObjectRequest(putObjectRequest)
            .signatureDuration(Duration.ofMinutes(5))
    );

    return presignedPutObjectRequest;

  }


}
