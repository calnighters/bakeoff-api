package com.bakeoff.api.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "BAKEOFF")
public class Bakeoff {

  @Id
  @Column(name = "BO_DATE")
  private LocalDate date;
  
  @Column(name = "FOOD")
  private String food;
}
