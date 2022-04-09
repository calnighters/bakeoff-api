package com.bakeoff.api.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BakeoffDto {

  LocalDate date;
  String title;
  List<ParticipantDto> participants;
}
