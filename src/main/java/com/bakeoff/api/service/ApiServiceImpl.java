package com.bakeoff.api.service;

import com.bakeoff.api.dto.BakeoffDto;
import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.BakerResponseDto;
import com.bakeoff.api.dto.JudgeResponseDto;
import com.bakeoff.api.dto.ParticipantDto;
import com.bakeoff.api.dto.PersonDto;
import com.bakeoff.api.dto.ResultDto;
import com.bakeoff.api.exceptions.InvalidFormatException;
import com.bakeoff.api.exceptions.NotFoundException;
import com.bakeoff.api.model.Bakeoff;
import com.bakeoff.api.model.Baker;
import com.bakeoff.api.model.Judge;
import com.bakeoff.api.model.Participant;
import com.bakeoff.api.model.Result;
import com.bakeoff.api.repositories.BakeoffRepistory;
import com.bakeoff.api.repositories.BakerRepository;
import com.bakeoff.api.repositories.JudgeRepository;
import com.bakeoff.api.repositories.ParticipantRepository;
import com.bakeoff.api.repositories.ResultRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
        .collect(Collectors.toList());
    return
        BakerResponseDto.builder()
            .bakers(
                bakers.stream()
                    .map(this::bakerToDto)
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
            .fkBakeoff(bakeoff)
            .build()
    );
  }

  @Override
  public JudgeResponseDto getJudges() {
    List<Judge> judges = StreamSupport
        .stream(judgeRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
    return
        JudgeResponseDto.builder()
            .judges(
                judges.stream()
                    .map(this::judgeToDto)
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

  private PersonDto judgeToDto(Judge judge) {
    return PersonDto.builder()
        .id(judge.getId())
        .name(judge.getJudgeName())
        .build();
  }

  private PersonDto bakerToDto(Baker baker) {
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
        .name(participant.getFkBaker().getBakerName())
        .results(scoresFromParticipant(participant))
        .description(participant.getDescription())
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
