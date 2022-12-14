package com.persistent.dao;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
//@JsonIgnoreProperties({ "hibernateLazyInitializer" })
public class Passenger {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "passenger_seq")
	@SequenceGenerator(name = "passenger_seq", allocationSize = 5)
	@Column(name = "passenger_id", unique = true, nullable = false)
	private Long passengerId;
	private String name;
	private String mobileNumber;
	private String email;
	private String gender;
	private Integer age;
	private String address;
	private String password;
	@CreatedDate
	private Date createdOn;
	public Passenger(Long passengerId) {
		super();
		this.passengerId = passengerId;
	}
	
}
