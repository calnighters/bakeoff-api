package com.bakeoff.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final SecuredRoutes securedRoutes;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers(HttpMethod.GET, securedRoutes.getUnsecured()).permitAll()
        .antMatchers(securedRoutes.getSecured()).hasRole("USER")
        .and()
        .httpBasic()
        .and()
        .formLogin().disable()
        .csrf().disable();
  }
}
