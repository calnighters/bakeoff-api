package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Bakeoff;
import com.bakeoff.api.model.Participant;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ParticipantRepository extends CrudRepository<Participant, Integer> {

  Optional<Participant> findByEntrantIdAndFkBakeoff(Integer entrantId, Bakeoff bakeoff);
}
