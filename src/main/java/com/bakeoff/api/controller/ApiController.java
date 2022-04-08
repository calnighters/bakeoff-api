package com.bakeoff.api.controller;

import com.bakeoff.api.dto.ResultDto;
import com.bakeoff.api.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bakeoff")
public class ApiController {

  private final ApiService apiService;

  @PostMapping(path = "result")
  public void enterNewResult(@RequestBody ResultDto resultDto) {
    apiService.enterNewResult(resultDto);
  }
  
}
