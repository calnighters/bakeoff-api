package com.bakeoff.api.service;

import com.bakeoff.api.exceptions.NotFoundException;
import com.bakeoff.api.model.Bakeoff;
import com.bakeoff.api.repositories.BakeoffRepistory;
import com.bakeoff.api.repositories.BakerRepository;
import com.bakeoff.api.repositories.JudgeRepository;
import com.bakeoff.api.repositories.ParticipantRepository;
import com.bakeoff.api.repositories.ResultRepository;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiServiceImpl implements ApiService {

  private final ResultRepository resultRepository;
  private final BakerRepository bakerRepository;
  private final JudgeRepository judgeRepository;
  private final BakeoffRepistory bakeoffRepistory;
  private final ParticipantRepository participantRepository;
  private final Clock clock;

  @Override
  public void addBakeoff(String bakeOffName) {
    bakeoffRepistory.save(
        Bakeoff.builder()
            .boDate(LocalDate.now(clock))
            .food(bakeOffName)
            .build()
    );
  }

  @Override
  public void updateBakeOff(String bakeOffName) {
    Bakeoff bakeoff = bakeoffRepistory.findByBoDate(LocalDate.now(clock))
        .orElseThrow(
            () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    bakeoff.setFood(bakeOffName);
    bakeoffRepistory.save(bakeoff);
  }
}
