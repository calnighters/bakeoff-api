package com.bakeoff.api.model;

import java.time.LocalDate;
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
@Table(name = "BAKEOFF")
public class Bakeoff {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Integer id;

  @Column(name = "BO_DATE")
  private LocalDate boDate;

  @Column(name = "FOOD", length = 28)
  private String food;

  @OneToMany(mappedBy = "fkBakeoff")
  private List<Participant> participants;

  @OneToMany(mappedBy = "fkBakeoff")
  private List<JudgeHistory> judgeHistories;

}