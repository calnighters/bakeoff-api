package com.bakeoff.api.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bakeoff.api.dto.BakeoffDto;
import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.ParticipantDto;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import mockit.Expectations;
import mockit.Injectable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ApiServiceImplTest {

  private Judge createJudge(Integer id, String name) {
    return Judge.builder()
        .id(id)
        .judgeName(name)
        .build();
  }

  private Baker createBaker(Integer id, String name) {
    return Baker.builder()
        .id(id)
        .bakerName(name)
        .build();
  }

  private Participant createParticipant(Integer id, Integer entrantId, String description,
      Baker baker, Bakeoff bakeoff) {
    return Participant.builder()
        .id(id)
        .entrantId(entrantId)
        .description(description)
        .fkBaker(baker)
        .fkBakeoff(bakeoff)
        .build();
  }

  private Result createResult(Integer id, Participant participant, Judge judge, Integer taste,
      Integer appearance) {
    return Result.builder()
        .id(id)
        .fkParticipant(participant)
        .fkJudge(judge)
        .taste(taste)
        .appearance(appearance)
        .build();
  }

  @Nested
  @DisplayName("getLatestBakeOff method")
  class GetLatestBakeOffMethod {

    @Test
    @DisplayName("When there is one bakeoff, ensure this is returned")
    void oneBakeoffReturned(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, clock);
      
      Judge judge1 = createJudge(1, "Zach");
      Judge judge2 = createJudge(2, "Bella");

      Baker baker1 = createBaker(1, "Callum");
      Baker baker2 = createBaker(2, "Harry");

      Bakeoff bakeoff = Bakeoff.builder()
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Participant participant1 = createParticipant(1, 1, "Vanilla", baker1, bakeoff);
      Participant participant2 = createParticipant(2, 2, "Lemon", baker2, bakeoff);
      bakeoff.setParticipants(List.of(participant1, participant2));

      Result result1 = createResult(1, participant1, judge1, 10, 5);
      Result result2 = createResult(2, participant1, judge2, 9, 4);
      Result result3 = createResult(3, participant2, judge1, 8, 3);
      Result result4 = createResult(4, participant2, judge2, 7, 2);
      participant1.setResults(List.of(result1, result2));
      participant2.setResults(List.of(result3, result4));

      List<Bakeoff> bakeoffs = List.of(bakeoff);

      new Expectations() {{
        bakeoffRepistory.findAll();
        result = bakeoffs;
      }};

      BakeoffResponseDto actual = apiService.getLatestBakeoff();
      assertEquals(1, actual.getBakeoffs().size());

      BakeoffDto response = actual.getBakeoffs().get(0);
      assertAll(
          () -> assertEquals(LocalDate.of(2021, 1, 1), response.getDate()),
          () -> assertEquals("Cheesecake", response.getTitle()),
          () -> assertEquals(2, response.getParticipants().size())
      );

      ParticipantDto p1 = response.getParticipants().get(0);
      ParticipantDto p2 = response.getParticipants().get(1);
      assertAll(
          () -> assertEquals(1, p1.getEntrantId()),
          () -> assertEquals("Callum", p1.getName()),
          () -> assertEquals(2, p1.getResults().size()),
          () -> assertEquals(2, p2.getEntrantId()),
          () -> assertEquals("Harry", p2.getName()),
          () -> assertEquals(2, p2.getResults().size())
      );
    }
  }
}