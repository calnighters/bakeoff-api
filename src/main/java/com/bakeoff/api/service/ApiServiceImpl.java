package com.bakeoff.api.service;

import com.bakeoff.api.dto.BakeoffDto;
import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.ParticipantDto;
import com.bakeoff.api.dto.ResultDto;
import com.bakeoff.api.exceptions.InvalidFormatException;
import com.bakeoff.api.exceptions.NotFoundException;
import com.bakeoff.api.model.Bakeoff;
import com.bakeoff.api.model.Participant;
import com.bakeoff.api.model.Result;
import com.bakeoff.api.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
