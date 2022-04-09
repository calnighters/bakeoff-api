package com.bakeoff.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteEntrantDto {

  Integer entrantId;
  
}
