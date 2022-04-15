package com.bakeoff.api.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdatePersonDto {

  String oldName;
  String newName;
}
