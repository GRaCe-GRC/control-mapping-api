package io.github.gracegrc.controlmappingapi.domain.mapper;

import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingResponseDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.entity.ControlMappingEntity;
import java.time.Instant;

public final class ControlMappingMapper {

	private ControlMappingMapper() {
	}

	public static ControlMappingEntity toEntity(ControlMappingRequestDto request, Instant createdAt) {
		return new ControlMappingEntity(null, request.requirementId(), request.controlId(), request.rationale(),
				createdAt, request.createdBy());
	}

	public static ControlMappingEntity toEntity(ControlMappingEntity existing, ControlMappingUpdateRequestDto request) {
		return new ControlMappingEntity(existing.id(), existing.requirementId(), existing.controlId(),
				request.rationale(), existing.createdAt(), existing.createdBy());
	}

	public static ControlMappingResponseDto toResponseDto(ControlMappingEntity entity) {
		return new ControlMappingResponseDto(entity.id(), entity.requirementId(), entity.controlId(),
				entity.rationale(), entity.createdAt(), entity.createdBy());
	}

}
