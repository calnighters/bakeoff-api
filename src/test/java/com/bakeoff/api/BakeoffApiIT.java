package com.bakeoff.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpStatusCodeException;

@DBRider
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
    "spring.main.allow-bean-definition-overriding=true"})
@DisplayName("IT Testing Bakeoff API")
public class BakeoffApiIT {

  private static final String ROOT_URL = "/bakeoff/";
  private static final HttpHeaders headers = new HttpHeaders();

  @Autowired
  private TestRestTemplate testRestTemplate;

  @BeforeEach
  private void setHeaders() {
    headers.clear();
    headers.setContentType(MediaType.APPLICATION_JSON);
  }

  private ResponseEntity<String> callService(String url, HttpMethod method, HttpEntity httpEntity) {
    try {
      return testRestTemplate.exchange(url, method, httpEntity, String.class);
    } catch (HttpStatusCodeException ex) {
      return new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST - add baker")
  class AddBaker {

    @Test
    @DisplayName("When a POST request is sent to add a baker, the baker is added.")
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(value = {"/data/api/output/bakerAdded/bakerAdded.xml"}, ignoreCols = "ID")
    void bakerAdded() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "baker?name=Callum", HttpMethod.POST,
          null);
      assertEquals(HttpStatus.OK, result.getStatusCode());
    }

  }

  @Nested
  @DisplayName("POST - add judge")
  class AddJudge {

