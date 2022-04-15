package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Judge;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface JudgeRepository extends CrudRepository<Judge, Integer> {

  Optional<Judge> findByJudgeName(String judgeName);
}
