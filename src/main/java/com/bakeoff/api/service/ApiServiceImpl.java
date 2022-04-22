package com.bakeoff.api.service;

import com.bakeoff.api.dto.BakeoffDto;
import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.BakerDto;
import com.bakeoff.api.dto.BakerResponseDto;
import com.bakeoff.api.dto.JudgeResponseDto;
import com.bakeoff.api.dto.ParticipantDto;
import com.bakeoff.api.dto.PersonDto;
import com.bakeoff.api.dto.ResultDto;
import com.bakeoff.api.dto.TotalResponseDto;
import com.bakeoff.api.dto.UpdatePersonDto;
import com.bakeoff.api.exceptions.InvalidFormatException;
import com.bakeoff.api.exceptions.NotFoundException;
import com.bakeoff.api.model.Bakeoff;
import com.bakeoff.api.model.Baker;
import com.bakeoff.api.model.Judge;
import com.bakeoff.api.model.JudgeHistory;
import com.bakeoff.api.model.Participant;
import com.bakeoff.api.model.Result;
import com.bakeoff.api.repositories.BakeoffRepistory;
import com.bakeoff.api.repositories.BakerRepository;
import com.bakeoff.api.repositories.JudgeHistoryRepository;
import com.bakeoff.api.repositories.JudgeRepository;
import com.bakeoff.api.repositories.ParticipantRepository;
import com.bakeoff.api.repositories.ResultRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ApiServiceImpl implements ApiService {

  private final ResultRepository resultRepository;
  private final BakerRepository bakerRepository;
  private final JudgeRepository judgeRepository;
  private final BakeoffRepistory bakeoffRepistory;
  private final ParticipantRepository participantRepository;
  private final JudgeHistoryRepository judgeHistoryRepository;
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

  @Override
  public BakeoffResponseDto getAllBakeoffs() {
    List<Bakeoff> bakeoffs = StreamSupport
        .stream(bakeoffRepistory.findAll().spliterator(), false)
        .collect(Collectors.toList());

    List<BakeoffDto> bakeoffDtos = bakeoffs
        .stream()
        .map(this::bakeoffToDto)
        .collect(Collectors.toList());

    return BakeoffResponseDto.builder()
        .bakeoffs(bakeoffDtos)
        .build();
  }

  @Override
  public BakeoffResponseDto getLatestBakeoff() {
    List<Bakeoff> bakeoffs = StreamSupport
        .stream(bakeoffRepistory.findAll().spliterator(), false)
        .sorted(Comparator.comparing(Bakeoff::getBoDate))
        .collect(Collectors.toList());

    List<BakeoffDto> bakeoffDtos;
    if (!bakeoffs.isEmpty()) {
      bakeoffDtos = List.of(bakeoffToDto(bakeoffs.get(bakeoffs.size() - 1)));
    } else {
      bakeoffDtos = Collections.emptyList();
    }

    return BakeoffResponseDto.builder()
        .bakeoffs(bakeoffDtos)
        .build();
  }

  @Override
  public void deleteParticipant(Integer entrantId) {
    if (entrantId == null) {
      throw new InvalidFormatException("Entrant ID must not be null");
    }
    Bakeoff bakeoff = bakeoffRepistory.findByBoDate(LocalDate.now(clock))
        .orElseThrow(
            () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    List<Participant> participants = bakeoff.getParticipants();
    Participant toDelete = null;
    for (Participant p : participants) {
      if (Objects.equals(p.getEntrantId(), entrantId)) {
        toDelete = p;
      }
    }

    if (toDelete != null) {
      resultRepository.deleteAll(toDelete.getResults());
      participantRepository.delete(toDelete);
    } else {
      throw new InvalidFormatException("Entrant ID: " + entrantId + " not part of current bakeoff");
    }
  }

  @Override
  public void addBaker(String bakerName) {
    bakerRepository.save(
        Baker.builder()
            .bakerName(bakerName)
            .build()
    );
  }

  @Override
  public BakerResponseDto getBakers() {
    List<Baker> bakers = StreamSupport
        .stream(bakerRepository.findAll().spliterator(), false)
        .sorted(Comparator.comparing(Baker::getId))
        .collect(Collectors.toList());
    return
        BakerResponseDto.builder()
            .bakers(
                bakers.stream()
                    .map(this::bakerToPersonDto)
                    .collect(Collectors.toList())
            )
            .build();
  }

  @Override
  public void addJudge(String name) {
    Bakeoff bakeoff = bakeoffRepistory.findByBoDate(LocalDate.now(clock))
        .orElseThrow(
            () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    judgeRepository.save(
        Judge.builder()
            .judgeName(name)
            .build()
    );
  }

  @Override
  public JudgeResponseDto getJudges() {
    List<Judge> judges = StreamSupport
        .stream(judgeRepository.findAll().spliterator(), false)
        .sorted(Comparator.comparing(Judge::getId))
        .collect(Collectors.toList());
    return
        JudgeResponseDto.builder()
            .judges(
                judges.stream()
                    .map(this::judgeToPersonDto)
                    .collect(Collectors.toList())
            )
            .build();
  }

  @Override
  public void addParticipant(ParticipantDto participantDto) {
    Bakeoff bakeoff = bakeoffRepistory.findByBoDate(LocalDate.now(clock))
        .orElseThrow(
            () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    Baker baker = bakerRepository.findById(participantDto.getBakerId()).orElseThrow(
        () -> new NotFoundException("Baker not found for ID: " + participantDto.getBakerId()));
    participantRepository.save(
        Participant.builder()
            .entrantId(participantDto.getEntrantId())
            .fkBakeoff(bakeoff)
            .fkBaker(baker)
            .description(participantDto.getDescription())
            .build()
    );
  }

  @Override
  public void addResult(ResultDto resultDto) {
    Bakeoff bakeoff = bakeoffRepistory.findByBoDate(LocalDate.now(clock))
        .orElseThrow(
            () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    Participant participant = participantRepository.findByEntrantIdAndFkBakeoff(
        resultDto.getEntrantId(), bakeoff).orElseThrow(() -> new NotFoundException(
        "Participant not found for Entrant ID: " + resultDto.getEntrantId() + " and date: "
            + LocalDate.now(clock)));
    Judge judge = judgeRepository.findByJudgeName(resultDto.getJudgeName())
        .orElseThrow(() -> new NotFoundException(
            "Judge not found for name: " + resultDto.getJudgeName()));
    Optional<JudgeHistory> judgeHistory = judgeHistoryRepository.findByFkBakeoffAndFkJudge(bakeoff,
        judge);
    if (judgeHistory.isEmpty()) {
      judgeHistoryRepository.save(
          JudgeHistory.builder()
              .fkBakeoff(bakeoff)
              .fkJudge(judge)
              .build()
      );
    }
    resultRepository.save(
        Result.builder()
            .fkJudge(judge)
            .fkParticipant(participant)
            .taste(resultDto.getTaste())
            .appearance(resultDto.getAppearance())
            .build()
    );
  }

  @Override
  public void updateBaker(UpdatePersonDto updatePersonDto) {
    Baker baker = bakerRepository.findByBakerName(updatePersonDto.getOldName())
        .orElseThrow(
            () -> new NotFoundException(
                "Baker cannot be found with the name: " + updatePersonDto.getOldName()));
    baker.setBakerName(updatePersonDto.getNewName());
    bakerRepository.save(baker);
  }

  @Override
  public void updateJudge(UpdatePersonDto updatePersonDto) {
    Judge judge = judgeRepository.findByJudgeName(updatePersonDto.getOldName())
        .orElseThrow(
            () -> new NotFoundException(
                "Judge cannot be found with the name: " + updatePersonDto.getOldName()));
    judge.setJudgeName(updatePersonDto.getNewName());
    judgeRepository.save(judge);
  }

  @Override
  public void deleteBaker(String name) {
    Baker baker = bakerRepository.findByBakerName(name)
        .orElseThrow(() -> new NotFoundException("Baker cannot be found with the name: " + name));
    List<Result> results = new ArrayList<>();
    for (Participant participant : baker.getParticipants()) {
      results.addAll(participant.getResults());
    }
    resultRepository.deleteAll(results);
    participantRepository.deleteAll(baker.getParticipants());
    bakerRepository.delete(baker);
  }

  @Override
  public TotalResponseDto getTotals() {
    List<Baker> bakers = StreamSupport
        .stream(bakerRepository.findAll().spliterator(), false)
        .sorted(Comparator.comparing(Baker::getId))
        .collect(Collectors.toList());
    return TotalResponseDto.builder()
        .bakers(
            bakers.stream()
                .map(this::bakerToDto)
                .collect(Collectors.toList())
        )
        .build();
  }

  @Override
  public void updateResult(ResultDto resultDto) {
    Bakeoff bakeoff = bakeoffRepistory.findByBoDate(LocalDate.now(clock))
        .orElseThrow(
            () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    Judge judge = judgeRepository.findByJudgeName(resultDto.getJudgeName())
        .orElseThrow(
            () -> new NotFoundException(
                "Judge cannot be found with the name: " + resultDto.getJudgeName()));
    Participant participant = participantRepository.findByEntrantIdAndFkBakeoff(
        resultDto.getEntrantId(), bakeoff).orElseThrow(() -> new NotFoundException(
        "Participant not found for Entrant ID: " + resultDto.getEntrantId() + " and date: "
            + LocalDate.now(clock)));
    Result result = resultRepository.findByFkJudgeAndFkParticipant(judge, participant).orElseThrow(
        () -> new NotFoundException(
            "Result not found for Judge: " + judge.getJudgeName() + " and Entrant ID: "
                + participant.getEntrantId()));
    result.setAppearance(resultDto.getAppearance());
    result.setTaste(resultDto.getTaste());
    resultRepository.save(result);
  }

  @Override
  public void deleteResult(ResultDto resultDto) {
    Bakeoff bakeoff = bakeoffRepistory.findByBoDate(LocalDate.now(clock))
        .orElseThrow(
            () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    Judge judge = judgeRepository.findByJudgeName(resultDto.getJudgeName())
        .orElseThrow(
            () -> new NotFoundException(
                "Judge cannot be found with the name: " + resultDto.getJudgeName()));
    Participant participant = participantRepository.findByEntrantIdAndFkBakeoff(
        resultDto.getEntrantId(), bakeoff).orElseThrow(() -> new NotFoundException(
        "Participant not found for Entrant ID: " + resultDto.getEntrantId() + " and date: "
            + LocalDate.now(clock)));
    Result result = resultRepository.findByFkJudgeAndFkParticipant(judge, participant).orElseThrow(
        () -> new NotFoundException(
            "Result not found for Judge: " + judge.getJudgeName() + " and Entrant ID: "
                + participant.getEntrantId()));
    resultRepository.delete(result);
  }

  @Override
  public void updateParticipant(ParticipantDto participantDto) {
    Bakeoff bakeoff = bakeoffRepistory.findByBoDate(LocalDate.now(clock))
        .orElseThrow(
            () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    Participant participant = participantRepository.findByEntrantIdAndFkBakeoff(
        participantDto.getEntrantId(), bakeoff).orElseThrow(() -> new NotFoundException(
        "Participant not found for Entrant ID: " + participantDto.getEntrantId() + " and date: "
            + LocalDate.now(clock)));
    Baker baker = bakerRepository.findById(participantDto.getBakerId()).orElseThrow(
        () -> new NotFoundException("Baker not found for ID: " + participantDto.getBakerId()));
    participant.setDescription(participantDto.getDescription());
    participant.setFkBaker(baker);
    participantRepository.save(participant);
  }

  private Result updateResults(Result result, Judge judge) {
    result.setFkJudge(judge);
    return result;
  }

  private JudgeHistory updateJudgeHistory(JudgeHistory judgeHistory, Judge judge) {
    judgeHistory.setFkJudge(judge);
    return judgeHistory;
  }

  private BakerDto bakerToDto(Baker baker) {
    return BakerDto.builder()
        .id(baker.getId())
        .name(baker.getBakerName())
        .totalAppearance(getTotalAppearance(baker.getParticipants()))
        .totalTaste(getTotalTaste(baker.getParticipants()))
        .events(participantsToListDtos(baker.getParticipants()))
        .build();
  }

  private BigDecimal getTotalTaste(List<Participant> participants) {
    List<Result> results = new ArrayList<>();
    for (Participant participant : participants) {
      results.addAll(participant.getResults());
    }
    return results.stream()
        .map(Result::getTaste)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal getTotalAppearance(List<Participant> participants) {
    List<Result> results = new ArrayList<>();
    for (Participant participant : participants) {
      results.addAll(participant.getResults());
    }
    return results.stream()
        .map(Result::getAppearance)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private PersonDto judgeToPersonDto(Judge judge) {
    return PersonDto.builder()
        .id(judge.getId())
        .name(judge.getJudgeName())
        .build();
  }

  private PersonDto bakerToPersonDto(Baker baker) {
    return PersonDto.builder()
        .id(baker.getId())
        .name(baker.getBakerName())
        .build();
  }

  private BakeoffDto bakeoffToDto(Bakeoff bakeoff) {
    return BakeoffDto.builder()
        .date(bakeoff.getBoDate())
        .title(bakeoff.getFood())
        .participants(participantsToListDtos(bakeoff.getParticipants()))
        .build();
  }

  private List<ParticipantDto> participantsToListDtos(List<Participant> participants) {
    return participants
        .stream()
        .map(this::participantToDto)
        .collect(Collectors.toList());
  }

  private ParticipantDto participantToDto(Participant participant) {
    return ParticipantDto.builder()
        .entrantId(participant.getEntrantId())
        .bakerId(participant.getFkBaker().getId())
        .name(participant.getFkBaker().getBakerName())
        .results(scoresFromParticipant(participant))
        .description(participant.getDescription())
        .bakeoffDescription(participant.getFkBakeoff().getFood())
        .totalTaste(getTotalTaste(List.of(participant)))
        .totalAppearance(getTotalAppearance(List.of(participant)))
        .build();
  }

  private List<ResultDto> scoresFromParticipant(Participant participant) {
    return participant.getResults()
        .stream()
        .map(this::resulttoDto)
        .collect(Collectors.toList());
  }

  private ResultDto resulttoDto(Result result) {
    return ResultDto.builder()
        .entrantId(result.getFkParticipant().getEntrantId())
        .judgeName(result.getFkJudge().getJudgeName())
        .appearance(result.getAppearance())
        .taste(result.getTaste())
        .build();
  }
}
