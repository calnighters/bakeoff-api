package com.bakeoff.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(Include.NON_NULL)
public class ParticipantDto {

  Integer entrantId;
  Integer bakerId;
  String name;
  List<ResultDto> results;
  String description;
  String bakeoffDescription;
  Integer totalTaste;
  Integer totalAppearance;

}
