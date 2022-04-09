package com.bakeoff.api.repositories;

import com.bakeoff.api.model.Participant;
import org.springframework.data.repository.CrudRepository;

public interface ParticipantRepository extends CrudRepository<Participant, Integer> {

}
