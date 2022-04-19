package com.bakeoff.api.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResultDto {
  
  Integer entrantId;
  String judgeName;
  BigDecimal appearance;
  BigDecimal taste;
}
