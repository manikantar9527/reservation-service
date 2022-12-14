package com.persistent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.persistent.dao.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

	Ticket findByPassengerPassengerIdAndTicketIdAndStatus(Long passengerId, Long ticketId, int i);

}
