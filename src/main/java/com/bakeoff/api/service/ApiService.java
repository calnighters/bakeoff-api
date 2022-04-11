package com.bakeoff.api.service;

import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.BakerResponseDto;
import com.bakeoff.api.dto.JudgeResponseDto;

public interface ApiService {

  void addBakeoff(String bakeOffName);

  void updateBakeOff(String name);

  BakeoffResponseDto getAllBakeoffs();

  BakeoffResponseDto getLatestBakeoff();

  void deleteParticipant(Integer entrantId);
  
  void addBaker(String bakerName);

  BakerResponseDto getBakers();

  void addJudge(String name);
  
  JudgeResponseDto getJudges();
}
