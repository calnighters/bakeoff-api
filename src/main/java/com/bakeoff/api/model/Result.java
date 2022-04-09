package com.bakeoff.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "RESULT")
public class Result {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Integer id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "FK_JUDGE_ID")
  private Judge fkJudge;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "FK_PARTICIPANT_ID")
  private Participant fkParticipant;

  @Column(name = "APPEARANCE")
  private Integer appearance;

  @Column(name = "TASTE")
  private Integer taste;

}