package com.bakeoff.api.model;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "PARTICIPANT")
public class Participant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "FK_BAKER_ID")
  private Baker fkBaker;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "FK_BAKEOFF_ID")
  private Bakeoff fkBakeoff;

  @Column(name = "ENTRANT_ID")
  private Integer entrantId;

  @Column(name = "DESCRIPTION", length = 256)
  private String description;

  @OneToMany(mappedBy = "fkParticipant")
  private List<Result> results;

}