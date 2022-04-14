package com.bakeoff.api.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bakeoff.api.dto.BakeoffDto;
import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.BakerResponseDto;
import com.bakeoff.api.dto.JudgeResponseDto;
import com.bakeoff.api.dto.ParticipantDto;
import com.bakeoff.api.dto.ResultDto;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Verifications;
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
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

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
          () -> assertEquals("Vanilla", p1.getDescription()),
          () -> assertEquals(2, p2.getEntrantId()),
          () -> assertEquals("Harry", p2.getName()),
          () -> assertEquals(2, p2.getResults().size()),
          () -> assertEquals("Lemon", p2.getDescription())
      );
    }
  }

  @Nested
  @DisplayName("addBaker method")
  class AddBakerMethod {

    @Test
    @DisplayName("When the method is called then the baker is added")
    void bakerAdded(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      new Expectations() {{
        bakerRepository.save(withInstanceOf(Baker.class));
      }};

      apiService.addBaker("callum");

      new Verifications() {{
        Baker baker;
        bakerRepository.save(baker = withCapture());
        assertEquals("callum", baker.getBakerName());
      }};
    }

  }

  @Nested
  @DisplayName("addJudge method")
  class AddJudgeMethod {

    @Test
    @DisplayName("When the method is called then the judge is added")
    void judgeAdded(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      Bakeoff bakeoff = Bakeoff.builder()
          .id(1)
          .food("Cheesecake")
          .boDate(LocalDate.of(2021, 1, 1))
          .build();

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff);
        judgeRepository.save(withInstanceOf(Judge.class));
      }};

      apiService.addJudge("callum");

      new Verifications() {{
        Judge judge;
        judgeRepository.save(judge = withCapture());
        assertEquals("callum", judge.getJudgeName());
      }};
    }

    @Test
    @DisplayName("When the method is called but there is no current bakeoff, exception is thrown")
    void bakeoffNotFound(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.empty();
      }};

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.addJudge("callum"));

      assertEquals("Bakeoff not found for date: 2021-01-01", e.getMessage());
    }

  }

  @Nested
  @DisplayName("getBakers method")
  class GetBakersMethod {

    @Test
    @DisplayName("When the method is called then all bakers are returned")
    void bakersReturned(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      Baker baker1 = createBaker(1, "Callum");
      Baker baker2 = createBaker(2, "Zach");
      Baker baker3 = createBaker(3, "Harry");

      new Expectations() {{
        bakerRepository.findAll();
        result = List.of(baker1, baker2, baker3);
      }};

      BakerResponseDto response = apiService.getBakers();

      assertEquals(3, response.getBakers().size());
      assertEquals("Callum", response.getBakers().get(0).getName());
      assertEquals("Zach", response.getBakers().get(1).getName());
      assertEquals("Harry", response.getBakers().get(2).getName());

    }

    @Test
    @DisplayName("When the method is called and there are no bakers, then an ampty list is returned")
    void noBakers(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      new Expectations() {{
        bakerRepository.findAll();
        result = Collections.emptyList();
      }};

      BakerResponseDto response = apiService.getBakers();

      assertEquals(0, response.getBakers().size());

    }

  }

  @Nested
  @DisplayName("getJudges method")
  class GetJudgesMethod {

    @Test
    @DisplayName("When the method is called then all judges are returned")
    void judgesReturned(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      Judge judge1 = createJudge(1, "Callum");
      Judge judge2 = createJudge(2, "Zach");
      Judge judge3 = createJudge(3, "Harry");

      new Expectations() {{
        judgeRepository.findAll();
        result = List.of(judge1, judge2, judge3);
      }};

      JudgeResponseDto response = apiService.getJudges();

      assertEquals(3, response.getJudges().size());
      assertEquals("Callum", response.getJudges().get(0).getName());
      assertEquals("Zach", response.getJudges().get(1).getName());
      assertEquals("Harry", response.getJudges().get(2).getName());

    }

    @Test
    @DisplayName("When the method is called and there are no bakers, then an ampty list is returned")
    void noJudges(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      new Expectations() {{
        judgeRepository.findAll();
        result = Collections.emptyList();
      }};

      JudgeResponseDto response = apiService.getJudges();

      assertEquals(0, response.getJudges().size());

    }

  }

  @Nested
  @DisplayName("addParticipant method")
  class AddParticipantMethod {

    @Test
    @DisplayName("When the method is called, then the participant is added")
    void participantAdded(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      Bakeoff bakeoff = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Callum");

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff);
        bakerRepository.findById(withInstanceOf(Integer.class));
        result = Optional.of(baker);
        participantRepository.save(withInstanceOf(Participant.class));
      }};

      ParticipantDto participantDto = ParticipantDto.builder()
          .entrantId(1)
          .bakerId(1)
          .description("Vanilla")
          .build();

      apiService.addParticipant(participantDto);

      new Verifications() {{
        Participant participant;
        participantRepository.save(participant = withCapture());

        assertEquals(bakeoff, participant.getFkBakeoff());
        assertEquals(baker, participant.getFkBaker());
        assertEquals("Vanilla", participant.getDescription());
        assertEquals(1, participant.getEntrantId());
      }};

    }

    @Test
    @DisplayName("When the method is called, and there is no bakeoff, then error is thrown")
    void noBakeOff(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.empty();
      }};

      ParticipantDto participantDto = ParticipantDto.builder()
          .entrantId(1)
          .bakerId(1)
          .description("Vanilla")
          .build();

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.addParticipant(participantDto));

      assertEquals("Bakeoff not found for date: 2021-01-01", e.getMessage());

    }

    @Test
    @DisplayName("When the method is called, and there is no baker, then error is thrown")
    void noBaker(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      Bakeoff bakeoff = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff);
        bakerRepository.findById(withInstanceOf(Integer.class));
        result = Optional.empty();
      }};

      ParticipantDto participantDto = ParticipantDto.builder()
          .entrantId(1)
          .bakerId(1)
          .description("Vanilla")
          .build();

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.addParticipant(participantDto));

      assertEquals("Baker not found for ID: 1", e.getMessage());

    }

  }

  @Nested
  @DisplayName("addResult method")
  class AddResultMethod {

    @Test
    @DisplayName("When the method is called, and there is no bakeoff, then error is thrown")
    void noBakeoff(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.empty();
      }};

      ResultDto resultDto = ResultDto.builder()
          .entrantId(1)
          .judgeName("Callum")
          .taste(1)
          .appearance(1)
          .build();

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.addResult(resultDto));

      assertEquals("Bakeoff not found for date: 2021-01-01", e.getMessage());

    }

    @Test
    @DisplayName("When the method is called, and there is no matching participant, then error is thrown")
    void noParticipant(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      Bakeoff bakeoff = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff);
        participantRepository.findByEntrantIdAndFkBakeoff(withInstanceOf(Integer.class),
            withInstanceOf(Bakeoff.class));
        result = Optional.empty();
      }};

      ResultDto resultDto = ResultDto.builder()
          .entrantId(1)
          .judgeName("Callum")
          .taste(1)
          .appearance(1)
          .build();

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.addResult(resultDto));

      assertEquals("Participant not found for Entrant ID: 1 and date: 2021-01-01", e.getMessage());

    }

    @Test
    @DisplayName("When the method is called, and there is no matching judge, then error is thrown")
    void noJudge(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      Bakeoff bakeoff = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Harry");

      Participant participant = createParticipant(1, 1, "Lemon", baker, bakeoff);

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff);
        participantRepository.findByEntrantIdAndFkBakeoff(withInstanceOf(Integer.class),
            withInstanceOf(Bakeoff.class));
        result = Optional.of(participant);
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.empty();
      }};

      ResultDto resultDto = ResultDto.builder()
          .entrantId(1)
          .judgeName("Callum")
          .taste(1)
          .appearance(1)
          .build();

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.addResult(resultDto));

      assertEquals("Judge not found for name: Callum", e.getMessage());

    }

    @Test
    @DisplayName("When the method is called with a valid object, then a result is created")
    void resultAdded(
        @Injectable ResultRepository resultRepository,
        @Injectable BakeoffRepistory bakeoffRepistory,
        @Injectable JudgeRepository judgeRepository,
        @Injectable BakerRepository bakerRepository,
        @Injectable ParticipantRepository participantRepository,
        @Injectable JudgeHistoryRepository judgeHistoryRepository
    ) {
      Clock clock = Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
      ApiService apiService = new ApiServiceImpl(resultRepository, bakerRepository, judgeRepository,
          bakeoffRepistory, participantRepository, judgeHistoryRepository, clock);

      Bakeoff bakeoff = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Harry");

      Participant participant = createParticipant(1, 1, "Lemon", baker, bakeoff);

      Judge judge = createJudge(1, "Callum");

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff);
        participantRepository.findByEntrantIdAndFkBakeoff(withInstanceOf(Integer.class),
            withInstanceOf(Bakeoff.class));
        result = Optional.of(participant);
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.of(judge);
        judgeHistoryRepository.findByFkBakeoffAndFkJudge(withInstanceOf(Bakeoff.class),
            withInstanceOf(Judge.class));
        result = Optional.empty();
      }};

      ResultDto resultDto = ResultDto.builder()
          .entrantId(1)
          .judgeName("Callum")
          .taste(1)
          .appearance(2)
          .build();

      apiService.addResult(resultDto);

      new Verifications() {{
        JudgeHistory judgeHistory;
        judgeHistoryRepository.save(judgeHistory = withCapture());

        assertEquals(judge, judgeHistory.getFkJudge());
        assertEquals(bakeoff, judgeHistory.getFkBakeoff());

        Result result;
        resultRepository.save(result = withCapture());

        assertEquals(judge, result.getFkJudge());
        assertEquals(participant, result.getFkParticipant());
        assertEquals(1, result.getTaste());
        assertEquals(2, result.getAppearance());
      }};

    }

  }
}