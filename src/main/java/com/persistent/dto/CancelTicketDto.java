package com.persistent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelTicketDto {
	private String mobileNumber;
	private Long ticketId;
}
