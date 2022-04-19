package com.bakeoff.api.controller;

import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.BakerResponseDto;
import com.bakeoff.api.dto.JudgeResponseDto;
import com.bakeoff.api.dto.ParticipantDto;
import com.bakeoff.api.dto.ResultDto;
import com.bakeoff.api.dto.TotalResponseDto;
import com.bakeoff.api.dto.UpdatePersonDto;
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

@CrossOrigin(origins = {"https://bakeoff-web.herokuapp.com", "http://localhost:3000"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/bakeoff")
public class ApiController {

  private final ApiService apiService;

  @PostMapping(path = "/event")
  public void addBakeoff(@RequestParam String name) {
    apiService.addBakeoff(name);
  }

  @PutMapping(path = "/event")
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

  @PostMapping(path = "/participant")
  public void addParticipant(@RequestBody ParticipantDto participantDto) {
    apiService.addParticipant(participantDto);
  }

  @PutMapping(path = "/participant")
  public void updateParticipant(@RequestBody ParticipantDto participantDto) {
    apiService.updateParticipant(participantDto);
  }

  @DeleteMapping(path = "/participant")
  public void deleteParticipant(@RequestParam Integer entrantId) {
    apiService.deleteParticipant(entrantId);
  }

  @PostMapping(path = "/baker")
  public void addBaker(@RequestParam String name) {
    apiService.addBaker(name);
  }

  @PutMapping(path = "/baker")
  public void updateBaker(@RequestBody UpdatePersonDto updatePersonDto) {
    apiService.updateBaker(updatePersonDto);
  }

  @DeleteMapping(path = "/baker")
  public void deleteBaker(@RequestParam String name) {
    apiService.deleteBaker(name);
  }

  @GetMapping(path = "/baker")
  public BakerResponseDto getBakers() {
    return apiService.getBakers();
  }

  @PostMapping(path = "/judge")
  public void addJudge(@RequestParam String name) {
    apiService.addJudge(name);
  }

  @PutMapping(path = "/judge")
  public void updateJudge(@RequestBody UpdatePersonDto updatePersonDto) {
    apiService.updateJudge(updatePersonDto);
  }

  @DeleteMapping(path = "judge")
  public void deleteJudge(@RequestParam String judgeName) {
    apiService.deleteJudge(judgeName);
  }

  @GetMapping(path = "/judge")
  public JudgeResponseDto getJudges() {
    return apiService.getJudges();
  }

  @PostMapping(path = "/result")
  public void addResult(@RequestBody ResultDto resultDto) {
    apiService.addResult(resultDto);
  }

  @PutMapping(path = "/result")
  public void updateResult(@RequestBody ResultDto resultDto) {
    apiService.updateResult(resultDto);
  }

  @DeleteMapping(path = "/result")
  public void deleteResult(@RequestBody ResultDto resultDto) {
    apiService.deleteResult(resultDto);
  }

  @GetMapping(path = "/totals")
  public TotalResponseDto getTotals() {
    return apiService.getTotals();
  }
}
