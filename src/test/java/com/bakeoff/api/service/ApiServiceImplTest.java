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
import com.bakeoff.api.dto.TotalResponseDto;
import com.bakeoff.api.dto.UpdatePersonDto;
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

  private Result createResult(Integer id, Participant participant, Judge judge, BigDecimal taste,
      BigDecimal appearance) {
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

      Result result1 = createResult(1, participant1, judge1, new BigDecimal(10), new BigDecimal(5));
      Result result2 = createResult(2, participant1, judge2, new BigDecimal(9), new BigDecimal(4));
      Result result3 = createResult(3, participant2, judge1, new BigDecimal(8), new BigDecimal(3));
      Result result4 = createResult(4, participant2, judge2, new BigDecimal(7), new BigDecimal(2));
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
          .taste(new BigDecimal(1))
          .appearance(new BigDecimal(1))
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
          .taste(new BigDecimal(1))
          .appearance(new BigDecimal(1))
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
          .taste(new BigDecimal(1))
          .appearance(new BigDecimal(1))
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
          .taste(new BigDecimal(1))
          .appearance(new BigDecimal(2))
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
        assertEquals(new BigDecimal(1), result.getTaste());
        assertEquals(new BigDecimal(2), result.getAppearance());
      }};

    }

  }

  @Nested
  @DisplayName("updateBaker method")
  class UpdateBakerMethod {

    @Test
    @DisplayName("When the method is called and a baker is found, then the baker name is updated.")
    void bakerUpdated(
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

      Baker baker = createBaker(1, "Callum");

      new Expectations() {{
        bakerRepository.findByBakerName(withInstanceOf(String.class));
        result = Optional.of(baker);
      }};

      apiService.updateBaker(UpdatePersonDto.builder()
          .oldName("Callum")
          .newName("Zach")
          .build());

      new Verifications() {{
        Baker updated;
        bakerRepository.save(updated = withCapture());

        assertEquals(1, updated.getId());
        assertEquals("Zach", updated.getBakerName());
      }};
    }

    @Test
    @DisplayName("When the method is called and a baker is not found, then error is thrown.")
    void bakerNotFound(
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
        bakerRepository.findByBakerName(withInstanceOf(String.class));
        result = Optional.empty();
      }};

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.updateBaker(UpdatePersonDto.builder()
              .oldName("Callum")
              .newName("Zach")
              .build()));

      assertEquals("Baker cannot be found with the name: Callum", e.getMessage());
    }

  }

  @Nested
  @DisplayName("updateJudge method")
  class UpdateJudgeMethod {

    @Test
    @DisplayName("When the method is called and a judge is found, then the judge name is updated.")
    void judgeUpdated(
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

      Judge judge = createJudge(1, "Callum");

      new Expectations() {{
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.of(judge);
      }};

      apiService.updateJudge(UpdatePersonDto.builder()
          .oldName("Callum")
          .newName("Zach")
          .build());

      new Verifications() {{
        Judge updated;
        judgeRepository.save(updated = withCapture());

        assertEquals(1, updated.getId());
        assertEquals("Zach", updated.getJudgeName());
      }};
    }

    @Test
    @DisplayName("When the method is called and a baker is not found, then error is thrown.")
    void judgeNotFound(
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
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.empty();
      }};

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.updateJudge(UpdatePersonDto.builder()
              .oldName("Callum")
              .newName("Zach")
              .build()));

      assertEquals("Judge cannot be found with the name: Callum", e.getMessage());
    }

  }

  @Nested
  @DisplayName("deleteBaker method")
  class DeleteBakerMethod {

    @Test
    @DisplayName("When the method is called and a baker is not found, then error is thrown.")
    void bakerDeleted(
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

      Bakeoff bakeoff1 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();
      Bakeoff bakeoff2 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Icecream")
          .build();

      Baker baker = createBaker(1, "Callum");

      Judge judge1 = createJudge(1, "Zach");
      Judge judge2 = createJudge(2, "Harry");

      Participant participant1 = createParticipant(1, 1, "Lemon", baker, bakeoff1);

      Result result1 = createResult(1, participant1, judge1, new BigDecimal(1), new BigDecimal(2));
      Result result2 = createResult(2, participant1, judge2, new BigDecimal(2), new BigDecimal(4));
      participant1.setResults(List.of(result1, result2));

      Participant participant2 = createParticipant(2, 1, "Vanilla", baker, bakeoff2);

      Result result3 = createResult(3, participant2, judge1, new BigDecimal(1), new BigDecimal(2));
      Result result4 = createResult(4, participant2, judge2, new BigDecimal(2), new BigDecimal(4));
      participant2.setResults(List.of(result3, result4));

      baker.setParticipants(List.of(participant1, participant2));

      new Expectations() {{
        bakerRepository.findByBakerName(withInstanceOf(String.class));
        result = Optional.of(baker);
      }};

      apiService.deleteBaker("Callum");

      new Verifications() {{
        List<Result> deletedResults;
        resultRepository.deleteAll(deletedResults = withCapture());
        assertEquals(4, deletedResults.size());
        assertEquals(result1, deletedResults.get(0));
        assertEquals(result2, deletedResults.get(1));
        assertEquals(result3, deletedResults.get(2));
        assertEquals(result4, deletedResults.get(3));

        List<Participant> deletedParticipants;
        participantRepository.deleteAll(deletedParticipants = withCapture());
        assertEquals(2, deletedParticipants.size());
        assertEquals(participant1, deletedParticipants.get(0));
        assertEquals(participant2, deletedParticipants.get(1));

        Baker deletedBaker;
        bakerRepository.delete(deletedBaker = withCapture());
        assertEquals(baker.getId(), deletedBaker.getId());
      }};
    }

    @Test
    @DisplayName("When the method is called and a baker is not found, then error is thrown.")
    void bakerNotFound(
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
        bakerRepository.findByBakerName(withInstanceOf(String.class));
        result = Optional.empty();
      }};

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.deleteBaker("Callum")
      );

      assertEquals("Baker cannot be found with the name: Callum", e.getMessage());
    }

  }

  @Nested
  @DisplayName("getTotals method")
  class GetTotalsMethod {

    @Test
    @DisplayName("When the method is called, then the baker and their events are returned.")
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

      Bakeoff bakeoff1 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();
      Bakeoff bakeoff2 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Icecream")
          .build();

      Baker baker = createBaker(1, "Callum");

      Judge judge1 = createJudge(1, "Zach");
      Judge judge2 = createJudge(2, "Harry");

      Participant participant1 = createParticipant(1, 1, "Lemon", baker, bakeoff1);

      Result result1 = createResult(1, participant1, judge1, new BigDecimal(1), new BigDecimal(2));
      Result result2 = createResult(2, participant1, judge2, new BigDecimal(2), new BigDecimal(4));
      participant1.setResults(List.of(result1, result2));

      Participant participant2 = createParticipant(2, 1, "Vanilla", baker, bakeoff2);

      Result result3 = createResult(3, participant2, judge1, new BigDecimal(1), new BigDecimal(2));
      Result result4 = createResult(4, participant2, judge2, new BigDecimal(2), new BigDecimal(4));
      participant2.setResults(List.of(result3, result4));

      baker.setParticipants(List.of(participant1, participant2));

      new Expectations() {{
        bakerRepository.findAll();
        result = List.of(baker);
      }};

      TotalResponseDto response = apiService.getTotals();

      assertAll(
          () -> assertEquals(1, response.getBakers().size()),
          () -> assertEquals(1, response.getBakers().get(0).getId()),
          () -> assertEquals("Callum", response.getBakers().get(0).getName()),
          () -> assertEquals(new BigDecimal(6), response.getBakers().get(0).getTotalTaste()),
          () -> assertEquals(new BigDecimal(12), response.getBakers().get(0).getTotalAppearance()),
          () -> assertEquals(2, response.getBakers().get(0).getEvents().size())
      );
    }

  }

  @Nested
  @DisplayName("updateResult method")
  class UpdateResultMethod {

    @Test
    @DisplayName("When the method is called, then the result is updated.")
    void resultUpdated(
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

      Bakeoff bakeoff1 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Callum");

      Judge judge1 = createJudge(1, "Zach");
      Judge judge2 = createJudge(2, "Harry");

      Participant participant1 = createParticipant(1, 1, "Lemon", baker, bakeoff1);

      Result result1 = createResult(1, participant1, judge1, new BigDecimal(1), new BigDecimal(2));
      Result result2 = createResult(2, participant1, judge2, new BigDecimal(2), new BigDecimal(4));
      participant1.setResults(List.of(result1, result2));

      baker.setParticipants(List.of(participant1));

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff1);
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.of(judge1);
        participantRepository.findByEntrantIdAndFkBakeoff(withInstanceOf(Integer.class),
            withInstanceOf(Bakeoff.class));
        result = Optional.of(participant1);
        resultRepository.findByFkJudgeAndFkParticipant(withInstanceOf(Judge.class),
            withInstanceOf(Participant.class));
        result = Optional.of(result1);
      }};

      apiService.updateResult(ResultDto.builder()
          .entrantId(1)
          .judgeName("Zach")
          .taste(new BigDecimal(7))
          .appearance(new BigDecimal(4))
          .build());

      new Verifications() {{
        Result result;
        resultRepository.save(result = withCapture());

        assertEquals(participant1, result.getFkParticipant());
        assertEquals(judge1, result.getFkJudge());
        assertEquals(new BigDecimal(7), result.getTaste());
        assertEquals(new BigDecimal(4), result.getAppearance());
      }};
    }

    @Test
    @DisplayName("When the method is called, but the result isn't found, then error is thrown.")
    void resultNotFound(
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

      Bakeoff bakeoff1 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Callum");

      Judge judge1 = createJudge(1, "Zach");
      Judge judge2 = createJudge(2, "Harry");

      Participant participant1 = createParticipant(1, 1, "Lemon", baker, bakeoff1);

      Result result2 = createResult(2, participant1, judge2, new BigDecimal(2), new BigDecimal(4));
      participant1.setResults(List.of(result2));

      baker.setParticipants(List.of(participant1));

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff1);
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.of(judge1);
        participantRepository.findByEntrantIdAndFkBakeoff(withInstanceOf(Integer.class),
            withInstanceOf(Bakeoff.class));
        result = Optional.of(participant1);
        resultRepository.findByFkJudgeAndFkParticipant(withInstanceOf(Judge.class),
            withInstanceOf(Participant.class));
        result = Optional.empty();
      }};

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.updateResult(ResultDto.builder()
              .entrantId(1)
              .judgeName("Zach")
              .taste(new BigDecimal(7))
              .appearance(new BigDecimal(4))
              .build())
      );

      assertEquals("Result not found for Judge: Zach and Entrant ID: 1", e.getMessage());
    }

    @Test
    @DisplayName("When the method is called, but the participant isn't found, then error is thrown.")
    void participantNotFound(
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

      Bakeoff bakeoff1 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Callum");

      Judge judge1 = createJudge(1, "Zach");
      Judge judge2 = createJudge(2, "Harry");

      baker.setParticipants(Collections.emptyList());

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff1);
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.of(judge1);
        participantRepository.findByEntrantIdAndFkBakeoff(withInstanceOf(Integer.class),
            withInstanceOf(Bakeoff.class));
        result = Optional.empty();
      }};

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.updateResult(ResultDto.builder()
              .entrantId(1)
              .judgeName("Zach")
              .taste(new BigDecimal(7))
              .appearance(new BigDecimal(4))
              .build())
      );

      assertEquals("Participant not found for Entrant ID: 1 and date: 2021-01-01", e.getMessage());
    }

    @Test
    @DisplayName("When the method is called, but the judge isn't found, then error is thrown.")
    void judgeNotFound(
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

      Bakeoff bakeoff1 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Callum");

      baker.setParticipants(Collections.emptyList());

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff1);
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.empty();
      }};

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.updateResult(ResultDto.builder()
              .entrantId(1)
              .judgeName("Zach")
              .taste(new BigDecimal(7))
              .appearance(new BigDecimal(4))
              .build())
      );

      assertEquals("Judge cannot be found with the name: Zach", e.getMessage());
    }

    @Test
    @DisplayName("When the method is called, but the bakeoff isn't found, then error is thrown.")
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
          () -> apiService.updateResult(ResultDto.builder()
              .entrantId(1)
              .judgeName("Zach")
              .taste(new BigDecimal(7))
              .appearance(new BigDecimal(4))
              .build())
      );

      assertEquals("Bakeoff not found for date: 2021-01-01", e.getMessage());
    }

  }

  @Nested
  @DisplayName("deleteResult method")
  class DeleteResultMethod {

    @Test
    @DisplayName("When the method is called, then the result is deleted.")
    void resultUpdated(
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

      Bakeoff bakeoff1 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Callum");

      Judge judge1 = createJudge(1, "Zach");
      Judge judge2 = createJudge(2, "Harry");

      Participant participant1 = createParticipant(1, 1, "Lemon", baker, bakeoff1);

      Result result1 = createResult(1, participant1, judge1, new BigDecimal(1), new BigDecimal(2));
      Result result2 = createResult(2, participant1, judge2, new BigDecimal(2), new BigDecimal(4));
      participant1.setResults(List.of(result1, result2));

      baker.setParticipants(List.of(participant1));

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff1);
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.of(judge1);
        participantRepository.findByEntrantIdAndFkBakeoff(withInstanceOf(Integer.class),
            withInstanceOf(Bakeoff.class));
        result = Optional.of(participant1);
        resultRepository.findByFkJudgeAndFkParticipant(withInstanceOf(Judge.class),
            withInstanceOf(Participant.class));
        result = Optional.of(result1);
      }};

      apiService.deleteResult(ResultDto.builder()
          .entrantId(1)
          .judgeName("Zach")
          .taste(new BigDecimal(1))
          .appearance(new BigDecimal(2))
          .build());

      new Verifications() {{
        Result result;
        resultRepository.delete(result = withCapture());

        assertEquals(participant1, result.getFkParticipant());
        assertEquals(judge1, result.getFkJudge());
        assertEquals(new BigDecimal(1), result.getTaste());
        assertEquals(new BigDecimal(2), result.getAppearance());
      }};
    }

    @Test
    @DisplayName("When the method is called, but the result isn't found, then error is thrown.")
    void resultNotFound(
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

      Bakeoff bakeoff1 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Callum");

      Judge judge1 = createJudge(1, "Zach");
      Judge judge2 = createJudge(2, "Harry");

      Participant participant1 = createParticipant(1, 1, "Lemon", baker, bakeoff1);

      Result result2 = createResult(2, participant1, judge2, new BigDecimal(2), new BigDecimal(4));
      participant1.setResults(List.of(result2));

      baker.setParticipants(List.of(participant1));

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff1);
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.of(judge1);
        participantRepository.findByEntrantIdAndFkBakeoff(withInstanceOf(Integer.class),
            withInstanceOf(Bakeoff.class));
        result = Optional.of(participant1);
        resultRepository.findByFkJudgeAndFkParticipant(withInstanceOf(Judge.class),
            withInstanceOf(Participant.class));
        result = Optional.empty();
      }};

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.deleteResult(ResultDto.builder()
              .entrantId(1)
              .judgeName("Zach")
              .taste(new BigDecimal(7))
              .appearance(new BigDecimal(4))
              .build())
      );

      assertEquals("Result not found for Judge: Zach and Entrant ID: 1", e.getMessage());
    }

    @Test
    @DisplayName("When the method is called, but the participant isn't found, then error is thrown.")
    void participantNotFound(
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

      Bakeoff bakeoff1 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Callum");

      Judge judge1 = createJudge(1, "Zach");
      Judge judge2 = createJudge(2, "Harry");

      baker.setParticipants(Collections.emptyList());

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff1);
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.of(judge1);
        participantRepository.findByEntrantIdAndFkBakeoff(withInstanceOf(Integer.class),
            withInstanceOf(Bakeoff.class));
        result = Optional.empty();
      }};

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.deleteResult(ResultDto.builder()
              .entrantId(1)
              .judgeName("Zach")
              .taste(new BigDecimal(7))
              .appearance(new BigDecimal(4))
              .build())
      );

      assertEquals("Participant not found for Entrant ID: 1 and date: 2021-01-01", e.getMessage());
    }

    @Test
    @DisplayName("When the method is called, but the judge isn't found, then error is thrown.")
    void judgeNotFound(
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

      Bakeoff bakeoff1 = Bakeoff.builder()
          .id(1)
          .boDate(LocalDate.of(2021, 1, 1))
          .food("Cheesecake")
          .build();

      Baker baker = createBaker(1, "Callum");

      baker.setParticipants(Collections.emptyList());

      new Expectations() {{
        bakeoffRepistory.findByBoDate(withInstanceOf(LocalDate.class));
        result = Optional.of(bakeoff1);
        judgeRepository.findByJudgeName(withInstanceOf(String.class));
        result = Optional.empty();
      }};

      NotFoundException e = assertThrows(NotFoundException.class,
          () -> apiService.deleteResult(ResultDto.builder()
              .entrantId(1)
              .judgeName("Zach")
              .taste(new BigDecimal(7))
              .appearance(new BigDecimal(4))
              .build())
      );

      assertEquals("Judge cannot be found with the name: Zach", e.getMessage());
    }

    @Test
    @DisplayName("When the method is called, but the bakeoff isn't found, then error is thrown.")
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
          () -> apiService.deleteResult(ResultDto.builder()
              .entrantId(1)
              .judgeName("Zach")
              .taste(new BigDecimal(7))
              .appearance(new BigDecimal(4))
              .build())
      );

      assertEquals("Bakeoff not found for date: 2021-01-01", e.getMessage());
    }

  }
}