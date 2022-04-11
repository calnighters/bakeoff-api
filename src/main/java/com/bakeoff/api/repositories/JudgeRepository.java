package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Bakeoff;
import com.bakeoff.api.model.Judge;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface JudgeRepository extends CrudRepository<Judge, Integer> {

  Optional<Judge> findByJudgeNameAndFkBakeoff(String judgeName, Bakeoff bakeoff);
}
