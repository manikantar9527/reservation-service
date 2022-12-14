package com.persistent.dto;

import java.util.Date;

import com.persistent.dao.Passenger;
import com.persistent.dao.TrainInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookTicketDto {
	private Long passengerId;
	private TrainInfo train;
	private Passenger passenger;
	private Long trainId;
	private Long cardNumber;
	private String cardType;
	
	private Double ticketCost;
	private String startingLocation;
	private String destination;
	private Date date;
	private String berthType;
}
