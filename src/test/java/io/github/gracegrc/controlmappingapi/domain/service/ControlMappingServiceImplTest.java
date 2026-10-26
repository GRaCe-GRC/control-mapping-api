package io.github.gracegrc.controlmappingapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingResponseDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.entity.ControlMappingEntity;
import io.github.gracegrc.controlmappingapi.domain.model.exception.ControlMappingAlreadyExistsException;
import io.github.gracegrc.controlmappingapi.domain.model.exception.ControlMappingNotFoundException;
import io.github.gracegrc.controlmappingapi.domain.repository.ControlMappingRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class ControlMappingServiceImplTest {

	private static final UUID ID = UUID.fromString("4e5f6a7b-8c9d-4e0f-a1b2-c3d4e5f6a7b8");

	private static final UUID REQUIREMENT_ID = UUID.fromString("1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d");

	private static final UUID OTHER_REQUIREMENT_ID = UUID.fromString("6f5e4d3c-2b1a-4f9e-8d7c-6b5a4f3e2d1c");

	private static final UUID CONTROL_ID = UUID.fromString("7c3d2e1f-0a9b-4c8d-8e7f-6a5b4c3d2e1f");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final ControlMappingEntity EXISTING = new ControlMappingEntity(ID, REQUIREMENT_ID, CONTROL_ID,
			"Covers it.", CREATED_AT, "admin-console");

	private static final ControlMappingEntity OTHER = new ControlMappingEntity(UUID.randomUUID(), OTHER_REQUIREMENT_ID,
			CONTROL_ID, null, CREATED_AT, "admin-console");

	@Mock
	private ControlMappingRepository controlMappingRepository;

	@InjectMocks
	private ControlMappingServiceImpl controlMappingService;

	@Captor
	private ArgumentCaptor<ControlMappingEntity> saved;

	@Test
	void createSavesTheMappingWithTheCurrentTime() {
		given(controlMappingRepository.save(any())).willAnswer(invocation -> {
			ControlMappingEntity entity = invocation.getArgument(0);
			return new ControlMappingEntity(ID, entity.requirementId(), entity.controlId(), entity.rationale(),
					entity.createdAt(), entity.createdBy());
		});
		Instant before = Instant.now();

		UUID id = controlMappingService
			.create(new ControlMappingRequestDto(REQUIREMENT_ID, CONTROL_ID, "Covers it.", "admin-console"));

		then(controlMappingRepository).should().save(saved.capture());
		assertThat(saved.getValue().id()).isNull();
		assertThat(saved.getValue().requirementId()).isEqualTo(REQUIREMENT_ID);
		assertThat(saved.getValue().controlId()).isEqualTo(CONTROL_ID);
		assertThat(saved.getValue().createdAt()).isBetween(before, Instant.now());
		assertThat(saved.getValue().createdBy()).isEqualTo("admin-console");
		assertThat(id).isEqualTo(ID);
	}

	@Test
	void createWithADuplicatePairThrowsAlreadyExists() {
		given(controlMappingRepository.save(any()))
			.willThrow(new DuplicateKeyException("control_mappings_requirement_id_control_id_key"));

		assertThatThrownBy(() -> controlMappingService
			.create(new ControlMappingRequestDto(REQUIREMENT_ID, CONTROL_ID, null, "admin-console")))
			.isInstanceOf(ControlMappingAlreadyExistsException.class)
			.hasMessage("Requirement " + REQUIREMENT_ID + " is already mapped to control " + CONTROL_ID);
	}

	@Test
	void findAllWithoutFiltersReturnsEveryMapping() {
		given(controlMappingRepository.findAll()).willReturn(List.of(EXISTING, OTHER));

		assertThat(controlMappingService.findAll(null, null)).extracting(ControlMappingResponseDto::requirementId)
			.containsExactly(REQUIREMENT_ID, OTHER_REQUIREMENT_ID);
	}

	@Test
	void findAllWithAnEmptyRequirementListReturnsEveryMapping() {
		given(controlMappingRepository.findAll()).willReturn(List.of(EXISTING));

		assertThat(controlMappingService.findAll(List.of(), null)).hasSize(1);
		then(controlMappingRepository).should(never()).findByRequirementIdIn(any());
	}

	@Test
	void findAllWithRequirementIdsReturnsTheirMappings() {
		List<UUID> requirementIds = List.of(REQUIREMENT_ID, OTHER_REQUIREMENT_ID);
		given(controlMappingRepository.findByRequirementIdIn(requirementIds)).willReturn(List.of(EXISTING, OTHER));

		assertThat(controlMappingService.findAll(requirementIds, null)).hasSize(2);
		then(controlMappingRepository).should(never()).findAll();
	}

	@Test
	void findAllWithControlIdReturnsItsMappings() {
		given(controlMappingRepository.findByControlId(CONTROL_ID)).willReturn(List.of(EXISTING, OTHER));

		assertThat(controlMappingService.findAll(null, CONTROL_ID)).hasSize(2);
		then(controlMappingRepository).should(never()).findAll();
	}

	@Test
	void findAllWithRequirementIdsAndControlIdUsesBothFilters() {
		given(controlMappingRepository.findByRequirementIdInAndControlId(List.of(REQUIREMENT_ID), CONTROL_ID))
			.willReturn(List.of(EXISTING));

		assertThat(controlMappingService.findAll(List.of(REQUIREMENT_ID), CONTROL_ID))
			.extracting(ControlMappingResponseDto::id)
			.containsExactly(ID);
		then(controlMappingRepository).should(never()).findByRequirementIdIn(any());
		then(controlMappingRepository).should(never()).findByControlId(any());
	}

	@Test
	void findByIdReturnsTheMapping() {
		given(controlMappingRepository.findById(ID)).willReturn(Optional.of(EXISTING));

		assertThat(controlMappingService.findById(ID)).isEqualTo(new ControlMappingResponseDto(ID, REQUIREMENT_ID,
				CONTROL_ID, "Covers it.", CREATED_AT, "admin-console"));
	}

	@Test
	void findByIdWithAnUnknownIdThrowsNotFound() {
		given(controlMappingRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> controlMappingService.findById(ID))
			.isInstanceOf(ControlMappingNotFoundException.class)
			.hasMessage("Control mapping " + ID + " not found");
	}

	@Test
	void updateChangesTheRationaleOnly() {
		given(controlMappingRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(controlMappingRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		ControlMappingResponseDto response = controlMappingService.update(ID,
				new ControlMappingUpdateRequestDto("Covers quarterly reviews."));

		assertThat(response).isEqualTo(new ControlMappingResponseDto(ID, REQUIREMENT_ID, CONTROL_ID,
				"Covers quarterly reviews.", CREATED_AT, "admin-console"));
	}

	@Test
	void updateWithAnUnknownIdThrowsNotFoundAndSavesNothing() {
		given(controlMappingRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> controlMappingService.update(ID, new ControlMappingUpdateRequestDto("x")))
			.isInstanceOf(ControlMappingNotFoundException.class);
		then(controlMappingRepository).should(never()).save(any());
	}

	@Test
	void deleteRemovesAnExistingMapping() {
		given(controlMappingRepository.existsById(ID)).willReturn(true);

		controlMappingService.delete(ID);

		then(controlMappingRepository).should().deleteById(ID);
	}

	@Test
	void deleteWithAnUnknownIdThrowsNotFoundAndDeletesNothing() {
		given(controlMappingRepository.existsById(ID)).willReturn(false);

		assertThatThrownBy(() -> controlMappingService.delete(ID)).isInstanceOf(ControlMappingNotFoundException.class);
		then(controlMappingRepository).should(never()).deleteById(any());
	}

}
