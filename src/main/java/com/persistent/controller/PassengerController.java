package com.persistent.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.persistent.dao.Availability;
import com.persistent.dao.Ticket;
import com.persistent.dao.TrainInfo;
import com.persistent.dto.AvailabilityDto;
import com.persistent.dto.BookTicketDto;
import com.persistent.dto.CancelTicketDto;
import com.persistent.dto.PassengerDto;
import com.persistent.dto.SearchTrainDto;
import com.persistent.dto.StatusDto;
import com.persistent.service.RegistrationService;

@RestController
@RequestMapping("/reservation")
public class PassengerController {

	@Autowired
	private RegistrationService service;

	@PostMapping("passeger/registration")
	public ResponseEntity<PassengerDto> addPassengerDetails(@RequestBody PassengerDto passenger) {
		return ResponseEntity.ok(service.addPassengerDetails(passenger));
	}

	@PostMapping("update/profile")
	public ResponseEntity<PassengerDto> updatePassengerDetails(@RequestBody PassengerDto passenger) {
		return ResponseEntity.ok(service.addPassengerDetails(passenger));
	}

	@PostMapping("add/availability")
	public ResponseEntity<StatusDto> addAvailability(@RequestBody AvailabilityDto reqDto) {
		return ResponseEntity.ok(service.addAvailability(reqDto));
	}

	@GetMapping("passeger/{mobileNumber}")
	public ResponseEntity<PassengerDto> getPassengerDetails(@PathVariable String mobileNumber) {
		return ResponseEntity.ok(service.getPassengerDetails(mobileNumber));
	}

	@PostMapping("searchTrain")
	public ResponseEntity<List<TrainInfo>> searchTrain(@RequestBody SearchTrainDto reqDto) {
		return ResponseEntity.ok(service.searchTrain(reqDto));
	}

	@PostMapping("ticket/availability")
	public ResponseEntity<List<Availability>> ticketAvailability(@RequestBody AvailabilityDto reqDto) {
		return ResponseEntity.ok(service.ticketAvailability(reqDto));
	}

	@PostMapping("book/ticket")
	public ResponseEntity<Ticket> bookTicket(@RequestBody BookTicketDto reqDto) {
		return ResponseEntity.ok(service.bookTicket(reqDto));
	}

	@PostMapping("cancel/ticket")
	public ResponseEntity<StatusDto> cancelTicket(@RequestBody CancelTicketDto reqDto) {
		return ResponseEntity.ok(service.cancelTicket(reqDto));
	}

	@PostMapping("add/train")
	public ResponseEntity<TrainInfo> addTrainDetails(@RequestBody TrainInfo train) {
		return ResponseEntity.ok(service.addTrainDetails(train));
	}

}
// bookTicket makePayment cancelTicket

//updateSchedule modifyTicketCount