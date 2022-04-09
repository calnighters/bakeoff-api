package com.bakeoff.api.controller;

import com.bakeoff.api.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bakeoff")
public class ApiController {

  private final ApiService apiService;

  @PostMapping
  public void addBakeoff(@RequestParam String name) {
    apiService.addBakeoff(name);
  }
  
  @PutMapping
  public void updateBakeOff(@RequestParam String name) {
    apiService.updateBakeOff(name);
  }
  
}
