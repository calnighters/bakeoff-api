package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Baker;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface BakerRepository extends CrudRepository<Baker, Integer> {

  Optional<Baker> findByBakerName(String bakerName);
}
