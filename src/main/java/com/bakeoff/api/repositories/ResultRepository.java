package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Judge;
import com.bakeoff.api.model.Participant;
import com.bakeoff.api.model.Result;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ResultRepository extends CrudRepository<Result, Integer> {

  Optional<Result> findByFkJudgeAndFkParticipant(Judge judge, Participant participant);
}
