package com.spotseeker.copliot.repository;

import com.spotseeker.copliot.model.FraudAlert;
import com.spotseeker.copliot.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {

    List<FraudAlert> findByEvent(Event event);

    List<FraudAlert> findByEventAndResolvedFalse(Event event);
}

