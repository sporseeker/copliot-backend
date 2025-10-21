package com.spotseeker.copliot.repository;

import com.spotseeker.copliot.model.Event;
import com.spotseeker.copliot.model.Withdrawal;
import com.spotseeker.copliot.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

    List<Withdrawal> findByEvent(Event event);

    List<Withdrawal> findByPartner(Partner partner);
}

