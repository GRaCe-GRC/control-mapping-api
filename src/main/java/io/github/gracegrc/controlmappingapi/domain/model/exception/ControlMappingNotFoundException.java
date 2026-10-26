package io.github.gracegrc.controlmappingapi.domain.model.exception;

import java.util.UUID;

public class ControlMappingNotFoundException extends RuntimeException {

	public ControlMappingNotFoundException(UUID id) {
		super("Control mapping " + id + " not found");
	}

}
