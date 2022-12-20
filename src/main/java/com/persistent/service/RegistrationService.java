package com.persistent.service;

import java.util.List;

import com.persistent.dao.Availability;
import com.persistent.dao.Ticket;
import com.persistent.dao.TrainInfo;
import com.persistent.dto.AvailabilityDto;
import com.persistent.dto.BookTicketDto;
import com.persistent.dto.CancelTicketDto;
import com.persistent.dto.PassengerDto;
import com.persistent.dto.SearchTrainDto;
import com.persistent.dto.StatusDto;

public interface RegistrationService {

	PassengerDto addPassengerDetails(PassengerDto passenger);

	PassengerDto getPassengerDetails(String mobileNumber);

	List<TrainInfo> searchTrain(SearchTrainDto reqDto);

	List<Availability>  ticketAvailability(AvailabilityDto reqDto);

	Ticket bookTicket(BookTicketDto reqDto);

	StatusDto cancelTicket(CancelTicketDto reqDto);

	TrainInfo addTrainDetails(TrainInfo train);

	StatusDto addAvailability(AvailabilityDto reqDto);

}
