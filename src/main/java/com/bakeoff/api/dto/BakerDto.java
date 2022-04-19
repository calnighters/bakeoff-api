package com.bakeoff.api.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BakerDto {

  Integer id;
  String name;
  BigDecimal totalTaste;
  BigDecimal totalAppearance;
  List<ParticipantDto> events;
}
