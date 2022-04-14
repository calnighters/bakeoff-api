package com.bakeoff.api.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "JUDGE_HISTORY")
public class JudgeHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "FK_JUDGE_ID")
  private Judge fkJudge;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "FK_BAKEOFF_ID")
  private Bakeoff fkBakeoff;

}