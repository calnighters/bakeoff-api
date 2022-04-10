package com.bakeoff.api.service;

import com.bakeoff.api.dto.BakeoffDto;
import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.ParticipantDto;
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
import java.util.List;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

  private Result createResult(Integer id, Participant participant, Judge judge, Integer taste, Integer appearance) {
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

//    getLatestBakeoff Tests
    @Test
    @DisplayName("When there is one bakeoff, ensure this is returned")
    void oneBakeoffReturned(
            @Injectable ResultRepository resultRepository,
            @Injectable BakeoffRepistory bakeoffRepistory,
            @Injectable JudgeRepository judgeRepository,
            @Injectable BakerRepository bakerRepository,
            @Injectable ParticipantRepository participantRepository,
            @Injectable Clock clock,
            @Tested ApiServiceImpl apiService
    ) {

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

  @Test
  @DisplayName("When there is multiple bakeoffs, ensure the latest is returned")
  void latestBakeoffReturned(
          @Injectable ResultRepository resultRepository,
          @Injectable BakeoffRepistory bakeoffRepistory,
          @Injectable JudgeRepository judgeRepository,
          @Injectable BakerRepository bakerRepository,
          @Injectable ParticipantRepository participantRepository,
          @Injectable Clock clock,
          @Tested ApiServiceImpl apiService
  ) {

    Judge judge1 = createJudge(1, "Zach");
    Judge judge2 = createJudge(2, "Bella");

    Baker baker1 = createBaker(1, "Callum");
    Baker baker2 = createBaker(2, "Harry");

    Bakeoff bakeoff = Bakeoff.builder()
            .boDate(LocalDate.of(2022, 1, 1))
            .food("Cheesecake")
            .boDate(LocalDate.of(2022, 2, 18))
            .food("Hot Cross Buns")
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
            () -> assertEquals(LocalDate.of(2022, 2, 18), response.getDate()),
            () -> assertEquals("Hot Cross Buns", response.getTitle()),
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

  @Test
  @DisplayName("When there is no bakeoff, ensure nothing is returned")
  void zeroBakeoffReturned(
          @Injectable ResultRepository resultRepository,
          @Injectable BakeoffRepistory bakeoffRepistory,
          @Injectable JudgeRepository judgeRepository,
          @Injectable BakerRepository bakerRepository,
          @Injectable ParticipantRepository participantRepository,
          @Injectable Clock clock,
          @Tested ApiServiceImpl apiService
  ) {

    Bakeoff bakeoff = Bakeoff.builder()
            .build();

    Judge judge1 = createJudge(1, "Zach");
    Judge judge2 = createJudge(2, "Bella");

    Baker baker1 = createBaker(1, "Callum");
    Baker baker2 = createBaker(2, "Harry");

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
      result = Collections.emptyList();
    }};

    BakeoffResponseDto actual = apiService.getLatestBakeoff();
    assertEquals(0, actual.getBakeoffs().size());
  }

//  getAllBakeoffs Tests
  @Test
  @DisplayName("Get all bake offs")
  void getAllBakeoffReturned(
          @Injectable ResultRepository resultRepository,
          @Injectable BakeoffRepistory bakeoffRepistory,
          @Injectable JudgeRepository judgeRepository,
          @Injectable BakerRepository bakerRepository,
          @Injectable ParticipantRepository participantRepository,
          @Injectable Clock clock,
          @Tested ApiServiceImpl apiService
  ) {

    Judge judge1 = createJudge(1, "Zach");
    Judge judge2 = createJudge(2, "Bella");

    Baker baker1 = createBaker(1, "Callum");
    Baker baker2 = createBaker(2, "Harry");

    Bakeoff bakeoff1 = Bakeoff.builder()
            .boDate(LocalDate.of(2022, 1, 1))
            .food("Cheesecake")
            .build();

    Bakeoff bakeoff2 = Bakeoff.builder()
            .boDate(LocalDate.of(2022, 2, 18))
            .food("Hot Cross Buns")
            .build();

    Participant participant1 = createParticipant(1, 1, "Vanilla", baker1, bakeoff1);
    Participant participant2 = createParticipant(2, 2, "Lemon", baker2, bakeoff2);
    bakeoff1.setParticipants(List.of(participant1));
    bakeoff2.setParticipants(List.of(participant2));

    Result result1 = createResult(1, participant1, judge1, 10, 5);
    Result result2 = createResult(2, participant1, judge2, 9, 4);
    Result result3 = createResult(3, participant2, judge1, 8, 3);
    Result result4 = createResult(4, participant2, judge2, 7, 2);
    participant1.setResults(List.of(result1, result2));
    participant2.setResults(List.of(result3, result4));

    List<Bakeoff> bakeoffs = List.of(bakeoff1, bakeoff2);

    new Expectations() {{
      bakeoffRepistory.findAll();
      result = bakeoffs;
    }};

    BakeoffResponseDto actual = apiService.getAllBakeoffs();
    assertEquals(2, actual.getBakeoffs().size());
  }

//  addBakeoff Tests
  @Test
  @DisplayName("Add bakeoff")
  void addBakeoff(
          @Injectable ResultRepository resultRepository,
          @Injectable BakeoffRepistory bakeoffRepistory,
          @Injectable JudgeRepository judgeRepository,
          @Injectable BakerRepository bakerRepository,
          @Injectable ParticipantRepository participantRepository,
          @Injectable Clock clock,
          @Tested ApiServiceImpl apiService
  ) {

    new Expectations() {{
      bakeoffRepistory.save(withInstanceOf(Bakeoff.class));
    }};

    apiService.addBakeoff("Add New Bake Off Name");

  }

//  updateBakeOff Tests
  @Test
  @DisplayName("Update bakeoff")
  void updateBakeoff(
          @Injectable ResultRepository resultRepository,
          @Injectable BakeoffRepistory bakeoffRepistory,
          @Injectable JudgeRepository judgeRepository,
          @Injectable BakerRepository bakerRepository,
          @Injectable ParticipantRepository participantRepository,
          @Injectable Clock clock,
          @Tested ApiServiceImpl apiService
  ) {

    Bakeoff bakeoff = Bakeoff.builder()
            .boDate(LocalDate.of(2022, 1, 1))
            .food("Cheesecake")
            .build();
    System.out.println(LocalDate.now(clock));

    new Expectations() {{
      bakeoffRepistory.findByBoDate(LocalDate.now(clock));
      bakeoffRepistory.save(withInstanceOf(Bakeoff.class));
    }};

    apiService.updateBakeOff("Updated Bake Off Name");

  }

  @Test
  @DisplayName("Update bakeoff - no bake off found")
  void noBakeoffFound(
          @Injectable ResultRepository resultRepository,
          @Injectable BakeoffRepistory bakeoffRepistory,
          @Injectable JudgeRepository judgeRepository,
          @Injectable BakerRepository bakerRepository,
          @Injectable ParticipantRepository participantRepository,
          @Injectable Clock clock,
          @Tested ApiServiceImpl apiService
  ) {

    Bakeoff bakeoff = Bakeoff.builder()
            .build();

    new Expectations() {{
      bakeoffRepistory.findByBoDate(LocalDate.now(clock))
              .orElseThrow(
                      () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    }};

    apiService.updateBakeOff("Updated Bake Off Name");

  }
  
//  deleteParticipant Tests
  
}