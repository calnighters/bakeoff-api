package com.bakeoff.api.service;

import com.bakeoff.api.dto.ResultDto;
import com.bakeoff.api.model.Baker;
import com.bakeoff.api.model.Judge;
import com.bakeoff.api.model.Result;
import com.bakeoff.api.repositories.BakerRepository;
import com.bakeoff.api.repositories.JudgeRepository;
import com.bakeoff.api.repositories.ResultsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiServiceImpl implements ApiService {

  private final ResultsRepository resultsRepository;
  private final BakerRepository bakerRepository;
  private final JudgeRepository judgeRepository;

  @Override
  public void enterNewResult(ResultDto resultDto) {
    bakerRepository.save(Baker.builder()
        .name(resultDto.getBakerName())
        .build());
    judgeRepository.save(Judge.builder()
        .name(resultDto.getJudgeName())
        .build());
    resultsRepository.save(Result.builder()
        .date(resultDto.getDate())
        .bakerName(resultDto.getBakerName())
        .judgeName(resultDto.getJudgeName())
        .appearance(resultDto.getAppearance())
        .taste(resultDto.getTaste())
        .build());
  }
}
