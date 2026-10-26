package io.github.gracegrc.controlmappingapi.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ControlMappingRequestDto(
		@NotNull UUID requirementId,
		@NotNull UUID controlId,
		String rationale,
		@NotBlank @Size(max = 255) String createdBy) {
}
