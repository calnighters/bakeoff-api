package com.bakeoff.api.service;

import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.dto.ImageDto;
import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;

public interface AwsService {

  ImageDto downloadImage(BakeoffResponseDto bakeoffResponseDto, Integer entrantId, String bakeoffDate);

  void uploadImage(Integer entrantId, MultipartFile file);
}
