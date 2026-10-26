package io.github.gracegrc.controlmappingapi.domain.controller;

import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingResponseDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.controlmappingapi.domain.service.ControlMappingService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/control-mappings")
public class ControlMappingApiController {

	private final ControlMappingService controlMappingService;

	public ControlMappingApiController(ControlMappingService controlMappingService) {
		this.controlMappingService = controlMappingService;
	}

	@PostMapping
	public ResponseEntity<Void> create(@Valid @RequestBody ControlMappingRequestDto request) {
		UUID id = controlMappingService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@GetMapping
	public List<ControlMappingResponseDto> findAll(@RequestParam(required = false) List<UUID> requirementId,
			@RequestParam(required = false) UUID controlId) {
		return controlMappingService.findAll(requirementId, controlId);
	}

	@GetMapping("/{id}")
	public ControlMappingResponseDto findById(@PathVariable UUID id) {
		return controlMappingService.findById(id);
	}

	@PutMapping("/{id}")
	public ControlMappingResponseDto update(@PathVariable UUID id,
			@Valid @RequestBody ControlMappingUpdateRequestDto request) {
		return controlMappingService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		controlMappingService.delete(id);
	}

}
