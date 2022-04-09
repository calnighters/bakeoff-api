package com.bakeoff.api.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResultDto {
  
  Integer entrantId;
  String judgeName;
  Integer appearance;
  Integer taste;
}
