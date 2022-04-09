package com.bakeoff.api.service;

import com.bakeoff.api.dto.BakeoffResponseDto;

public interface ApiService {

  void addBakeoff(String bakeOffName);

  void updateBakeOff(String name);

  BakeoffResponseDto getAllBakeoffs();

  BakeoffResponseDto getLatestBakeoff();

  void deleteParticipant(Integer entrantId);
}
