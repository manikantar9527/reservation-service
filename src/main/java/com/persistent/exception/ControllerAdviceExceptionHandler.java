package com.persistent.exception;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import lombok.Builder;
import lombok.Data;

@ControllerAdvice
public class ControllerAdviceExceptionHandler {

	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorMessage> handleForbiddenAccessException(Exception exception, HttpStatus status) {
		List<String> details = new ArrayList<>();
		details.add(exception.getLocalizedMessage());

		ErrorMessage errorMessage = ErrorMessage.builder().httpStatus(status).errorDetails(details).build();

		return ResponseEntity.status(status).body(errorMessage);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorMessage> handleUserBadRequestException(HttpStatus status, WebRequest request) {
		List<String> details = new ArrayList<>();
		details.add(request.getContextPath());

		ErrorMessage errorMessage = ErrorMessage.builder().httpStatus(status).errorDetails(details).build();

		return ResponseEntity.status(status).body(errorMessage);
	}

	@ExceptionHandler(ReservationException.class)
	public ResponseEntity<ErrorDto> handleReservationException(ReservationException e, WebRequest request) {
		ErrorDto errorMessage = ErrorDto.builder().id(UUID.randomUUID()).severity(e.getSeverity())
				.message(e.getMessage()).currentTime(OffsetDateTime.now()).build();
		return ResponseEntity.status(e.getStatus()).body(errorMessage);
	}

	@Data
	@Builder
	private static class ErrorMessage {
		private final String message;
		private final HttpStatus httpStatus;
		private final List<String> errorDetails;
	}
}
