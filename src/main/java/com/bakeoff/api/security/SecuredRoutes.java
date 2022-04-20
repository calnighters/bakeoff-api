package com.bakeoff.api.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "securedroutes")
public class SecuredRoutes {

  private String[] secured;
  private String[] unsecured;
}
