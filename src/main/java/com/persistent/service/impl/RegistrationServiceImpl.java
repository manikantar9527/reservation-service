package com.persistent.service.impl;

import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.persistent.config.AppConstants;
import com.persistent.dao.Availability;
import com.persistent.dao.Passenger;
import com.persistent.dao.Payment;
import com.persistent.dao.Ticket;
import com.persistent.dao.TrainInfo;
import com.persistent.dto.AvailabilityDto;
import com.persistent.dto.BookTicketDto;
import com.persistent.dto.CancelTicketDto;
import com.persistent.dto.PassengerDto;
import com.persistent.dto.SearchTrainDto;
import com.persistent.dto.StatusDto;
import com.persistent.exception.ReservationException;
import com.persistent.exception.Severity;
import com.persistent.repository.AvailabilityRepository;
import com.persistent.repository.PassengerRepository;
import com.persistent.repository.PaymentRepository;
import com.persistent.repository.TicketRepository;
import com.persistent.repository.TrainInfoRepository;
import com.persistent.service.RegistrationService;

@Service
public class RegistrationServiceImpl implements RegistrationService {

	@Autowired
	private PassengerRepository passengerRepository;

	@Autowired
	private AvailabilityRepository availabilityRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private TrainInfoRepository trainInfoRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Override
	public PassengerDto addPassengerDetails(PassengerDto reqDto) {
		if (reqDto.getPassengerId() == null) {
			if (passengerRepository.findByMobileNumber(reqDto.getMobileNumber()) != null) {
				throw new ReservationException(AppConstants.USER_ALREADY_REGISTERED, HttpStatus.BAD_REQUEST,
						Severity.INFO);
			}
		}
		passengerRepository.save(modelMapper.map(reqDto, Passenger.class));
		return reqDto;
	}

