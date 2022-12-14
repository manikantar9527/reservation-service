package com.persistent.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDto {
	private Date Date;
	private Long trainId;
	private String mobileNumber;
}
