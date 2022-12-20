package com.persistent.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@JsonIgnoreProperties({ "hibernateLazyInitializer" })
public class PassengerDto {
	private Long userId;
	private String name;
	private String contactNumber;
	private String email;
	private String gender;
	private Integer age;
	private String address;
	@JsonIgnore
	private String password;
	private Date createdOn;
}
