package com.persistent.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainInfoDto {
	private Long trainId;
	private String trainNumber;
	private String trainName;
	private String source;
	private String destination;

	private String departureLocation;
	private String availableDays;
	private String departuretime;
	private Integer totalSeats;
	private Integer noOfCoaches;

	private String arrivalTime;

	private Date createdOn;

}