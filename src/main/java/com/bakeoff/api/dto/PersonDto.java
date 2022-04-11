package com.bakeoff.api.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PersonDto {

  Integer id;
  String name;
}
