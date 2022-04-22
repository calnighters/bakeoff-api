package com.bakeoff.api.service;

import static org.apache.http.entity.ContentType.*;

import com.bakeoff.api.config.AwsConfig;
import com.bakeoff.api.dto.BakeoffResponseDto;
import com.bakeoff.api.exceptions.NotFoundException;
import com.bakeoff.api.model.Bakeoff;
import com.bakeoff.api.model.Participant;
import com.bakeoff.api.repositories.BakerRepository;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.utils.IoUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import com.bakeoff.api.dto.ImageDto;
import com.bakeoff.api.repositories.BakeoffRepistory;
import com.bakeoff.api.repositories.ParticipantRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class AwsServiceImpl implements AwsService {

  private static final String KEY_FORMAT = "BAKER%s/%s";

  private final S3Client s3Client;
  private final AwsConfig awsConfig;
  private final ParticipantRepository participantRepository;
  private final BakeoffRepistory bakeoffRepistory;
  private final BakerRepository bakerRepository;
  private final Clock clock;

  @Override
  public ImageDto downloadImage(BakeoffResponseDto bakeoffResponseDto, Integer entrantId,
      String bakeoffDate) {
    LocalDate date = bakeoffResponseDto.getBakeoffs().get(0).getDate();
    if (bakeoffDate != null) {
      date = LocalDate.parse(bakeoffDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    Bakeoff bakeoff = bakeoffRepistory.findByBoDate(date)
        .orElseThrow(
            () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    Participant participant = bakeoff.getParticipants().stream()
        .filter(p -> Objects.equals(p.getEntrantId(), entrantId))
        .findFirst()
        .orElseThrow(() -> new NotFoundException(
            "No participant found with entrant id: " + entrantId + "and date: "
                + bakeoff.getBoDate()));

    return ImageDto.builder()
        .image(download(String.format("BAKER%s/%s", participant.getFkBaker().getId(),
            participant.getImageName()))).build();
  }

  @Override
  public void uploadImage(Integer entrantId,
      MultipartFile file) {
    isFileEmpty(file);
    isImage(file);

    Bakeoff bakeoff = bakeoffRepistory.findByBoDate(LocalDate.now(clock))
        .orElseThrow(
            () -> new NotFoundException("Bakeoff not found for date: " + LocalDate.now(clock)));
    Participant participant = bakeoff.getParticipants().stream()
        .filter(p -> Objects.equals(p.getEntrantId(), entrantId))
        .findFirst()
        .orElseThrow(() -> new NotFoundException(
            "No participant found with entrant id: " + entrantId + " and date: "
                + bakeoff.getBoDate()));

    Map<String, String> metadata = extractMetadata(file);

    String filename = UUID.randomUUID().toString();
    String oldkey = String.format(KEY_FORMAT, participant.getFkBaker().getId(),
        participant.getImageName());
    String newKey = String.format(KEY_FORMAT, participant.getFkBaker().getId(), filename);

    delete(oldkey);
    try {
      upload(newKey, metadata, file.getInputStream());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    participant.setImageName(filename);
  }

  private void isFileEmpty(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalStateException("Cannot upload empty file [ " + file.getSize() + "]");
    }
  }

  private void isImage(MultipartFile file) {
    if (!Arrays.asList(
            IMAGE_JPEG.getMimeType(),
            IMAGE_PNG.getMimeType()
        )
        .contains(file.getContentType())) {
      throw new IllegalStateException("File must be an image [" + file.getContentType() + "]");
    }
  }

  private Map<String, String> extractMetadata(MultipartFile file) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("Content-Type", file.getContentType());
    metadata.put("Content-Length", String.valueOf(file.getSize()));
    return metadata;
  }

  private byte[] download(String key) {
    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(awsConfig.getBucketName())
        .key(key)
        .build();
    try {
      ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
      return IoUtils.toByteArray(response);
    } catch (NoSuchKeyException e) {
      throw new NotFoundException("No file in bucket called '" + key + "'");
    } catch (IOException e) {
      throw new IllegalStateException("Failed to download file to s3", e);
    }
  }

  private void upload(String key, Map<String, String> metadata,
      InputStream inputStream)
      throws IOException {
    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(awsConfig.getBucketName())
        .key(key)
        .metadata(metadata)
        .build();
    try {
      s3Client.putObject(request,
          RequestBody.fromInputStream(inputStream, inputStream.available()));
    } catch (S3Exception e) {
      throw new IllegalStateException(
          String.format("Unable to transfer file to S3 bucket bucket=%s fileName=%s reason=%s",
              awsConfig.getBucketName(), key, e.getMessage()));
    }
  }

  private void delete(String key) {
    DeleteObjectRequest request = DeleteObjectRequest.builder()
        .bucket(awsConfig.getBucketName())
        .key(key)
        .build();
    s3Client.deleteObject(request);
  }
}
