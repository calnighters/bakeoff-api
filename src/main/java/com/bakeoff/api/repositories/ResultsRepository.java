package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Result;
import com.bakeoff.api.model.Result.ResultId;
import org.springframework.data.repository.CrudRepository;

public interface ResultsRepository extends CrudRepository<Result, ResultId> {

}
