package io.github.gracegrc.controlmappingapi.domain.model.dto;

import java.time.Instant;
import java.util.UUID;

public record ControlMappingResponseDto(
		UUID id,
		UUID requirementId,
		UUID controlId,
		String rationale,
		Instant createdAt,
		String createdBy) {
}
