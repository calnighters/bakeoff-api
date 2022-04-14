package com.bakeoff.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String ROLE_BAKEOFF_USER = "BAKEOFF_USER";

  @Value("${appsecurity.bakeoff.username}")
  private String bakeoffUsername;

  @Value("${appsecurity.bakeoff.password}")
  private String bakeoffPassword;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    auth.inMemoryAuthentication().withUser(bakeoffUsername)
        .password(encoder.encode(bakeoffPassword)).roles(ROLE_BAKEOFF_USER);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers(HttpMethod.GET, "/bakeoff/**").permitAll()
        .antMatchers("/bakeoff/**")
        .hasAnyRole(ROLE_BAKEOFF_USER)
        .anyRequest().authenticated()
        .and()
        .httpBasic()
        .and()
        .formLogin().disable()
        .csrf().disable();
  }
}
