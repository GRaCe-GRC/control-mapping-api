package io.github.gracegrc.controlmappingapi.domain.service;

import io.github.gracegrc.controlmappingapi.domain.mapper.ControlMappingMapper;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingResponseDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.entity.ControlMappingEntity;
import io.github.gracegrc.controlmappingapi.domain.model.exception.ControlMappingAlreadyExistsException;
import io.github.gracegrc.controlmappingapi.domain.model.exception.ControlMappingNotFoundException;
import io.github.gracegrc.controlmappingapi.domain.repository.ControlMappingRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ControlMappingServiceImpl implements ControlMappingService {

	private final ControlMappingRepository controlMappingRepository;

	public ControlMappingServiceImpl(ControlMappingRepository controlMappingRepository) {
		this.controlMappingRepository = controlMappingRepository;
	}

	@Override
	public UUID create(ControlMappingRequestDto request) {
		ControlMappingEntity mapping = ControlMappingMapper.toEntity(request, Instant.now());
		try {
			return controlMappingRepository.save(mapping).id();
		}
		catch (DuplicateKeyException ex) {
			throw new ControlMappingAlreadyExistsException(mapping.requirementId(), mapping.controlId());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ControlMappingResponseDto> findAll(List<UUID> requirementIds, UUID controlId) {
		boolean byRequirements = requirementIds != null && !requirementIds.isEmpty();
		List<ControlMappingEntity> mappings;
		if (byRequirements && controlId != null) {
			mappings = controlMappingRepository.findByRequirementIdInAndControlId(requirementIds, controlId);
		}
		else if (byRequirements) {
			mappings = controlMappingRepository.findByRequirementIdIn(requirementIds);
		}
		else if (controlId != null) {
			mappings = controlMappingRepository.findByControlId(controlId);
		}
		else {
			mappings = controlMappingRepository.findAll();
		}
		return mappings.stream().map(ControlMappingMapper::toResponseDto).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ControlMappingResponseDto findById(UUID id) {
		return ControlMappingMapper.toResponseDto(findEntity(id));
	}

	@Override
	public ControlMappingResponseDto update(UUID id, ControlMappingUpdateRequestDto request) {
		ControlMappingEntity mapping = ControlMappingMapper.toEntity(findEntity(id), request);
		return ControlMappingMapper.toResponseDto(controlMappingRepository.save(mapping));
	}

	@Override
	public void delete(UUID id) {
		if (!controlMappingRepository.existsById(id)) {
			throw new ControlMappingNotFoundException(id);
		}
		controlMappingRepository.deleteById(id);
	}

	private ControlMappingEntity findEntity(UUID id) {
		return controlMappingRepository.findById(id).orElseThrow(() -> new ControlMappingNotFoundException(id));
	}

}
