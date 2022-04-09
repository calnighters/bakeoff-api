package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Baker;
import org.springframework.data.repository.CrudRepository;

public interface BakerRepository extends CrudRepository<Baker, Integer> {

}
