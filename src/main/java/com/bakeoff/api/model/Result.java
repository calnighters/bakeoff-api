package com.bakeoff.api.model;

import com.bakeoff.api.model.Result.ResultId;
import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "RESULTS")
@IdClass(ResultId.class)
public class Result {

  @Id
  @Column(name = "FK_BAKER_NAME")
  private String bakerName;

  @Id
  @Column(name = "FK_JUDGE_NAME")
  private String judgeName;

  @Id
  @Column(name = "FK_DATE")
  private LocalDate date;
  
  @Column(name = "APPEARANCE")
  private Integer appearance;
  
  @Column(name = "TASTE")
  private Integer taste;
  
  @Data
  public static class ResultId implements Serializable {
    
    private String bakerName;
    private String judgeName;
    private LocalDate date;
  }
}