	@Override
	public PassengerDto getPassengerDetails(String mobileNumber) {
		Passenger passenger = passengerRepository.findByMobileNumber(mobileNumber);
		if (passenger == null) {
			throw new ReservationException(AppConstants.INVALID_MOBILENUMBER, HttpStatus.BAD_REQUEST, Severity.INFO);
		}

		return modelMapper.map(passenger, PassengerDto.class);
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<TrainInfo> searchTrain(SearchTrainDto reqDto) {
		return trainInfoRepository.findBySourceAndDestinationAndAvailableDaysContaining(reqDto.getSource(),
				reqDto.getDestination(), String.valueOf(reqDto.getDate().getDay()));
	}

	@Override
	public StatusDto ticketAvailability(AvailabilityDto reqDto) {

		Availability availability = availabilityRepository.findByTrainTrainIdAndDate(reqDto.getTrainId(),
				reqDto.getDate());
		TrainInfo trainInfo = trainInfoRepository.findByTrainId(reqDto.getTrainId());
		if (availability == null) {
			availabilityRepository.save(new Availability(null, reqDto.getDate(), trainInfo, trainInfo.getTotalSeats(),
					new Date(), trainInfo.getTotalSeats() / 2, trainInfo.getTotalSeats() / 2));
			return new StatusDto(0, AppConstants.SEATS_AVAILABLE);
		}

		if (availability.getNoOfSeatsAvailable() != 0)
			return new StatusDto(0, AppConstants.SEATS_AVAILABLE);
		return new StatusDto(1, AppConstants.SEATS_NOT_AVAILABLE);
	}

	@Override
	public Ticket bookTicket(BookTicketDto reqDto) {
		Passenger passenger = passengerRepository.findByPassengerId(reqDto.getPassengerId());
		Availability availability = availabilityRepository.findByTrainTrainIdAndDate(reqDto.getTrainId(),
				reqDto.getDate());

		TrainInfo trainInfo = trainInfoRepository.findByTrainId(reqDto.getTrainId());
		Ticket ticket = new Ticket();
		if (availability != null) {
			if (availability.getNoOfSeatsAvailable() == 0) {
				throw new ReservationException(AppConstants.SEATS_NOT_AVAILABLE, HttpStatus.ACCEPTED, Severity.INFO);
			} else if (AppConstants.LOWER.equalsIgnoreCase(reqDto.getBerthType())
					|| passenger.getGender().equalsIgnoreCase(AppConstants.FEMALE) || passenger.getAge() < 15
					|| passenger.getAge() > 40) {
				if (availability.getNoOfLowerSeatsAvailable() == 0)
					throw new ReservationException(AppConstants.SEATS_NOT_AVAILABLE_IN_LOWER, HttpStatus.ACCEPTED,
							Severity.INFO);
				else {
					availability.setNoOfLowerSeatsAvailable(availability.getNoOfLowerSeatsAvailable() - 1);
					availability.setNoOfSeatsAvailable(availability.getNoOfSeatsAvailable() - 1);
					ticket.setBerthType(AppConstants.LOWER);
				}
			} else {
				availability.setNoOfUpperSeatsAvailable(availability.getNoOfUpperSeatsAvailable() - 1);
				availability.setNoOfSeatsAvailable(availability.getNoOfSeatsAvailable() - 1);
				ticket.setBerthType(AppConstants.UPPER);
			}
		} else {
			availability = new Availability();
			availability.setNoOfSeatsAvailable(trainInfo.getTotalSeats() - 1);
			if (AppConstants.LOWER.equalsIgnoreCase(reqDto.getBerthType())
					|| passenger.getGender().equalsIgnoreCase(AppConstants.FEMALE) || passenger.getAge() < 15
					|| passenger.getAge() > 40) {
				availability.setNoOfLowerSeatsAvailable(trainInfo.getTotalSeats() / 2 - 1);
				availability.setNoOfUpperSeatsAvailable(trainInfo.getTotalSeats() / 2);
				ticket.setBerthType(AppConstants.LOWER);
			} else {
				availability.setNoOfUpperSeatsAvailable(trainInfo.getTotalSeats() / 2 - 1);
				availability.setNoOfLowerSeatsAvailable(trainInfo.getTotalSeats() / 2);
				ticket.setBerthType(AppConstants.UPPER);
			}

			availability.setDate(reqDto.getDate());
			availability.setTrain(trainInfo);
		}

		/* modelMapper.map(reqDto, Ticket.class); */
		ticket.setTicketCost(reqDto.getTicketCost());

		ticket.setStartingLocation(reqDto.getStartingLocation());
		ticket.setDestination(reqDto.getDestination());
		ticket.setDate(reqDto.getDate());
		ticket.setStatus(0);
		ticket.setSeatNumber(availability.getNoOfSeatsAvailable());
		ticket.setTrain(trainInfo);
		ticket.setPassenger(passenger);
		ticketRepository.save(ticket);
		paymentRepository.save(new Payment(null, passenger, reqDto.getCardType(), reqDto.getCardNumber(), new Date()));
		availabilityRepository.save(availability);
		return ticket;
	}

	@Override
	public StatusDto cancelTicket(CancelTicketDto reqDto) {
		Passenger passenger = passengerRepository.findByMobileNumber(reqDto.getMobileNumber());
		Ticket ticket = ticketRepository.findByPassengerPassengerIdAndTicketIdAndStatus(passenger.getPassengerId(),
				reqDto.getTicketId(), 0);
		if (ticket == null) {
			return new StatusDto(1, AppConstants.INVALID_TICKET_DETAILS);
		}
		ticket.setStatus(1);
		ticketRepository.save(ticket);
		Availability availability = availabilityRepository.findByTrainTrainIdAndDate(ticket.getTrain().getTrainId(),
				ticket.getDate());
		if (AppConstants.LOWER.equalsIgnoreCase(ticket.getBerthType()))
			availability.setNoOfLowerSeatsAvailable(availability.getNoOfLowerSeatsAvailable() + 1);
		else
			availability.setNoOfUpperSeatsAvailable(availability.getNoOfUpperSeatsAvailable() + 1);

		availability.setNoOfSeatsAvailable(availability.getNoOfSeatsAvailable() + 1);
		availabilityRepository.save(availability);
		return new StatusDto(1, AppConstants.TICKET_CANCELLED_SUCCESSFULLY);
	}

	@Override
	public TrainInfo addTrainDetails(TrainInfo trainInfo) {
		return trainInfoRepository.save(trainInfo);
	}

}
