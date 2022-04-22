package com.bakeoff.api;

import static software.amazon.awssdk.regions.Region.of;

import com.bakeoff.api.config.AwsConfig;
import java.net.URI;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableJpaRepositories(basePackages = "com.bakeoff.api.repositories")
@EntityScan(basePackages = "com.bakeoff.api.model")
@RequiredArgsConstructor
public class ApiConfiguration {

  private final AwsConfig awsConfig;

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  public S3Client s3Client() {
    AwsCredentials awsCredentials = AwsBasicCredentials.create(
        awsConfig.getAccessKey(),
        awsConfig.getSecretKey()
    );
    return S3Client.builder()
        .region(of(awsConfig.getRegion()))
        .endpointOverride(URI.create(awsConfig.getEndpoint()))
        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
        .httpClientBuilder(UrlConnectionHttpClient.builder())
        .build();
  }
}
