package com.bakeoff.api.controller;

import com.bakeoff.api.dto.ImageDto;
import com.bakeoff.api.service.ApiService;
import com.bakeoff.api.service.AwsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = {"https://bakeoff-web.herokuapp.com", "http://localhost:3000"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/aws")
public class AwsController {

  private final AwsService awsService;
  private final ApiService apiService;

  @GetMapping(path = "/download/{entrantId}")
  public ImageDto downloadImageForEntrant(@PathVariable Integer entrantId,
      @RequestParam(required = false) String bakeoffDate) {
    return awsService.downloadImage(apiService.getLatestBakeoff(), entrantId, bakeoffDate);
  }

  @PostMapping(
      path = "/upload/{entrantId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public void uploadImage(@PathVariable Integer entrantId,
      @RequestParam("file") MultipartFile file) {
    awsService.uploadImage(entrantId, file);
  }

}
