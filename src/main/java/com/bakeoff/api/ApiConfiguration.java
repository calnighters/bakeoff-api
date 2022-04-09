package com.bakeoff.api;

import java.time.Clock;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.bakeoff.api.repositories")
@EntityScan(basePackages = "com.bakeoff.api.model")
public class ApiConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
