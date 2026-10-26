package io.github.gracegrc.controlmappingapi.domain.service;

import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingResponseDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingUpdateRequestDto;
import java.util.List;
import java.util.UUID;

public interface ControlMappingService {

	UUID create(ControlMappingRequestDto request);

	List<ControlMappingResponseDto> findAll(List<UUID> requirementIds, UUID controlId);

	ControlMappingResponseDto findById(UUID id);

	ControlMappingResponseDto update(UUID id, ControlMappingUpdateRequestDto request);

	void delete(UUID id);

}
