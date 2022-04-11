package com.bakeoff.api;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpStatusCodeException;

@DBRider
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("IT Testing Bakeoff API")
public class BakeoffApiIT {

  private static final String ROOT_URL = "/bakeoff/";
  private static final HttpHeaders headers = new HttpHeaders();

  @Autowired
  private TestRestTemplate testRestTemplate;

  @BeforeEach
  @DataSet(cleanBefore = true)
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

  @Test
  @DataSet({"test.xml"})
  void test() throws JSONException {
    ResponseEntity<String> result = callService(ROOT_URL, HttpMethod.GET, null);
    JSONAssert.assertEquals(TestUtils.getResource("/response.json"), result.getBody(), true);
  }
}
