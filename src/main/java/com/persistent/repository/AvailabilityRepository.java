package com.persistent.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.persistent.dao.Availability;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long>{

	Availability findByTrainTrainIdAndDate(Long trainId, Date date);

	Availability findByTrainTrainId(Long trainId);

}
