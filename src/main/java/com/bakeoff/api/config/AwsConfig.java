package com.bakeoff.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aws")
public class AwsConfig {

  private String bucketName;
  private String accessKey;
  private String secretKey;
  private String region;
  private String endpoint;
}
