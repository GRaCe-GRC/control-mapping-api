package io.github.gracegrc.controlmappingapi.domain.model.exception;

import java.util.UUID;

public class ControlMappingAlreadyExistsException extends RuntimeException {

	public ControlMappingAlreadyExistsException(UUID requirementId, UUID controlId) {
		super("Requirement " + requirementId + " is already mapped to control " + controlId);
	}

}
