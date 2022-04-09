package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Judge;
import org.springframework.data.repository.CrudRepository;

public interface JudgeRepository extends CrudRepository<Judge, Integer> {

}
