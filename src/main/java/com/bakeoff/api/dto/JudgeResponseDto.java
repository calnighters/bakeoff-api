package com.bakeoff.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JudgeResponseDto {

  List<PersonDto> judges;
}
