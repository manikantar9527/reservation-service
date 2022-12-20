package com.persistent.service.impl;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
		
		if (reqDto.getUserId() != null) {
			if (passengerRepository.findByUserIdOrContactNumber(reqDto.getUserId(),reqDto.getContactNumber()) == null) {
				throw new ReservationException(AppConstants.INVALID_MOBILENUMBER, HttpStatus.BAD_REQUEST,
						Severity.INFO);
			}
		}
		if(passengerRepository.findByContactNumber(reqDto.getContactNumber()) != null)
			throw new ReservationException(AppConstants.USER_ALREADY_REGISTERED, HttpStatus.BAD_REQUEST,
					Severity.INFO);
		
		passengerRepository.save(modelMapper.map(reqDto, Passenger.class));
		return reqDto;
	}

	@Override
	public PassengerDto getPassengerDetails(String ContactNumber) {
		Passenger passenger = passengerRepository.findByContactNumber(ContactNumber);
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

		List<Availability> availabilities = availabilityRepository.findByTrainTrainIdAndDate(reqDto.getTrainId(),
				reqDto.getDate());
		TrainInfo trainInfo = trainInfoRepository.findByTrainId(reqDto.getTrainId());
		int seatsPerCoach = trainInfo.getTotalSeats() / trainInfo.getNoOfCoaches();
		if (availabilities.isEmpty()) {
			for (int i = 1; i < trainInfo.getNoOfCoaches() + 1; i++) {
				availabilityRepository.save(new Availability(null, reqDto.getDate(), trainInfo, new Date(),
						seatsPerCoach / 2, seatsPerCoach / 2, "c" + i));
			}

			return new StatusDto(0, AppConstants.SEATS_AVAILABLE);
		}

		if (availabilities.stream().collect(Collectors.summingInt(Availability::getNoOfLowerSeatsAvailable))
				+ availabilities.stream().collect(Collectors.summingInt(Availability::getNoOfUpperSeatsAvailable)) != 0)
			return new StatusDto(0, AppConstants.SEATS_AVAILABLE);
		return new StatusDto(1, AppConstants.SEATS_NOT_AVAILABLE);
	}

	@Override
	public Ticket bookTicket(BookTicketDto reqDto) {
		Passenger passenger = passengerRepository.findByUserId(reqDto.getUserId());
		List<Availability> availabilities = availabilityRepository.findByTrainTrainIdAndDate(reqDto.getTrainId(),
				reqDto.getDate());
		String seatNumber = null;
		String coach = null;

		TrainInfo trainInfo = trainInfoRepository.findByTrainId(reqDto.getTrainId());
		Ticket ticket = new Ticket();
		if (!availabilities.isEmpty()) {
			if (availabilities.stream().collect(Collectors.summingInt(Availability::getNoOfLowerSeatsAvailable))
					+ availabilities.stream()
							.collect(Collectors.summingInt(Availability::getNoOfUpperSeatsAvailable)) == 0) {
				throw new ReservationException(AppConstants.SEATS_NOT_AVAILABLE, HttpStatus.ACCEPTED, Severity.INFO);
			} else if (AppConstants.LOWER.equalsIgnoreCase(reqDto.getBerthType())
					|| (passenger.getGender().equalsIgnoreCase(AppConstants.FEMALE) && passenger.getAge() > 40)
					|| passenger.getAge() < 15 || passenger.getAge() > 60) {
				if (availabilities.stream()
						.collect(Collectors.summingInt(Availability::getNoOfLowerSeatsAvailable)) == 0)
					throw new ReservationException(AppConstants.SEATS_NOT_AVAILABLE_IN_LOWER, HttpStatus.ACCEPTED,
							Severity.INFO);
				else {
					Availability availability = availabilities.stream()
							.collect(Collectors.maxBy(Comparator.comparing(Availability::getNoOfLowerSeatsAvailable)))
							.get();

					availability.setNoOfLowerSeatsAvailable(availability.getNoOfLowerSeatsAvailable() - 1);
					availabilityRepository.save(availability);
					seatNumber = availability.getCoach() + "-" + (availability.getNoOfLowerSeatsAvailable()+1);
					coach = availability.getCoach();
					ticket.setBerthType(AppConstants.LOWER);
				}
			} else {
				if (availabilities.stream()
						.collect(Collectors.summingInt(Availability::getNoOfUpperSeatsAvailable)) != 0) {
					Availability availability = availabilities.stream()
							.collect(Collectors.maxBy(Comparator.comparing(Availability::getNoOfUpperSeatsAvailable)))
							.get();
					availability.setNoOfUpperSeatsAvailable(availability.getNoOfUpperSeatsAvailable() - 1);
					availabilityRepository.save(availability);
					seatNumber = availability.getCoach() + "-" + (availability.getNoOfUpperSeatsAvailable()+1);
					coach = availability.getCoach();
					ticket.setBerthType(AppConstants.UPPER);
				} else {
					Availability availability = availabilities.stream()
							.collect(Collectors.maxBy(Comparator.comparing(Availability::getNoOfLowerSeatsAvailable)))
							.get();
					availability.setNoOfLowerSeatsAvailable(availability.getNoOfLowerSeatsAvailable() - 1);
					ticket.setBerthType(AppConstants.LOWER);
					seatNumber = availability.getCoach() + "-" + (availability.getNoOfLowerSeatsAvailable()+1);
					coach = availability.getCoach();
					availabilityRepository.save(availability);
				}

			}
		} else {

			int seatsPerCoach = trainInfo.getTotalSeats() / trainInfo.getNoOfCoaches();
			if (AppConstants.LOWER.equalsIgnoreCase(reqDto.getBerthType())
					|| (passenger.getGender().equalsIgnoreCase(AppConstants.FEMALE) && passenger.getAge() > 40)
					|| passenger.getAge() < 15 || passenger.getAge() > 60) {
				for (int i = 1; i < trainInfo.getNoOfCoaches() + 1; i++) {
					if (i == 1) {
						availabilityRepository.save(new Availability(null, reqDto.getDate(), trainInfo, new Date(),
								seatsPerCoach / 2, (seatsPerCoach / 2) - 1, "c" + i));
						seatNumber = "c" + i + "-" + (seatsPerCoach / 2);
						coach = "c" + i;
					} else {
						availabilityRepository.save(new Availability(null, reqDto.getDate(), trainInfo, new Date(),
								seatsPerCoach / 2, (seatsPerCoach / 2), "c" + i));

					}
				}
				ticket.setBerthType(AppConstants.LOWER);
			} else {
				for (int i = 1; i < trainInfo.getNoOfCoaches() + 1; i++) {
					if (i == 1) {
						availabilityRepository.save(new Availability(null, reqDto.getDate(), trainInfo, new Date(),
								(seatsPerCoach / 2) - 1, seatsPerCoach / 2, "c" + i));
						seatNumber = "c" + i + "-" + (seatsPerCoach / 2);
						coach = "c" + i;
					} else {
						availabilityRepository.save(new Availability(null, reqDto.getDate(), trainInfo, new Date(),
								seatsPerCoach / 2, (seatsPerCoach / 2), "c" + i));

					}
				}
				ticket.setBerthType(AppConstants.UPPER);
			}
		}
		ticket.setTicketCost(reqDto.getTicketCost());
		ticket.setStartingLocation(reqDto.getStartingLocation());
		ticket.setDestination(reqDto.getDestination());
		ticket.setDate(reqDto.getDate());
		ticket.setStatus(0);
		ticket.setCoach(coach);
		ticket.setSeatNumber(seatNumber);
		ticket.setTrain(trainInfo);
		ticket.setPassenger(passenger);
		ticketRepository.save(ticket);
		paymentRepository.save(new Payment(null, passenger, reqDto.getCardType(), reqDto.getCardNumber(), new Date()));
		return ticket;
	}

	@Override
	public StatusDto cancelTicket(CancelTicketDto reqDto) {
		Passenger passenger = passengerRepository.findByContactNumber(reqDto.getContactNumber());
		Ticket ticket = ticketRepository.findByPassengerUserIdAndTicketIdAndStatus(passenger.getUserId(),
				reqDto.getTicketId(), 0);
		if (ticket == null) {
			return new StatusDto(1, AppConstants.INVALID_TICKET_DETAILS);
		}
		ticket.setStatus(1);
		ticketRepository.save(ticket);
		Availability availability = availabilityRepository
				.findByTrainTrainIdAndDateAndCoach(ticket.getTrain().getTrainId(), ticket.getDate(), ticket.getCoach());
		if (AppConstants.LOWER.equalsIgnoreCase(ticket.getBerthType()))
			availability.setNoOfLowerSeatsAvailable(availability.getNoOfLowerSeatsAvailable() + 1);
		else
			availability.setNoOfUpperSeatsAvailable(availability.getNoOfUpperSeatsAvailable() + 1);

		availabilityRepository.save(availability);
		return new StatusDto(1, AppConstants.TICKET_CANCELLED_SUCCESSFULLY);
	}

	@Override
	public TrainInfo addTrainDetails(TrainInfo trainInfo) {
		return trainInfoRepository.save(trainInfo);
	}

}
