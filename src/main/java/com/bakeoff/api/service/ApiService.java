package com.bakeoff.api.service;

import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.BakerResponseDto;
import com.bakeoff.api.dto.JudgeResponseDto;
import com.bakeoff.api.dto.ParticipantDto;
import com.bakeoff.api.dto.ResultDto;
import com.bakeoff.api.dto.TotalResponseDto;
import com.bakeoff.api.dto.UpdatePersonDto;

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

  void addParticipant(ParticipantDto participantDto);

  void addResult(ResultDto resultDto);

  void updateBaker(UpdatePersonDto updatePersonDto);

  void updateJudge(UpdatePersonDto updatePersonDto);

  void deleteBaker(String name);

  TotalResponseDto getTotals();
}
