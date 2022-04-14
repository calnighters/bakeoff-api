package com.bakeoff.api.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
@Table(name = "JUDGE")
public class Judge {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Integer id;

  @Column(name = "JUDGE_NAME", length = 28)
  private String judgeName;

  @OneToMany(mappedBy = "fkJudge")
  private List<JudgeHistory> judgeHistories;

  @OneToMany(mappedBy = "fkJudge")
  private List<Result> results;

}