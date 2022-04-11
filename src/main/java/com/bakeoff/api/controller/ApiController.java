package com.bakeoff.api.controller;

import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.BakerResponseDto;
import com.bakeoff.api.dto.JudgeResponseDto;
import com.bakeoff.api.dto.ParticipantDto;
import com.bakeoff.api.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "https://bakeoff-web.herokuapp.com")
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

  @GetMapping
  public BakeoffResponseDto getAllBakeoffs() {
    return apiService.getAllBakeoffs();
  }

  @GetMapping(path = "/latest")
  public BakeoffResponseDto getLatestBakeoff() {
    return apiService.getLatestBakeoff();
  }

  @PostMapping("/participant")
  public void addParticipant(@RequestBody ParticipantDto participantDto) {
    apiService.addParticipant(participantDto);
  }
  
  @DeleteMapping("/participant")
  public void deleteParticipant(@PathVariable Integer entrantId) {
    apiService.deleteParticipant(entrantId);
  }

  @PostMapping(path = "/baker")
  public void addBaker(@RequestParam String name) {
    apiService.addBaker(name);
  }

  @GetMapping(path = "/baker")
  public BakerResponseDto getBakers() {
    return apiService.getBakers();
  }

  @PostMapping(path = "/judge")
  public void addJudge(@RequestParam String name) {
    apiService.addJudge(name);
  }

  @GetMapping(path = "/judge")
  public JudgeResponseDto getJudges() {
    return apiService.getJudges();
  }
}