    @Test
    @DisplayName("When a POST request is sent to add a judge, the judge is added.")
    @DataSet(value = "/data/api/input/addJudge/bakeoff.xml", cleanBefore = true)
    @ExpectedDataSet(value = {"/data/api/output/judgeAdded/judgeAdded.xml"}, ignoreCols = "ID")
    void judgeAdded() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "judge?name=Callum", HttpMethod.POST,
          null);
      assertEquals(HttpStatus.OK, result.getStatusCode());
    }

  }

  @Nested
  @DisplayName("POST - add participant")
  class AddParticipant {

    @Test
    @DisplayName("When a POST request is sent to add a participant, then the participant is added")
    @DataSet(value = "/data/api/input/addParticipant/data.xml", cleanBefore = true)
    @ExpectedDataSet(value = {
        "/data/api/output/addParticipant/participantAdded.xml"}, ignoreCols = "ID")
    void judgeAdded() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "participant", HttpMethod.POST,
          new HttpEntity(
              TestUtils.getResource("/data/api/input/addParticipant/addParticipant.json"),
              headers));
      assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("When a POST request is sent to add a participant but there is no valid bakeoff, then error is returned")
    @DataSet(cleanBefore = true)
    void noBakeoff() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "participant", HttpMethod.POST,
          new HttpEntity(
              TestUtils.getResource("/data/api/input/addParticipant/addParticipant.json"),
              headers));
      assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
      assertTrue(result.getBody().contains("Bakeoff not found for date: 2021-01-01"));
    }

    @Test
    @DisplayName("When a POST request is sent to add a participant but there is no baker, then error is returned")
    @DataSet(value = "/data/api/input/addParticipant/noBaker.xml", cleanBefore = true)
    void noBaker() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "participant", HttpMethod.POST,
          new HttpEntity(
              TestUtils.getResource("/data/api/input/addParticipant/addParticipant.json"),
              headers));
      assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
      assertTrue(result.getBody().contains("Baker not found for ID: 1"));
    }

  }

  @Nested
  @DisplayName("GET - get bakers")
  class GetBakers {

    @Test
    @DisplayName("When a GET request is sent to get all bakers, the bakers are returned")
    @DataSet(value = {"/data/api/input/getBakers/bakers.xml"}, cleanBefore = true)
    void bakersReturned() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "baker", HttpMethod.GET, null);
      assertEquals(HttpStatus.OK, result.getStatusCode());
      JSONAssert.assertEquals(
          TestUtils.getResource("/data/api/output/getBakers/bakersReturned.json"), result.getBody(),
          true);
    }

    @Test
    @DataSet(cleanBefore = true)
    @DisplayName("When a GET request is sent to get all bakers, but there are none, then an empty list is returned")
    void noBakersReturned() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "baker", HttpMethod.GET, null);
      assertEquals(HttpStatus.OK, result.getStatusCode());
      JSONAssert.assertEquals(
          TestUtils.getResource("/data/api/output/getBakers/noBakers.json"), result.getBody(),
          true);
    }

  }

  @Nested
  @DisplayName("GET - get judges")
  class GetJudges {

    @Test
    @DisplayName("When a GET request is sent to get all bakers, the bakers are returned")
    @DataSet(value = {"/data/api/input/getJudges/judges.xml"}, cleanBefore = true)
    void judgesReturned() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "judge", HttpMethod.GET, null);
      assertEquals(HttpStatus.OK, result.getStatusCode());
      JSONAssert.assertEquals(
          TestUtils.getResource("/data/api/output/getJudges/judgesReturned.json"), result.getBody(),
          true);
    }

    @Test
    @DataSet(cleanBefore = true)
    @DisplayName("When a GET request is sent to get all judges, but there are none, then an empty list is returned")
    void noJudgesReturned() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "judge", HttpMethod.GET, null);
      assertEquals(HttpStatus.OK, result.getStatusCode());
      JSONAssert.assertEquals(
          TestUtils.getResource("/data/api/output/getJudges/noJudges.json"), result.getBody(),
          true);
    }

  }

  @Nested
  @DisplayName("POST - add result")
  class AddResult {

    @Test
    @DisplayName("When a POST request is sent to add a result, the result is added.")
    @DataSet(value = "/data/api/input/addResult/valid.xml", cleanBefore = true)
    @ExpectedDataSet(value = {"/data/api/output/addResult/validResponse.xml"}, ignoreCols = "ID")
    void resultAdded() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "result", HttpMethod.POST,
          new HttpEntity<>(TestUtils.getResource("/data/api/input/addResult/valid.json"), headers));
      assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("When a POST request is sent to add a result, the result is added.")
    @DataSet(value = "/data/api/input/addResult/valid.xml", cleanBefore = true)
    @ExpectedDataSet(value = {"/data/api/output/addResult/validResponseTwo.xml"}, ignoreCols = "ID")
    void twoResultsAdded() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "result", HttpMethod.POST,
          new HttpEntity<>(TestUtils.getResource("/data/api/input/addResult/valid.json"), headers));
      assertEquals(HttpStatus.OK, result.getStatusCode());
      result = callService(ROOT_URL + "result", HttpMethod.POST,
          new HttpEntity<>(TestUtils.getResource("/data/api/input/addResult/valid2.json"), headers));
      assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("When a POST request is sent to add a result, the result is added.")
    @DataSet(value = "/data/api/input/addResult/validTwoBakers.xml", cleanBefore = true)
    @ExpectedDataSet(value = {"/data/api/output/addResult/validResponseTwoBakers.xml"}, ignoreCols = "ID")
    void twoBakers() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "result", HttpMethod.POST,
          new HttpEntity<>(TestUtils.getResource("/data/api/input/addResult/valid.json"), headers));
      assertEquals(HttpStatus.OK, result.getStatusCode());
      result = callService(ROOT_URL + "result", HttpMethod.POST,
          new HttpEntity<>(TestUtils.getResource("/data/api/input/addResult/valid3.json"), headers));
      assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("When a POST request is sent to add result, but there is no bakeoff, ensure error thrown")
    @DataSet(cleanBefore = true)
    void noBakeoff() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "result", HttpMethod.POST,
          new HttpEntity<>(TestUtils.getResource("/data/api/input/addResult/valid.json"), headers));
      assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
      assertTrue(result.getBody().contains("Bakeoff not found for date: 2021-01-01"));
    }

    @Test
    @DisplayName("When a POST request is sent to add result, but there is no bakeoff, ensure error thrown")
    @DataSet(value = "/data/api/input/addResult/noParticipant.xml", cleanBefore = true)
    void noParticipant() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "result", HttpMethod.POST,
          new HttpEntity<>(TestUtils.getResource("/data/api/input/addResult/valid.json"), headers));
      assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
      assertTrue(result.getBody().contains("Participant not found for Entrant ID: 1 and date: 2021-01-01"));
    }

    @Test
    @DisplayName("When a POST request is sent to add result, but there is no bakeoff, ensure error thrown")
    @DataSet(value = "/data/api/input/addResult/noJudge.xml", cleanBefore = true)
    void noJudge() throws JSONException {
      ResponseEntity<String> result = callService(ROOT_URL + "result", HttpMethod.POST,
          new HttpEntity<>(TestUtils.getResource("/data/api/input/addResult/valid.json"), headers));
      assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
      assertTrue(result.getBody().contains("Judge not found for name: Callum"));
    }

  }

  @TestConfiguration
  public static class TestConfig {

    @Bean
    public Clock clock() {
      return Clock.fixed(Instant.parse("2021-01-01T10:10:10.00Z"), ZoneId.systemDefault());
    }

  }
}
