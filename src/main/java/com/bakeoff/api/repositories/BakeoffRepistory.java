package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Bakeoff;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface BakeoffRepistory extends CrudRepository<Bakeoff, Integer> {

  Optional<Bakeoff> findByBoDate(LocalDate boDate);
  
}
