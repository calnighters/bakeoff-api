package com.bakeoff.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BakerDto {

  Integer id;
  String name;
  Integer totalTaste;
  Integer totalAppearance;
  List<ParticipantDto> events;
}
