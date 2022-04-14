package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Bakeoff;
import com.bakeoff.api.model.Judge;
import com.bakeoff.api.model.JudgeHistory;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface JudgeHistoryRepository extends CrudRepository<JudgeHistory, Integer> {

  Optional<JudgeHistory> findByFkBakeoffAndFkJudge(Bakeoff bakeoff, Judge judge);
}
